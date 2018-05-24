/*
 * Copyright 2012-2018, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.representation;

import com.flipkart.flux.api.ClientElbDefinition;
import com.flipkart.flux.clientelb.dao.iface.ClientElbDAO;
import com.flipkart.flux.domain.ClientElb;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <code>ClientElbPersistenceService</code> class does persistence related operations in memory
 * before storing, updating, reading or deleting objects in DB. It is also responsible for maintaining
 * an in-memory cache of clientElb entries for faster query results.
 * @author akif.khan
 */
@Singleton
public class ClientElbPersistenceService {

    private ClientElbDAO clientElbDAO;

    private ConcurrentHashMap<String, ClientElb> clientElbCache;

    private ConcurrentHashMap<String, Long> lastAccessed;

    private Integer MAX_CACHE_SIZE;

    private static final Logger logger = LoggerFactory.getLogger(ClientElbPersistenceService.class);

    @Inject
    public ClientElbPersistenceService(ClientElbDAO clientElbDAO,
                                       @Named("elbCache.maxSize") Integer MAX_CACHE_SIZE) {

        this.clientElbDAO = clientElbDAO;
        this.MAX_CACHE_SIZE = MAX_CACHE_SIZE;
        this.clientElbCache = new ConcurrentHashMap<>();
        this.lastAccessed = new ConcurrentHashMap<>();
    }

    public Integer getClientElbCacheSize() {
        return clientElbCache.size();
    }

    public boolean clientElbCacheContainsKey(String key) {
        return clientElbCache.containsKey(key);
    }

    public String getClientElbCacheUrl(String key) {
        return clientElbCache.get(key).getElbUrl();
    }

    /**
     * Query Local Cache ClientElbCache for a given clientId
     * Updates and stores last access time of a Client ELb Entry in lastAccessed Map which is a basis for
     * LRU operation in case of cache entry replacement.
     * @param clientId
     * @return clientElb
     */
    private ClientElb queryClientElbCache(String clientId) {

        ClientElb clientElb = null;
        try {
            clientElb = clientElbCache.get(clientId);
            if (clientElb != null) {
                lastAccessed.replace(clientId, System.currentTimeMillis());
            }
        }
        catch(Exception ex) {
            logger.error("Exception in ClientElbCache read {} {}", ex.getMessage(), ex.getStackTrace());
        }
        return clientElb;

    }

    /**
     * Refreshes ClientElbCache in case of cache miss, either with a new entry addition into the cache if cache has not
     * reached it's maximum size or a LRU replacement policy.
     * It's a thread safe method synchronized on singleton instance of this class.
     * @param clientId
     * @param clientElb
     */
    private synchronized void clientElbCacheRefresh(String clientId, ClientElb clientElb) {

        try {

            if(clientElbCache.size() < MAX_CACHE_SIZE) {
                clientElbCache.put(clientId, clientElb);
                lastAccessed.put(clientId, System.currentTimeMillis());
            }

            else {
                String removableEntryKey = null;
                Long oldestAccessTime = Long.MAX_VALUE;
                for (String key : lastAccessed.keySet()) {
                    if (lastAccessed.get(key) < oldestAccessTime) {
                        oldestAccessTime = lastAccessed.get(key);
                        removableEntryKey = key;
                    }
                }

                if (removableEntryKey != null) {
                    clientElbCache.remove(removableEntryKey);
                    lastAccessed.remove(removableEntryKey);
                    clientElbCache.put(clientId, clientElb);
                    lastAccessed.put(clientId, System.currentTimeMillis());
                }
            }
            logger.info("ClientElbCache after refresh, size =" + clientElbCache.size() +
                    " entries: " + clientElbCache);
        }
        catch(Exception ex) {
            logger.error("Exception in ClientElbCache refresh {} {}", ex.getMessage(), ex.getStackTrace());
        }
    }

    /**
     * Updates in memory ClientElbCache when there is an URL update in database, double verification is done
     * within this function operation before update.
     * It's a thread safe method synchronized on singleton instance of this class.
     * @param clientId
     */
    private synchronized void clientElbCacheUpdate(String clientId) {
        try {
            if(clientElbCache.containsKey(clientId) && lastAccessed.containsKey(clientId)) {
                ClientElb clientElb = clientElbDAO.findById(clientId);
                if (clientElb != null) {
                    clientElbCache.replace(clientId, clientElb);
                    lastAccessed.replace(clientId, System.currentTimeMillis());
                    logger.info("After update ClientElbCache, " +
                            "size=" + this.clientElbCache.size() + " entries=" + this.clientElbCache);
                }
            }
        }
        catch(Exception ex) {
            logger.error("ClientElbCache update entry failed {} {}", ex.getMessage(), ex.getStackTrace());
        }
    }

    /**
     * Deletes in memory ClientElbCache entry when there is a delete in database, double verification is done
     * within this function operation before delete in cache.
     * It's a thread safe method synchronized on singleton instance of this class.
     * @param clientId
     */
    private synchronized void clientElbCacheDelete(String clientId) {

        try {
            if(clientElbCache.containsKey(clientId) && lastAccessed.containsKey(clientId)) {
                ClientElb clientElb = clientElbDAO.findById(clientId);

                if (clientElb == null) {
                    clientElbCache.remove(clientId);
                    lastAccessed.remove(clientId);
                    logger.info("After delete ClientElbCache, " +
                            "size=" + this.clientElbCache.size() + " entries=" + this.clientElbCache);
                }
            }
        }
        catch(Exception ex) {
            logger.error("ClientElbCache delete failed {} {}", ex.getMessage(), ex.getStackTrace());
        }
    }

    /**
     * ClientElbCache Initializer function, initialized during the startup of Flux Runtime. It populates cache
     * by retrieving oldest entries from database bounded by maximum cache size mentioned.
     * It's a thread safe method synchronized on singleton instance of this class.
     */
    public synchronized void clientElbCacheInitializer() {
        try {
            if (clientElbCache.size() < 1) {
                List<ClientElb> clientElbList;
                clientElbList = clientElbDAO.retrieveOldest(MAX_CACHE_SIZE);
                clientElbCache.clear();
                lastAccessed.clear();
                for (int i = 0; i < clientElbList.size(); i++) {
                    ClientElb curEntry = clientElbList.get(i);
                    clientElbCache.put(curEntry.getId(), curEntry);
                    lastAccessed.put(curEntry.getId(), System.currentTimeMillis());
                }
                logger.info("Initialized ClientElbCache, size=" + this.clientElbCache.size() +
                        " entries=" + this.clientElbCache);
            }
        }
        catch(Exception ex) {
            logger.error("Exception in local ClientElbCache initializer {} {}", ex.getMessage(), ex.getStackTrace());
        }
    }

    /**
     * Converts {@link ClientElbDefinition} to domain object {@link ClientElb}
     * @param clientElbDefinition
     * @return clientElb domain object
     */
    public ClientElb convertClientElbDefinitionToClientElb(ClientElbDefinition clientElbDefinition) {
        return new ClientElb(clientElbDefinition.getId(), clientElbDefinition.getElbUrl());
    }

    /**
     * Persists the ClientElb details object in DB.
     * @param clientElbDefinition
     * @return created clientElb
     */
    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public ClientElb persistClientElb(String clientId, ClientElbDefinition clientElbDefinition) {
        ClientElb clientElb = new ClientElb(clientElbDefinition.getId(), clientElbDefinition.getElbUrl());
        return clientElbDAO.create(clientId, clientElb);
    }

    /**
     * findById ClientElb details in DB. It queries in-memory clientElbCache before querying the database.
     * Block for clientElbCacheRefresh is thread safe on the instance of this class.
     * @param clientId
     * @return searched clientElb
     */
    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public ClientElb findByIdClientElb(String clientId) {

        ClientElb clientElb;
        clientElb = this.queryClientElbCache(clientId);
        if(clientElb != null)
            return clientElb;

        synchronized (this) {
            clientElb = clientElbDAO.findById(clientId);
            if (clientElb != null)
                this.clientElbCacheRefresh(clientId, clientElb);
            return clientElb;
        }

    }

    /**
     * Update ClientElb URL in DB.
     * @param clientId
     * @param clientElbUrl
     */
    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public void updateClientElb(String clientId, String clientElbUrl) {
        clientElbDAO.updateElbUrl(clientId, clientElbUrl);
        this.clientElbCacheUpdate(clientId);
    }

    /**
     * Delete ClientElb identified by given id in DB.
     * @param clientId
     */
    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public void deleteClientElb(String clientId) {
        clientElbDAO.delete(clientId);
        this.clientElbCacheDelete(clientId);
    }
}

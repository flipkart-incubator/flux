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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * <code>ClientElbPersistenceService</code> class does persistence related operations in memory
 * before storing, updating, reading or deleting objects in DB. It is also responsible for maintaining
 * an in-memory cache(Google Guava: https://github.com/google/guava/wiki) of clientElb entries for faster query results.
 *
 * @author akif.khan
 */
@Singleton
public class ClientElbPersistenceService {

    private ClientElbDAO clientElbDAO;

    private LoadingCache<String, String> clientElbCache;

    private Integer MAX_CACHE_SIZE;

    private static final Logger logger = LoggerFactory.getLogger(ClientElbPersistenceService.class);

    @Inject
    public ClientElbPersistenceService(ClientElbDAO clientElbDAO,
                                       @Named("elbCache.maxSize") Integer MAX_CACHE_SIZE) {
        this.clientElbDAO = clientElbDAO;
        this.MAX_CACHE_SIZE = MAX_CACHE_SIZE;
        this.clientElbCache = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE)
                .expireAfterAccess(1, TimeUnit.DAYS)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String clientElbId) throws Exception {
                        //Call ClientElbDAO layer, if entry not found in cache
                        return clientElbDAO.findById(clientElbId).getElbUrl();
                    }
                });
    }

    public long getClientElbCacheSize() {
        return this.clientElbCache.size();
    }

    public boolean clientElbCacheContainsKey(String key) {
        return this.clientElbCache.asMap().containsKey(key);
    }

    /**
     * Converts {@link ClientElbDefinition} to domain object {@link ClientElb}
     *
     * @param clientElbDefinition
     * @return clientElb domain object
     */
    public ClientElb convertClientElbDefinitionToClientElb(ClientElbDefinition clientElbDefinition) {
        return new ClientElb(clientElbDefinition.getId(), clientElbDefinition.getElbUrl());
    }

    /**
     * Persists the ClientElb details object in DB.
     *
     * @param clientElbDefinition
     * @return created clientElb
     */
    public ClientElb persistClientElb(String clientId, ClientElbDefinition clientElbDefinition) {
        ClientElb clientElb = new ClientElb(clientElbDefinition.getId(), clientElbDefinition.getElbUrl());
        return clientElbDAO.create(clientElb);
    }

    /**
     * findById ClientElb details in DB. It queries in-memory clientElbCache before querying the database.
     * Block for clientElbCacheRefresh is thread safe on the instance of this class.
     *
     * @param clientId
     * @return searched clientElb
     */
    public String findByIdClientElb(String clientId) {
        String clientElbUrl = null;
        try {
            clientElbUrl = this.clientElbCache.get(clientId);
            if (clientElbUrl == null) {
                logger.error("ClientElbCache entry not found in both DB and cache. Try registering ClientElb again.");
            }
        } catch (ExecutionException e) {
            logger.error("Error occured while accessing ClientElbCache Entry {} {}", clientId, e.getMessage(), e.getStackTrace());
        } catch (Exception ex) {
            logger.error("Errored occured while loading entity {} in Cache {}", clientId, ex);
        }
        return clientElbUrl;
    }

    /**
     * Update ClientElb URL in DB.
     *
     * @param clientId
     * @param clientElbUrl
     */
    public void updateClientElb(String clientId, String clientElbUrl) {
        clientElbDAO.updateElbUrl(clientId, clientElbUrl);
        this.clientElbCache.refresh(clientId);
        logger.info("After ClientElb entry update, cache contains: {}", clientElbCache.asMap());
    }

    /**
     * Delete ClientElb identified by given id in DB.
     *
     * @param clientId
     */
    public void deleteClientElb(String clientId) {
        clientElbDAO.delete(clientId);
        if (this.clientElbCache.asMap().containsKey(clientId)) {
            this.clientElbCache.asMap().remove(clientId);
        }
        logger.info("After ClientElb entry delete, cache contains: {}", clientElbCache.asMap());
    }
}
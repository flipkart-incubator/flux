package com.flipkart.flux.guice.module;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.dao.AuditDAOImpl;
import com.flipkart.flux.dao.EventsDAOImpl;
import com.flipkart.flux.dao.StateMachinesDAOImpl;
import com.flipkart.flux.dao.StatesDAOImpl;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.guice.interceptor.TransactionInterceptor;
import com.flipkart.flux.persistence.DatabaseConnectionPoolFactory;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.resource.DeploymentUnitResource;
import com.flipkart.flux.type.BlobType;
import com.flipkart.flux.type.ListJsonType;
import com.flipkart.flux.type.StoreFQNType;
import com.flipkart.polyguice.config.YamlConfiguration;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.mysql.jdbc.PreparedStatement;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.hibernate.cfg.Configuration;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.*;

/**
 * <code>JDBIModule</code> is a Guice {@link AbstractModule} implementation used for wiring jdbi, DAO classes.
 *
 * @author amitkumar.o
 */
public class JDBIModule extends AbstractModule {
    private static Logger logger = LoggerFactory.getLogger(JDBIModule.class);

    final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    final MetricRegistry metricRegistry = new MetricRegistry();
    final ObjectMapper objectMapper = Jackson.newObjectMapper();
    final Environment environment = new Environment("fluxRuntime", objectMapper, validator,
            metricRegistry, Thread.currentThread().getContextClassLoader());

    public static final String FLUX_HIBERNATE_CONFIG_NAME_SPACE = "flux.Database";

    /**
     * Performs concrete bindings for interfaces
     *
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        logger.info("Inside JDBI Module");
    }

    /**
     * Creates hibernate configuration from the configuration yaml properties.
     * Since the yaml properties are already flattened in input param <code>yamlConfiguration</code>
     * the method loops over them to selectively pick Hibernate specific properties.
     */
    @Provides
    @Singleton
    @Named("fluxJDBIConfiguration")
    public Map getConfiguration(YamlConfiguration yamlConfiguration) {
        return getConfiguration(yamlConfiguration, FLUX_HIBERNATE_CONFIG_NAME_SPACE);
    }


    @Provides
    @Singleton
    @Named("fluxJDBIConnectionPool")
    public DatabaseConnectionPoolFactory getDatabaseConnectionPoolFactory(@Named("fluxJDBIConfiguration") Map configuration) {

        final DBIFactory dbiFactory = new DBIFactory();
        final DBI dbi = dbiFactory.build(environment, getDataSourceFactory(configuration), "mysql");
        return new DatabaseConnectionPoolFactory(dbi);
    }


    private Map getConfiguration(YamlConfiguration yamlConfiguration, String prefix) {
        org.apache.commons.configuration.Configuration jdbiConfig = yamlConfiguration.subset(prefix);
        Iterator<String> propertyKeys = jdbiConfig.getKeys();
        Map configProperties = new HashMap<String, Object>();
        while (propertyKeys.hasNext()) {
            String propertyKey = propertyKeys.next();
            Object propertyValue = jdbiConfig.getProperty(propertyKey);
            configProperties.put(propertyKey, propertyValue);
        }
        return configProperties;
    }

    private DataSourceFactory getDataSourceFactory(Map configuration) {
        final DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass(configuration.get("driverClass").toString());
        dataSourceFactory.setUser(configuration.get("user").toString());
        dataSourceFactory.setPassword(configuration.get("password").toString());
        dataSourceFactory.setUrl(configuration.get("url").toString());
        dataSourceFactory.setValidationQuery(configuration.get("validationQuery").toString());
        dataSourceFactory.setMinSize(Integer.parseInt(configuration.get("minSize").toString()));
        dataSourceFactory.setMaxSize(Integer.parseInt(configuration.get("maxSize").toString()));
        dataSourceFactory.setMaxWaitForConnection(Duration.parse(configuration.get("maxWaitForConnection").toString()));
        dataSourceFactory.setEvictionInterval(Duration.parse(configuration.get("evictionInterval").toString()));
        dataSourceFactory.setMinIdleTime(Duration.parse(configuration.get("minIdleTime").toString()));
        try {
            TypeReference propertyType = new TypeReference<Map<String, String>>() {
            };
            final Map<String, String> properties = objectMapper.readValue(configuration.get("properties").toString(), propertyType);
            dataSourceFactory.setProperties(properties);
        } catch (Exception ex) {
            logger.error("Unable to load database properties config {}", ex.getMessage());
            dataSourceFactory.setProperties(new HashMap<String, String>());
        }
        return dataSourceFactory;
    }
}

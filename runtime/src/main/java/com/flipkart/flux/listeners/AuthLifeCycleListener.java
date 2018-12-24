package com.flipkart.flux.listeners;

import com.flipkart.kloud.authn.filter.AuthConfig;
import io.dropwizard.setup.Environment;
import org.apache.commons.lang.UnhandledException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.*;
import org.eclipse.jetty.util.component.LifeCycle;


import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class AuthLifeCycleListener implements LifeCycle.Listener {

    @Inject
    private final AuthConfig authConfig;
    private final Environment environment;

    public AuthLifeCycleListener(AuthConfig authConfig, Environment environment) {
        this.authConfig = authConfig;
        this.environment = environment;
    }

    @Override
    public void lifeCycleStarting(LifeCycle lifeCycleEvent) {
        if (!(lifeCycleEvent instanceof Server)) {
            return;
        }
        Server server = (Server) lifeCycleEvent;
        DefaultSessionIdManager sessionIdManager = sessionIdManager(server);


    }

    public DefaultSessionIdManager sessionIdManager(Server server) {
        DefaultSessionIdManager defaultSessionIdManager = new DefaultSessionIdManager(server);
        try {
            defaultSessionIdManager.setWorkerName(InetAddress.getLocalHost().getHostAddress().replace(".", "-"));
        } catch (UnknownHostException ex) {
            defaultSessionIdManager.setWorkerName("");
        }
        return defaultSessionIdManager;
    }


    SessionHandler sqlSessionHandler(String driver, String url){
        SessionHandler sessionHandler = new SessionHandler();
        SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
        sessionCache.setSessionDataStore(jdbcSessionDataStoreFactory(driver, url).getSessionDataStore(sessionHandler));
        sessionHandler.setSessionCache(sessionCache);
        return sessionHandler;
    }

    JDBCSessionDataStoreFactory jdbcSessionDataStoreFactory(String driver, String url){
        DatabaseAdaptor databaseAdaptor = new DatabaseAdaptor();
        databaseAdaptor.setDriverInfo(driver, url);
        JDBCSessionDataStoreFactory jdbcSessionDataStoreFactory = new JDBCSessionDataStoreFactory();
        jdbcSessionDataStoreFactory.setDatabaseAdaptor(databaseAdaptor);
        return  jdbcSessionDataStoreFactory;
    }

    @Override
    public void lifeCycleStarted(LifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {

    }

    @Override
    public void lifeCycleStopping(LifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleStopped(LifeCycle lifeCycle) {

    }
}

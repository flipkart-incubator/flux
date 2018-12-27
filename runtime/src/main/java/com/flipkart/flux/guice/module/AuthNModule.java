package com.flipkart.flux.guice.module;

import com.flipkart.kloud.authn.filter.AuthConfig;
import com.flipkart.kloud.authn.filter.AuthValueFactoryProvider;
import com.flipkart.kloud.filter.*;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class AuthNModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    @Named("AuthnConfig")
    AuthConfig getAuthNConfig(@Named("authConfig.authEnabled") boolean authEnabled,
                              @Named("authConfig.authnUrls") String[] urls,
                              @Named("authConfig.clientId") String clientId,
                              @Named("authConfig.clientSecret") String clientSecret,
                              @Named("authConfig.redirectUri") String redirectUrl,
                              @Named("authConfig.authIgnoreUrls") String authIgnoreUrls
    ) {

        AuthConfig authConfig = new AuthConfig();
        authConfig.setAuthEnabled(authEnabled);
        authConfig.setAuthnUrls(urls);
        authConfig.setClientId(clientId);
        authConfig.setClientSecret(clientSecret);
        authConfig.setRedirectUri(redirectUrl);
        authConfig.setAuthIgnoreUrls(authIgnoreUrls);
        return authConfig;
    }

    public static void configureUIApp(//Environment environment,
                                      @Named("DashboardContext") WebAppContext webAppContext,
                                      AuthConfig config) throws Exception {
        if (!config.isAuthEnabled()) {
            return;
        }

        String clientId = config.getClientId();
        String clientSecret = config.getClientSecret();
        LoginUrlFilter loginUrlFilter = new LoginUrlFilter(config.getAuthnUrls()[0], clientId, config.getScopes(), config.getAdditionalParams());

        CallbackUrlFilter callbackUrlFilter = new CallbackUrlFilter(config.getAuthnUrls()[0], clientId, clientSecret);
        LogoutUrlFilter logoutUrlFilter = new LogoutUrlFilter(config.getAuthnUrls()[0]);

        SecurityContextPersistenceFilter securityContextFilter = new SecurityContextPersistenceFilter();

        ServletEnvironment servlets = environment.servlets();

        loginUrlFilter.setLoginUrl(config.getLoginUrl());
        callbackUrlFilter.setRedirectUrl(config.getLoginUrl() + "_callback");

        if (config.getRedirectUri() != null) {
            loginUrlFilter.setRedirectUri(config.getRedirectUri());
            callbackUrlFilter.setRedirectUri(config.getRedirectUri());
        }
        // webAppContext.addFilter(SecurityContextPersistenceFilter.class.getName(), securityContextFilter);
        // webAppContext.
        webAppContext.addFilter(new FilterHolder(securityContextFilter), "/*", EnumSet.of(DispatcherType.REQUEST));
        webAppContext.addFilter(new FilterHolder(loginUrlFilter), "/*", EnumSet.of(DispatcherType.REQUEST));
        webAppContext.addFilter(new FilterHolder(callbackUrlFilter), "/*", EnumSet.of(DispatcherType.REQUEST));
        webAppContext.addFilter(new FilterHolder(logoutUrlFilter), "/*", EnumSet.of(DispatcherType.REQUEST));


       /* servlets.addFilter(SecurityContextPersistenceFilter.class.getName(), securityContextFilter)
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

        servlets.addFilter(LoginUrlFilter.class.getName(), loginUrlFilter)
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

        servlets.addFilter(CallbackUrlFilter.class.getName(), callbackUrlFilter)
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

        servlets.addFilter(LogoutUrlFilter.class.getName(), logoutUrlFilter)
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
*/

        EnsureAuthenticationFilter ensureAuthenticationFilter = new EnsureAuthenticationFilter(config.getLoginUrl(), config.getAuthIgnoreUrls());
        webAppContext.addFilter(new FilterHolder(ensureAuthenticationFilter), "/*", EnumSet.of(DispatcherType.REQUEST));
     //   servlets.addFilter(EnsureAuthenticationFilter.class.getName(), ensureAuthenticationFilter)
       //         .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(new AuthValueFactoryProvider.Binder<User>(User.class));
     //   resourceConfig.register(RequestLoggingFilter.class);
        webAppContext.addServlet(new ServletHolder(new ServletContainer(resourceConfig)), "/*");
    //    environment.jersey().register(new AuthValueFactoryProvider.Binder<User>(User.class));
    }

}

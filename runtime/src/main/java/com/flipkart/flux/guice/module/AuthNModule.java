package com.flipkart.flux.guice.module;

import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.flipkart.kloud.authn.filter.AuthConfig;
import com.flipkart.kloud.filter.*;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.inject.Named;
import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class AuthNModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Named("UiAuthnConfig")
    AuthConfig getUIAuthNConfig(@Named("uiAuthConfig.authEnabled") boolean authEnabled,
                              @Named("uiAuthConfig.authnUrl") String url,
                              @Named("uiAuthConfig.clientId") String clientId,
                              @Named("uiAuthConfig.clientSecret") String clientSecret,
                              @Named("uiAuthConfig.redirectUri") String redirectUrl,
                              @Named("uiAuthConfig.authIgnoreUrls") String authIgnoreUrls
    ) {
        String[] authnUrls = new String[1];
        authnUrls[0] = url;
        AuthConfig authConfig = new AuthConfig();
        authConfig.setAuthEnabled(authEnabled);
        authConfig.setAuthnUrls(authnUrls);
        authConfig.setClientId(clientId);
        authConfig.setClientSecret(clientSecret);
        authConfig.setRedirectUri(redirectUrl);
        authConfig.setAuthIgnoreUrls(authIgnoreUrls);
        return authConfig;
    }

    @Provides
    @Named("ApiAuthnConfig")
    AuthConfig getAuthNConfig(@Named("apiAuthConfig.authEnabled") boolean authEnabled,
                              @Named("apiAuthConfig.authnUrl") String url,
                              @Named("apiAuthConfig.clientId") String clientId,
                              @Named("apiAuthConfig.clientSecret") String clientSecret,
                              @Named("apiAuthConfig.authIgnoreUrls") String authIgnoreUrls

    ) {
        String[] authnUrls = new String[1];
        authnUrls[0] = url;
        AuthConfig authConfig = new AuthConfig();
        authConfig.setAuthEnabled(authEnabled);
        authConfig.setAuthnUrls(authnUrls);
        authConfig.setClientId(clientId);
        authConfig.setClientSecret(clientSecret);
        authConfig.setAuthIgnoreUrls(authIgnoreUrls);
        return authConfig;
    }


    public static void configureUIApp(WebAppContext webAppContext, AuthConfig config) {
        if (!config.isAuthEnabled()) {
            return;
        }

        String clientId = config.getClientId();
        String clientSecret = config.getClientSecret();
        LoginUrlFilter loginUrlFilter = new LoginUrlFilter(config.getAuthnUrls()[0], clientId, config.getScopes(), config.getAdditionalParams());

        CallbackUrlFilter callbackUrlFilter = new CallbackUrlFilter(config.getAuthnUrls()[0], clientId, clientSecret);
        LogoutUrlFilter logoutUrlFilter = new LogoutUrlFilter(config.getAuthnUrls()[0]);

        SecurityContextPersistenceFilter securityContextFilter = new SecurityContextPersistenceFilter();

        loginUrlFilter.setLoginUrl(config.getLoginUrl());
        callbackUrlFilter.setRedirectUrl(config.getLoginUrl() + "_callback");

        if (config.getRedirectUri() != null) {
            loginUrlFilter.setRedirectUri(config.getRedirectUri());
            callbackUrlFilter.setRedirectUri(config.getRedirectUri());
        }

        webAppContext.addFilter(new FilterHolder(securityContextFilter), "/*", EnumSet.of(DispatcherType.REQUEST));
        webAppContext.addFilter(new FilterHolder(loginUrlFilter), "/*", EnumSet.of(DispatcherType.REQUEST));
        webAppContext.addFilter(new FilterHolder(callbackUrlFilter), "/*", EnumSet.of(DispatcherType.REQUEST));
        webAppContext.addFilter(new FilterHolder(logoutUrlFilter), "/*", EnumSet.of(DispatcherType.REQUEST));

        EnsureAuthenticationFilter ensureAuthenticationFilter = new EnsureAuthenticationFilter(config.getLoginUrl(), null);
        webAppContext.addFilter(new FilterHolder(ensureAuthenticationFilter), "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    public static void configureApiApp(Server server, AuthConfig apiAuthConfig){
        final InstrumentedHandler handler = (InstrumentedHandler) server.getHandler();
        final ServletContextHandler context = (ServletContextHandler) handler.getHandler();
        SecurityContextPersistenceFilter securityContextPersistenceFilter = new SecurityContextPersistenceFilter();
        context.addFilter(new FilterHolder(securityContextPersistenceFilter), "/*", EnumSet.of(DispatcherType.REQUEST));
        SystemUserAuthenticationFilter systemAuthFilter =
                new SystemUserAuthenticationFilter(apiAuthConfig.getAuthnUrls(), apiAuthConfig.getClientId(), apiAuthConfig.getAuthIgnoreUrls());
        context.addFilter(new FilterHolder(systemAuthFilter), "/*", EnumSet.of(DispatcherType.REQUEST));

        EnsureAuthenticationFilter ensureAuthenticationFilter = new EnsureAuthenticationFilter(null, apiAuthConfig.getAuthIgnoreUrls());
        context.addFilter(new FilterHolder(ensureAuthenticationFilter), "/*", EnumSet.of(DispatcherType.REQUEST));
    }
}

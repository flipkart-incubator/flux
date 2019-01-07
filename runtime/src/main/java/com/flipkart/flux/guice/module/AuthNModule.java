package com.flipkart.flux.guice.module;

import com.flipkart.kloud.authn.filter.AuthConfig;
import com.flipkart.kloud.filter.*;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;

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
                              @Named("authConfig.authnUrl") String url,
                              @Named("authConfig.clientId") String clientId,
                              @Named("authConfig.clientSecret") String clientSecret,
                              @Named("authConfig.redirectUri") String redirectUrl,
                              @Named("authConfig.authIgnoreUrls") String authIgnoreUrls
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

    public static void configureUIApp(//Environment environment,
                                      WebAppContext webAppContext,
                                      AuthConfig config) {
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

        EnsureAuthenticationFilter ensureAuthenticationFilter = new EnsureAuthenticationFilter(config.getLoginUrl(), null   );
        webAppContext.addFilter(new FilterHolder(ensureAuthenticationFilter), "/*", EnumSet.of(DispatcherType.REQUEST));
    }

}

package com.flipkart.flux.guice.module;

import com.flipkart.kloud.authn.filter.AuthConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.eclipse.jetty.security.Authenticator;

import javax.inject.Named;
import javax.inject.Singleton;

public class AuthNModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    @Named("AuthNConfig")
    AuthConfig getAuthNConfig(@Named("authConfig.authEnabled") boolean authEnabled,
                              @Named("authConfig.authnUrls")String []urls,
                              @Named("authConfig.clientId")String clientId,
                              @Named("authConfig.clientSecret") String clientSecret,
                              @Named("authConfig.redirectUri") String redirectUrl,
                              @Named("authConfig.authIgnoreUrls") String authIgnoreUrls){

        AuthConfig authConfig = new AuthConfig();
        authConfig.setAuthEnabled(authEnabled);
        authConfig.setAuthnUrls(urls);
        authConfig.setClientId(clientId);
        authConfig.setClientSecret(clientSecret);
        authConfig.setRedirectUri(redirectUrl);
        authConfig.setAuthIgnoreUrls(authIgnoreUrls);
        return authConfig;
    }


}

package com.flipkart.flux.client;

import com.flipkart.flux.client.config.FluxClientConfiguration;
import com.flipkart.kloud.authn.AuthTokenService;
import com.flipkart.kloud.authn.OAuthClientCredentials;

import javax.inject.Inject;
import javax.inject.Provider;

public class AuthTokenServiceProvider implements Provider<AuthTokenService> {

    @Inject
    private Provider<FluxClientConfiguration> fluxClientConfigurationProvider;

    @Override
    public AuthTokenService get() {
        return provideAuthTokenService();
    }

    public AuthTokenService provideAuthTokenService() {
        FluxClientConfiguration configuration  = fluxClientConfigurationProvider.get();
        String authnUrl = System.getProperty("flux.authnUrl");
        if (authnUrl == null) {
            authnUrl = configuration.getAuthnUrl();
        }
        String authnClientId = System.getProperty("flux.authClientId");
        if (authnClientId == null) {
            authnClientId = configuration.getAuthnClientId();
        }
        String authnClientSecret = System.getProperty("flux.authnClientSecret");
        if (authnClientSecret == null) {
            authnClientSecret = configuration.getAuthnClientSecret();
        }
        return new AuthTokenService(authnUrl, new OAuthClientCredentials(authnClientId, authnClientSecret));
    }
}

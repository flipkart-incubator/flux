//package com.flipkart.flux.client;
//
//import com.flipkart.flux.client.config.FluxClientConfiguration;
//import com.flipkart.kloud.authn.AuthTokenService;
//import com.flipkart.kloud.authn.OAuthClientCredentials;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//
//public class AuthTokenServiceProvider implements Provider<AuthTokenService> {
//
//    @Inject
//    private Provider<FluxClientConfiguration> fluxClientConfigurationProvider;
//
//    @Override
//    public AuthTokenService get() {
//        return provideAuthTokenService();
//    }
//
//
//}

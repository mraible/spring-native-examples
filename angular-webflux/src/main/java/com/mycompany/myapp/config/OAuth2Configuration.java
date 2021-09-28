package com.mycompany.myapp.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;

@Configuration
public class OAuth2Configuration {

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
        ReactiveClientRegistrationRepository clientRegistrationRepository,
        ServerOAuth2AuthorizedClientRepository authorizedClientRepository
    ) {
        DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager = new DefaultReactiveOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientRepository
        );

        authorizedClientManager.setAuthorizedClientProvider(
            ReactiveOAuth2AuthorizedClientProviderBuilder
                .builder()
                .authorizationCode()
                .refreshToken(builder -> builder.clockSkew(Duration.ofMinutes(1)))
                .clientCredentials()
                .password()
                .build()
        );

        return authorizedClientManager;
    }
}

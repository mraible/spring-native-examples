package com.mycompany.myapp;

import com.mycompany.myapp.config.ApplicationProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.endpoint.AbstractWebClientReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

@SpringBootApplication
@NativeHint(options = "--enable-url-protocols=http,https")
@TypeHint(types = {
    ReactiveOAuth2AuthorizedClientManager.class,
    ReactiveOAuth2AuthorizedClientProviderBuilder.class,
    DefaultReactiveOAuth2AuthorizedClientManager.class,
    AbstractWebClientReactiveOAuth2AccessTokenResponseClient.class,

},
    typeNames = {
        "org.springframework.web.reactive.function.client.DefaultWebClientBuilder",
        "reactor.core.publisher.Traces$StackWalkerCallSiteSupplierFactory",
        "reactor.core.publisher.Traces$SharedSecretsCallSiteSupplierFactory",
        "reactor.core.publisher.Traces$ExceptionCallSiteSupplierFactory",

    },
    access = AccessBits.ALL)
@EnableConfigurationProperties({ApplicationProperties.class})
public class JhipsterApp {
    public static void main(String[] args) {
        var sa = new SpringApplicationBuilder(JhipsterApp.class)
            .profiles("dev")
            .run(args);
    }
}


class SimpleEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(JhipsterApp.class);

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
        String serverPort = env.getProperty("server.port");
        String contextPath = Optional
            .ofNullable(env.getProperty("server.servlet.context-path"))
            .filter(StringUtils::isNotBlank)
            .orElse("/");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        log.info(
            "\n----------------------------------------------------------\n\t" +
                "Application '{}' is running! Access URLs:\n\t" +
                "Local: \t\t{}://localhost:{}{}\n\t" +
                "External: \t{}://{}:{}{}\n\t" +
                "Profile(s): \t{}\n----------------------------------------------------------",
            env.getProperty("spring.application.name"),
            protocol,
            serverPort,
            contextPath,
            protocol,
            hostAddress,
            serverPort,
            contextPath,
            env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles()
        );
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        logApplicationStartup(environment);
    }
}

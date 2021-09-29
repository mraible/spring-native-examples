package com.mycompany.myapp;

import com.mycompany.myapp.config.ApplicationProperties;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.endpoint.AbstractWebClientReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import tech.jhipster.config.DefaultProfileUtil;
import tech.jhipster.config.JHipsterConstants;

@NativeHint(options = "--enable-url-protocols=http,https")
@TypeHint(types = {
    ReactiveOAuth2AuthorizedClientManager.class,
    ReactiveOAuth2AuthorizedClientProviderBuilder.class,
    DefaultReactiveOAuth2AuthorizedClientManager.class,
    AbstractWebClientReactiveOAuth2AccessTokenResponseClient.class
},
    typeNames = {
        "org.springframework.web.reactive.function.client.DefaultWebClientBuilder",
        "reactor.core.publisher.Traces$StackWalkerCallSiteSupplierFactory",
        "reactor.core.publisher.Traces$SharedSecretsCallSiteSupplierFactory",
        "reactor.core.publisher.Traces$ExceptionCallSiteSupplierFactory",
        "com.mycompany.myapp.repository.UserRepositoryInternal",
        "com.mycompany.myapp.repository.UserRepositoryInternal$UserRepositoryInternalImpl",
        "com.mycompany.myapp.domain.User",
        "org.HdrHistogram.Histogram",
        "org.HdrHistogram.ConcurrentHistogram"
    },
    access = AccessBits.ALL)
@SpringBootApplication
@EnableConfigurationProperties({ LiquibaseProperties.class, ApplicationProperties.class })
public class PostgresApp {

    private static final Logger log = LoggerFactory.getLogger(PostgresApp.class);

    private final Environment env;

    public PostgresApp(Environment env) {
        this.env = env;
    }

    /**
     * Initializes Postgres.
     * <p>
     * Spring profiles can be configured with a program argument --spring.profiles.active=your-active-profile
     * <p>
     * You can find more information on how profiles work with JHipster on <a href="https://www.jhipster.tech/profiles/">https://www.jhipster.tech/profiles/</a>.
     */
    @PostConstruct
    public void initApplication() {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (
            activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) &&
            activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
        ) {
            log.error(
                "You have misconfigured your application! It should not run " + "with both the 'dev' and 'prod' profiles at the same time."
            );
        }
        if (
            activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) &&
            activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_CLOUD)
        ) {
            log.error(
                "You have misconfigured your application! It should not " + "run with both the 'dev' and 'cloud' profiles at the same time."
            );
        }
    }

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PostgresApp.class);
        DefaultProfileUtil.addDefaultProfile(app);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

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
}

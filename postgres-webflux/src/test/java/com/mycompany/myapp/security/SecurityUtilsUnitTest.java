package com.mycompany.myapp.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.util.context.Context;

import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the {@link SecurityUtils} utility class.
 */
class SecurityUtilsUnitTest {

    @Test
    void testgetCurrentUserLogin() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin")))
            .block();
        assertThat(login).isEqualTo("admin");
    }

    @Test
    void testIsAuthenticated() {
        Boolean isAuthenticated = SecurityUtils
            .isAuthenticated()
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin")))
            .block();
        assertThat(isAuthenticated).isTrue();
    }

    @Test
    void testAnonymousIsNotAuthenticated() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS));
        Boolean isAuthenticated = SecurityUtils
            .isAuthenticated()
            .contextWrite(
                ReactiveSecurityContextHolder.withAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin", authorities))
            )
            .block();
        assertThat(isAuthenticated).isFalse();
    }

    @Test
    void testHasCurrentUserAnyOfAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("admin", "admin", authorities)
        );
        Boolean hasCurrentUserThisAuthority = SecurityUtils
            .hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)
            .contextWrite(context)
            .block();
        assertThat(hasCurrentUserThisAuthority).isTrue();

        hasCurrentUserThisAuthority =
            SecurityUtils
                .hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ANONYMOUS, AuthoritiesConstants.ADMIN)
                .contextWrite(context)
                .block();
        assertThat(hasCurrentUserThisAuthority).isFalse();
    }

    @Test
    void testHasCurrentUserNoneOfAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("admin", "admin", authorities)
        );
        Boolean hasCurrentUserThisAuthority = SecurityUtils
            .hasCurrentUserNoneOfAuthorities(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)
            .subscriberContext(context)
            .block();
        assertThat(hasCurrentUserThisAuthority).isFalse();

        hasCurrentUserThisAuthority =
            SecurityUtils
                .hasCurrentUserNoneOfAuthorities(AuthoritiesConstants.ANONYMOUS, AuthoritiesConstants.ADMIN)
                .subscriberContext(context)
                .block();
        assertThat(hasCurrentUserThisAuthority).isTrue();
    }

    @Test
    void testHasCurrentUserThisAuthority() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("admin", "admin", authorities)
        );
        Boolean hasCurrentUserThisAuthority = SecurityUtils
            .hasCurrentUserThisAuthority(AuthoritiesConstants.USER)
            .subscriberContext(context)
            .block();
        assertThat(hasCurrentUserThisAuthority).isTrue();

        hasCurrentUserThisAuthority =
            SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN).subscriberContext(context).block();
        assertThat(hasCurrentUserThisAuthority).isFalse();
    }
}

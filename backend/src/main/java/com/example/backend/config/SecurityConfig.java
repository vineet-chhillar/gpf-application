
package com.example.backend.config;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        CookieCsrfTokenRepository csrfRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        csrfRepository.setHeaderName("X-XSRF-TOKEN");

        http
            .cors(Customizer.withDefaults())

            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfRepository)
                .csrfTokenRequestHandler(
                    new SpaCsrfTokenRequestHandler()
                )
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/index",
                    "/loginprocess",
                    "/logout",
                    "/fonts/**",
                    "/login/**",
                    "/images/**",
                    "/adminlte3/**",
                    "/css/**",
                    "/js/**"
                ).permitAll()

                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                .anyRequest().authenticated()
            );

        // Force generation of the deferred CSRF token so the
        // repository can write the XSRF-TOKEN cookie.
        http.addFilterAfter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain)
                    throws ServletException, IOException {

                CsrfToken token = (CsrfToken) request.getAttribute(
                    CsrfToken.class.getName()
                );

                if (token != null) {
                    token.getToken();
                }

                filterChain.doFilter(request, response);
            }
        }, CsrfFilter.class);

        return http.build();
    }

    @Bean
public CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of(
        "http://localhost:3000",
        "http://localhost:9090",
        "https://dev.digital.nic.in",
        "http://dev.digital.nic.in"
    ));

    config.setAllowedMethods(List.of(
        "GET", "POST", "PUT", "PATCH",
        "DELETE", "OPTIONS"
    ));

    config.setAllowedHeaders(List.of(
        "Content-Type",
        "Accept",
        "X-XSRF-TOKEN"
    ));

    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", config);

    return source;
}

    private static final class SpaCsrfTokenRequestHandler
            implements CsrfTokenRequestHandler {

        private final CsrfTokenRequestHandler plain =
                new CsrfTokenRequestAttributeHandler();

        private final CsrfTokenRequestHandler xor =
                new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(
                HttpServletRequest request,
                HttpServletResponse response,
                Supplier<CsrfToken> csrfToken) {

            // Keep Spring Security's BREACH protection for request attributes.
            xor.handle(request, response, csrfToken);

            // Force deferred token generation and cookie creation.
            csrfToken.get();
        }

        @Override
        public String resolveCsrfTokenValue(
                HttpServletRequest request,
                CsrfToken csrfToken) {

            // Axios sends the raw token from the XSRF-TOKEN cookie.
            if (StringUtils.hasText(
                    request.getHeader(csrfToken.getHeaderName()))) {

                return plain.resolveCsrfTokenValue(request, csrfToken);
            }

            return xor.resolveCsrfTokenValue(request, csrfToken);
        }
    }
}

package de.tigges.tchreservation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String[] WHITELIST_URLS = {
            "/angular/**",
            "/resources/**",
            "/css/**",
            "/actuator/**",
            "/api-docs/**",
            "/h2-console/**",
            "/rest/reservation/getOccupations/**",
            "/rest/reservation/systemconfig/**",
            "/rest/systemconfig/getone/*",
            "/rest/systemconfig/getall",
            "/rest/application/properties",
            "/rest/user/me",
            "/rest/news/**"
    };

    private static final String ANGULAR_URL = "/angular/index.html";
    public static final String LOGIN_PROCESSING_URL = "/login";

    @Value("${login.remember-me.key}")
    private String rememberMeKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(SecurityConfig::formLoginConfiguration)
                .logout(SecurityConfig::logoutConfiguration)
                .authorizeHttpRequests(SecurityConfig::requestConfiguration)
                .csrf(AbstractHttpConfigurer::disable)
                .rememberMe(this::rememberMeConfiguration)
                .build();
    }

    private static void logoutConfiguration(LogoutConfigurer<HttpSecurity> logout) {
        logout.logoutSuccessUrl(ANGULAR_URL);
    }

    private static void requestConfiguration(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
        requests.requestMatchers(WHITELIST_URLS).permitAll()
                .anyRequest().authenticated();
    }

    private static void formLoginConfiguration(FormLoginConfigurer<HttpSecurity> formLogin) {
        formLogin.loginPage(ANGULAR_URL)
                .loginProcessingUrl(LOGIN_PROCESSING_URL)
                .failureHandler(new AppAuthenticationFailureHandler());
    }

    private void rememberMeConfiguration(RememberMeConfigurer<HttpSecurity> rememberMe) {
        rememberMe.key(rememberMeKey).alwaysRemember(true);
    }

    private static class AppAuthenticationFailureHandler implements AuthenticationFailureHandler {
        @Override
        public void onAuthenticationFailure(
                HttpServletRequest request,
                HttpServletResponse response,
                AuthenticationException exception) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }
}

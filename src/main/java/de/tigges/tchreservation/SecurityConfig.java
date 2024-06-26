package de.tigges.tchreservation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ANGULAR_URL = "/angular/index.html";

    @Value("${login.remember-me.key}")
    private String rememberMeKey;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.formLogin(formLogin -> formLogin
                        .loginPage(ANGULAR_URL)
                        .loginProcessingUrl("/login")
                        .failureHandler(new AppAuthenticationFailureHandler()))
                .logout(logout -> logout.logoutSuccessUrl(ANGULAR_URL))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(WHITELIST_URLS).permitAll()
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .rememberMe(rememberMe -> rememberMe.key(rememberMeKey))
        ;
        return http.build();
    }

    private static class AppAuthenticationFailureHandler implements AuthenticationFailureHandler {
        @Override
        public void onAuthenticationFailure(
                HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }
}

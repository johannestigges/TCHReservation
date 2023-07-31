package de.tigges.tchreservation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    public static final String[] WHITELIST_URLS = {
            "/resources/**",
            "/css/**",
            "/index",
            "/actuator/**",
            "/api-docs/**",
            "/angular/**",
            "/h2-console/**",
            "/rest/reservation/getOccupations/**",
            "/rest/reservation/systemconfig/**",
            "/rest/systemconfig/getone/*",
            "/rest/application/properties",
            "/rest/user/me"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.formLogin(formLogin -> formLogin
                        .loginPage("/angular/#/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/angular/index.html")
                        .failureHandler(new AppAuthenticationFailureHandler()))
                .logout(logout -> logout
                        .logoutSuccessUrl("/angular/index.html"))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(WHITELIST_URLS).permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())
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

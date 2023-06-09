package de.tigges.tchreservation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.SecurityFilterChain;

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
        http.authorizeHttpRequests(requests -> requests
                        .requestMatchers(WHITELIST_URLS).permitAll()
                        .anyRequest().authenticated())
                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .csrf().disable()
        ;
        return http.build();
    }
}

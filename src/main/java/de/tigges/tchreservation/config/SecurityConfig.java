package de.tigges.tchreservation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http.antMatcher("/**")
			.authorizeRequests()
				.antMatchers("resources/**", "css/**", "/index", 
						"/actuator/**", "/api-docs/**", 
						"reservation/getOccupations/**").permitAll()
				.anyRequest().authenticated()
			.and().formLogin().permitAll()
			.and().logout().permitAll()
			.and().csrf()
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
		;
		// @formatter:on
	}
}

package de.tigges.tchreservation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * security configuration for the application
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http.authorizeRequests()
				// allow some static resources, mvc and some rest services without
				// authentication
				.antMatchers(
						"/resources/**", 
						"/css/**", 
						"/index",
						"/actuator/**", 
						"/api-docs/**",
						"/mvc/**", 
						"/angular/**",
						"/reservation/getOccupations/**", 
						"/reservation/systemconfig/**",
						"/user/me")
					.permitAll()
				// all other rest services need authentication
				.anyRequest().authenticated()
				// login and logout
				.and().formLogin()
					.permitAll()
				.and().logout()
					.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
					.logoutSuccessUrl("/angular")
					.permitAll()
				// use csrf the angular way
				.and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
		// @formatter:on
	}
}
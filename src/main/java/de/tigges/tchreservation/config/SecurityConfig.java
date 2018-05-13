package de.tigges.tchreservation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeRequests()
				.antMatchers("resources/**", "css/**", "/index", 
						"/actuator/**", "/api-docs/**", 
						"/mvc/**", 
						"/angular/**",
						"/reservation/getOccupations/**", 
						"/user/me").permitAll()
				.antMatchers("/user/**").hasRole("ADMIN")
				.anyRequest().authenticated()
			.and().formLogin().permitAll()
			.and().logout().permitAll()
			.and().csrf().disable()
			
		;
		// @formatter:on
	}
}

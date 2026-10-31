package com.tokoku.order.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.Customizer.withDefaults;

/** Resource server JWT dengan Lambda DSL (slide 88): deny by default. */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	@Bean
	SecurityFilterChain api(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()
				.requestMatchers(GET, "/api/orders/**").hasAuthority("SCOPE_orders.read")
				.requestMatchers(POST, "/api/orders").hasAuthority("SCOPE_orders.write")
				.requestMatchers(POST, "/api/payments").hasAuthority("SCOPE_payments.write")
				.anyRequest().authenticated())
			.oauth2ResourceServer(oauth -> oauth.jwt(withDefaults()))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			// Bearer token, tanpa cookie session: CSRF tidak relevan.
			.csrf(csrf -> csrf.disable());
		return http.build();
	}

}

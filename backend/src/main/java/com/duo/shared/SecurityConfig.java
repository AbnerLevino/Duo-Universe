package com.duo.shared;

import com.duo.auth.GoogleLoginSuccessHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Política: tudo exige autenticação (RN-20), exceto a saúde da aplicação e o início do login com Google (DEC-030).
 * A sessão própria (tokens de acesso e renovação) entra na etapa 5 do épico 1.
 */
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, GoogleLoginSuccessHandler googleLoginSuccessHandler)
			throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
				.anyRequest().authenticated())
			.oauth2Login(oauth -> oauth.successHandler(googleLoginSuccessHandler))
			.exceptionHandling(errors -> errors
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
		return http.build();
	}
}

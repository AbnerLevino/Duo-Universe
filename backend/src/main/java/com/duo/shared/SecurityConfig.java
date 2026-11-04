package com.duo.shared;

import com.duo.auth.GoogleLoginSuccessHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Política: tudo exige autenticação (RN-20), exceto a saúde da aplicação, o início do login com Google (DEC-030)
 * e a renovação/saída da sessão, que se provam pelo cookie do token de renovação (RA-04).
 * As rotas da API são autenticadas pelo token de acesso (JWT) no cabeçalho {@code Authorization: Bearer}.
 * O CSRF fica desligado: a API não usa cookie para autenticar (só o refresh/logout, que é SameSite=Strict).
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
				.requestMatchers(HttpMethod.POST, "/api/auth/refresh", "/api/auth/logout").permitAll()
				.anyRequest().authenticated())
			.oauth2Login(oauth -> oauth.successHandler(googleLoginSuccessHandler))
			.oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
			.exceptionHandling(errors -> errors
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
		return http.build();
	}
}

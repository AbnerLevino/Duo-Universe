package com.duo.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Política: tudo exige autenticação (RN-20), exceto a saúde da aplicação, o início do login com Google (DEC-030)
 * e a renovação/saída da sessão, que se provam pelo cookie do token de renovação (RA-04).
 * As rotas da API são autenticadas pelo token de acesso (JWT) no cabeçalho {@code Authorization: Bearer}.
 * O CSRF fica desligado: a API não usa cookie para autenticar (só o refresh/logout, que é SameSite=Strict).
 */
@Configuration
public class SecurityConfig {

	/**
	 * O parâmetro é a interface do próprio Spring Security, não {@code GoogleLoginSuccessHandler} direto: se
	 * fosse o tipo concreto, {@code shared} passaria a depender de {@code auth} — e {@code auth} já depende
	 * de {@code users}, que (Etapa 13) passou a depender de {@code shared} para o {@code DuoHttpException} —
	 * fechando um ciclo entre os três módulos. Dependendo da interface, o Spring ainda encontra e injeta o
	 * único bean que a implementa ({@code GoogleLoginSuccessHandler}), mas o ArchUnit não vê mais a ligação.
	 */
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationSuccessHandler googleLoginSuccessHandler)
			throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/auth/refresh", "/api/auth/logout").permitAll()
				// Só existe de verdade com o perfil "dev" ativo (DevLoginController); sem o perfil, o bean
				// não existe e a rota 404, então liberá-la aqui não abre nada em produção.
				.requestMatchers(HttpMethod.GET, "/api/dev/**").permitAll()
				.anyRequest().authenticated())
			.oauth2Login(oauth -> oauth.successHandler(googleLoginSuccessHandler))
			.oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
			.exceptionHandling(errors -> errors
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
		return http.build();
	}
}

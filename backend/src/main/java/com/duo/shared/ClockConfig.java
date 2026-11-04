package com.duo.shared;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** O relógio vira um bean para os testes poderem fixar o "agora" (ex.: expiração de token). */
@Configuration
public class ClockConfig {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}

package com.duo.shared.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.duo.shared.RateLimiter;
import com.duo.shared.exception.RateLimitExceededException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * RF-A06: várias primeiras tentativas da mesma chave ao mesmo tempo. Antes da correção, as que perdiam a corrida
 * pela criação da linha batiam no índice único e viravam erro 500; agora todas passam (ou são limitadas, nunca
 * quebram). {@code @SpringBootTest} porque o proxy transacional do Spring só existe com o contexto de verdade.
 */
@ActiveProfiles("test")
@SpringBootTest
class RateLimiterConcurrencyTest {

	private static final int THREADS = 8;

	@Autowired
	RateLimiter limiter;

	@Autowired
	JdbcTemplate jdbc;

	@Test
	void simultaneousFirstAttemptsOfTheSameKeyNeverFailWithAnUnexpectedError() throws Exception {
		String key = UUID.randomUUID().toString();
		ExecutorService pool = Executors.newFixedThreadPool(THREADS);
		CountDownLatch ready = new CountDownLatch(THREADS);
		CountDownLatch go = new CountDownLatch(1);
		try {
			List<Future<Boolean>> results = new ArrayList<>();
			for (int i = 0; i < THREADS; i++) {
				results.add(pool.submit(() -> {
					ready.countDown();
					go.await();
					try {
						limiter.checkAndRecord("rate-concurrency-test", key, 5, Duration.ofHours(1));
						return true;
					}
					catch (RateLimitExceededException expected) {
						return false;
					}
				}));
			}
			ready.await();
			go.countDown();
			long allowed = 0;
			for (Future<Boolean> result : results) {
				if (result.get()) {
					allowed++;
				}
			}
			// 8 tentativas contra um limite de 5: exatamente 5 passam e nenhuma quebra com erro inesperado
			assertThat(allowed).isEqualTo(5);
		}
		finally {
			pool.shutdownNow();
			jdbc.update("delete from rate_limit_attempts where scope = ? and attempt_key = ?",
					"rate-concurrency-test", key);
		}
	}
}

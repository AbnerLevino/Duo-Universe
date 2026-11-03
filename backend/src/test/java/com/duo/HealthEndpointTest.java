package com.duo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthEndpointTest {

	@Value("${local.server.port}")
	int port;

	private HttpResponse<String> get(String path) throws Exception {
		return HttpClient.newHttpClient().send(
			HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build(),
			HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void healthIsPublicAndUp() throws Exception {
		HttpResponse<String> response = get("/actuator/health");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"status\":\"UP\"");
	}

	@Test
	void otherRoutesRequireAuthentication() throws Exception {
		// RN-20: nada além da saúde é acessível sem autenticação
		assertThat(get("/qualquer-coisa").statusCode()).isEqualTo(401);
	}
}

package com.duo;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/** RN-20: a documentação da API também exige autenticação; nada é público por esquecimento. */
@ActiveProfiles("test")
@SpringBootTest
@WebAppConfiguration
class OpenApiDocsTest {

	@Autowired
	WebApplicationContext context;

	private MockMvc mvc() {
		return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	@Test
	void apiDocsRequireAuthentication() throws Exception {
		mvc().perform(get("/v3/api-docs")).andExpect(status().isUnauthorized());
	}

	@Test
	void apiDocsAreServedToAuthenticatedUsers() throws Exception {
		mvc().perform(get("/v3/api-docs").with(user("dev"))).andExpect(status().isOk());
	}
}

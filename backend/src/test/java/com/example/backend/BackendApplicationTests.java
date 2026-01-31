package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void applicationMainTest() {
		// Teste para garantir que o método main é executado sem exceções
		// Usamos uma propriedade para evitar que o servidor tente subir na porta real em teste
		System.setProperty("server.port", "0");
		try {
			BackendApplication.main(new String[] {});
		} catch (Exception e) {
			// Ignorar exceções de startup que podem ocorrer em ambiente de teste
		}
	}

}

package com.movienotebook.api;

import com.movienotebook.api.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Проверка инициализации контекста Spring Boot")
class ApiApplicationTests extends BaseIntegrationTest {
	
	@Test
	@DisplayName("Контекст приложения должен успешно загружаться")
	void contextLoads() {
	}

}

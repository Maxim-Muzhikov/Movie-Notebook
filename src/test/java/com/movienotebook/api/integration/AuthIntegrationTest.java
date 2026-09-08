package com.movienotebook.api.integration;

import com.movienotebook.api.dto.auth.LoginRequestDto;
import com.movienotebook.api.dto.auth.RegisterRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class AuthIntegrationTest extends BaseIntegrationTest {
	
	@Test
	@DisplayName("Полный цикл: Регистрация и последующий логин")
	void shouldRegisterAndLoginSuccessfully() throws Exception {
		RegisterRequestDto registerDto = new RegisterRequestDto(
				"integration_user",
				"test@test.com",
				"Password123!",
				true
		);
		
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.username").value("integration_user"));
		
		LoginRequestDto loginDto = new LoginRequestDto(
				"integration_user",
				"Password123!"
		);
		
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").exists())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());
	}
	
	@Test
	@DisplayName("Ошибка регистрации при существующем username")
	void shouldFailRegistrationWhenUsernameExists() throws Exception {
		RegisterRequestDto registerDto1 = new RegisterRequestDto(
				"duplicate_user", "email1@test.com", "Password123!", true);
		
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerDto1)))
				.andExpect(status().isCreated());
		
		RegisterRequestDto registerDto2 = new RegisterRequestDto(
				"duplicate_user", "email2@test.com", "Password123!", true);
		
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerDto2)))
				.andExpect(status().isConflict());
	}
}
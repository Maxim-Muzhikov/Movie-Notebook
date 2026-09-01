package com.movienotebook.api.controller;

import com.movienotebook.api.dto.auth.LoginRequestDto;
import com.movienotebook.api.dto.auth.LoginResponseDto;
import com.movienotebook.api.dto.auth.RegisterRequestDto;
import com.movienotebook.api.dto.user.UserResponseDto;
import com.movienotebook.api.security.CustomUserDetailsService;
import com.movienotebook.api.security.JwtService;
import com.movienotebook.api.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureJsonTesters
class AuthControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private JacksonTester<RegisterRequestDto> registerRequestJsonTester;
	
	@Autowired
	private JacksonTester<LoginRequestDto> loginRequestJsonTester;
	
	@MockitoBean
	private JwtService jwtService;
	
	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;
	
	@MockitoBean
	private AuthService authService;
	
	@Nested
	@DisplayName("POST /api/v1/auth/register - Регистрация пользователя")
	class RegisterTests {
		
		@Test
		@DisplayName("Успешная регистрация нового пользователя (201)")
		void register_ValidRequest_Returns201() throws Exception {
			// Arrange
			var request = new RegisterRequestDto(
					"john_doe",
					"john.doe@example.com",
					"P@ssword123",
					true
			);
			
			var response = new UserResponseDto(
					1L,
					"john_doe",
					"john.doe@example.com",
					"ROLE_USER",
					OffsetDateTime.parse("2026-09-01T12:00:00Z")
			);
			
			when(authService.register(any(RegisterRequestDto.class))).thenReturn(response);
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/auth/register")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(registerRequestJsonTester.write(request).getJson()))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.id").value(1L))
					.andExpect(jsonPath("$.username").value("john_doe"))
					.andExpect(jsonPath("$.email").value("john.doe@example.com"))
					.andExpect(jsonPath("$.role").value("ROLE_USER"));
			
			verify(authService).register(any(RegisterRequestDto.class));
		}
		
		@Test
		@DisplayName("Ошибка валидации: некорректный формат email (400)")
		void register_InvalidEmail_Returns400() throws Exception {
			// Arrange
			var invalidRequest = new RegisterRequestDto(
					"john_doe",
					"not-an-email",
					"P@ssword123",
					true
			);
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/auth/register")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(registerRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(authService);
		}
		
		@Test
		@DisplayName("Ошибка валидации: пароль короче 8 символов (400)")
		void register_ShortPassword_Returns400() throws Exception {
			// Arrange
			var invalidRequest = new RegisterRequestDto(
					"john_doe",
					"john.doe@example.com",
					"short",
					true
			);
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/auth/register")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(registerRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(authService);
		}
		
		@Test
		@DisplayName("Ошибка валидации: не принято пользовательское соглашение (400)")
		void register_AgreementNotAccepted_Returns400() throws Exception {
			// Arrange
			var invalidRequest = new RegisterRequestDto(
					"john_doe",
					"john.doe@example.com",
					"P@ssword123",
					false
			);
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/auth/register")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(registerRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(authService);
		}
	}
	
	@Nested
	@DisplayName("POST /api/v1/auth/login - Аутентификация (вход)")
	class LoginTests {
		
		@Test
		@DisplayName("Успешный вход пользователя (200)")
		void login_ValidRequest_Returns200() throws Exception {
			// Arrange
			var request = new LoginRequestDto("john_doe", "P@ssword123");
			var response = new LoginResponseDto("mocked.jwt.token");
			
			when(authService.login(any(LoginRequestDto.class))).thenReturn(response);
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/auth/login")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(loginRequestJsonTester.write(request).getJson()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.accessToken").value("mocked.jwt.token"));
			
			verify(authService).login(any(LoginRequestDto.class));
		}
		
		@Test
		@DisplayName("Ошибка валидации: пустое имя пользователя (400)")
		void login_BlankUsername_Returns400() throws Exception {
			// Arrange
			var invalidRequest = new LoginRequestDto("", "P@ssword123");
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/auth/login")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(loginRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(authService);
		}
		
		@Test
		@DisplayName("Ошибка валидации: пароль короче 8 символов (400)")
		void login_ShortPassword_Returns400() throws Exception {
			// Arrange
			var invalidRequest = new LoginRequestDto("john_doe", "12345");
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/auth/login")
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(loginRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(authService);
		}
	}
}
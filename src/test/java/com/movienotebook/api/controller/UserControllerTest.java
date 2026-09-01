package com.movienotebook.api.controller;

import com.movienotebook.api.dto.collection.CollectionResponseDto;
import com.movienotebook.api.dto.user.UserResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.security.CustomUserDetailsService;
import com.movienotebook.api.security.JwtService;
import com.movienotebook.api.service.CollectionService;
import com.movienotebook.api.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureJsonTesters
class UserControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private JwtService jwtService;
	
	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;
	
	@MockitoBean
	private UserService userService;
	
	@MockitoBean
	private CollectionService collectionService;
	
	@Nested
	@DisplayName("GET /api/v1/users/me - Получение профиля текущего пользователя")
	class GetCurrentUserProfileTests {
		
		@Test
		@DisplayName("Успешное получение своего профиля (200)")
		void getCurrentUserProfile_Returns200() throws Exception {
			// Arrange
			String username = "john_doe";
			var userDetails = new CustomUserDetails(1L, username, "pass", List.of());
			
			var responseDto = new UserResponseDto(
					1L,
					username,
					"john.doe@example.com",
					"ROLE_USER",
					OffsetDateTime.parse("2026-09-01T12:00:00Z")
			);
			
			when(userService.getByUsername(any())).thenReturn(responseDto);
			
			// Act & Assert
			mockMvc.perform(get("/api/v1/users/me")
							.with(user(userDetails)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1L))
					.andExpect(jsonPath("$.username").value(username))
					.andExpect(jsonPath("$.email").value("john.doe@example.com"))
					.andExpect(jsonPath("$.role").value("ROLE_USER"));
			
			verify(userService).getByUsername(any());
		}
	}
	
	@Nested
	@DisplayName("GET /api/v1/users/{userId}/collections - Получение публичных коллекций пользователя")
	class GetUsersCollectionsTests {
		
		@Test
		@DisplayName("Успешное получение публичных коллекций (200)")
		void getUsersCollections_Returns200() throws Exception {
			// Arrange
			Long userId = 2L;
			var collectionDto = new CollectionResponseDto(
					10L,
					"Любимые драмы",
					"Подборка лучших драм",
					true,
					OffsetDateTime.parse("2026-09-01T10:00:00Z"),
					OffsetDateTime.parse("2026-09-01T11:00:00Z")
			);
			
			when(collectionService.getPublic(eq(userId))).thenReturn(List.of(collectionDto));
			
			// Act & Assert
			mockMvc.perform(get("/api/v1/users/{userId}/collections", userId)
							.with(user("testuser")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$[0].id").value(10L))
					.andExpect(jsonPath("$[0].name").value("Любимые драмы"))
					.andExpect(jsonPath("$[0].description").value("Подборка лучших драм"))
					.andExpect(jsonPath("$[0].isPublic").value(true));
			
			verify(collectionService).getPublic(eq(userId));
		}
	}
}
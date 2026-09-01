package com.movienotebook.api.controller;

import com.movienotebook.api.dto.collection.CollectionRequestDto;
import com.movienotebook.api.dto.collection.CollectionResponseDto;
import com.movienotebook.api.dto.collection.CollectionWithMoviesResponseDto;
import com.movienotebook.api.dto.movie.MovieShortResponseDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.security.CustomUserDetailsService;
import com.movienotebook.api.security.JwtService;
import com.movienotebook.api.service.CollectionService;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CollectionController.class)
@AutoConfigureJsonTesters
class CollectionControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private JacksonTester<CollectionRequestDto> collectionRequestJsonTester;
	
	@MockitoBean
	private JwtService jwtService;
	
	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;
	
	@MockitoBean
	private CollectionService collectionService;
	
	@Nested
	@DisplayName("POST /api/v1/collections - Создание коллекции")
	class CreateCollectionTests {
		
		@Test
		@DisplayName("Успешное создание коллекции (201)")
		void createCollection_ValidRequest_Returns201() throws Exception {
			// Arrange
			var request = new CollectionRequestDto("Любимая фантастика", "Лучшие фильмы жанра", true);
			var response = new CollectionResponseDto(
					1L, "Любимая фантастика", "Лучшие фильмы жанра", true,
					OffsetDateTime.parse("2026-09-01T12:00:00Z"),
					OffsetDateTime.parse("2026-09-01T12:00:00Z")
			);
			
			var userDetails = new CustomUserDetails(1L, "testuser", "pass", List.of());
			
			when(collectionService.create(any(CollectionRequestDto.class), any()))
					.thenReturn(response);
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/collections")
							.with(user(userDetails))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(collectionRequestJsonTester.write(request).getJson()))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.id").value(1L))
					.andExpect(jsonPath("$.name").value("Любимая фантастика"))
					.andExpect(jsonPath("$.isPublic").value(true));
			
			verify(collectionService).create(any(CollectionRequestDto.class), any());
		}
		
		@Test
		@DisplayName("Ошибка валидации пустого названия (400)")
		void createCollection_EmptyName_Returns400() throws Exception {
			// Arrange
			var invalidRequest = new CollectionRequestDto("", "Описание", false);
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/collections")
							.with(user("testuser"))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(collectionRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(collectionService);
		}
	}
	
	@Nested
	@DisplayName("GET /api/v1/collections - Мои коллекции")
	class GetMyCollectionsTests {
		
		@Test
		@DisplayName("Успешное получение списка своих коллекций (200)")
		void getMyCollections_Returns200() throws Exception {
			// Arrange
			var collectionDto = new CollectionResponseDto(
					1L, "Смотреть позже", null, false,
					OffsetDateTime.now(), OffsetDateTime.now()
			);
			
			when(collectionService.getByCurrentUser(any())).thenReturn(List.of(collectionDto));
			
			// Act & Assert
			mockMvc.perform(get("/api/v1/collections")
							.with(user("testuser")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$[0].id").value(1L))
					.andExpect(jsonPath("$[0].name").value("Смотреть позже"))
					.andExpect(jsonPath("$[0].isPublic").value(false));
			
			verify(collectionService).getByCurrentUser(any());
		}
	}
	
	@Nested
	@DisplayName("GET /api/v1/collections/{collectionId} - Детали коллекции")
	class GetCollectionTests {
		
		@Test
		@DisplayName("Успешное получение коллекции с фильмами (200)")
		void getCollection_ValidId_Returns200() throws Exception {
			// Arrange
			Long collectionId = 1L;
			var movieDto = new MovieShortResponseDto(
					1L, "Интерстеллар", 2014, "poster.jpg", BigDecimal.valueOf(8.65)
			);
			var collectionDto = new CollectionWithMoviesResponseDto(
					collectionId, "Космос", "Про космос", true, "testuser",
					List.of(movieDto), OffsetDateTime.now(), OffsetDateTime.now()
			);
			
			when(collectionService.getWithMoviesById(eq(collectionId), any())).thenReturn(collectionDto);
			
			// Act & Assert
			mockMvc.perform(get("/api/v1/collections/{collectionId}", collectionId)
							.with(user("testuser")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(collectionId))
					.andExpect(jsonPath("$.name").value("Космос"))
					.andExpect(jsonPath("$.movies[0].title").value("Интерстеллар"))
					.andExpect(jsonPath("$.authorUsername").value("testuser"));
			
			verify(collectionService).getWithMoviesById(eq(collectionId), any());
		}
	}
	
	@Nested
	@DisplayName("PUT /api/v1/collections/{collectionId} - Обновление коллекции")
	class UpdateCollectionTests {
		
		@Test
		@DisplayName("Успешное редактирование коллекции (200)")
		void updateCollection_ValidRequest_Returns200() throws Exception {
			// Arrange
			Long collectionId = 1L;
			var request = new CollectionRequestDto("Новое название", "Новое описание", true);
			var response = new CollectionResponseDto(
					collectionId, "Новое название", "Новое описание", true,
					OffsetDateTime.now(), OffsetDateTime.now()
			);
			
			when(collectionService.update(eq(collectionId), any(CollectionRequestDto.class), any()))
					.thenReturn(response);
			
			// Act & Assert
			mockMvc.perform(put("/api/v1/collections/{collectionId}", collectionId)
							.with(user("testuser"))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(collectionRequestJsonTester.write(request).getJson()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.name").value("Новое название"))
					.andExpect(jsonPath("$.isPublic").value(true));
			
			verify(collectionService).update(eq(collectionId), any(CollectionRequestDto.class), any());
		}
		
		@Test
		@DisplayName("Ошибка валидации при обновлении (400)")
		void updateCollection_EmptyName_Returns400() throws Exception {
			// Arrange
			Long collectionId = 1L;
			var invalidRequest = new CollectionRequestDto(" ", "Описание", true);
			
			// Act & Assert
			mockMvc.perform(put("/api/v1/collections/{collectionId}", collectionId)
							.with(user("testuser"))
							.with(csrf())
							.contentType(MediaType.APPLICATION_JSON)
							.content(collectionRequestJsonTester.write(invalidRequest).getJson()))
					.andExpect(status().isBadRequest());
			
			verifyNoInteractions(collectionService);
		}
	}
	
	@Nested
	@DisplayName("DELETE /api/v1/collections/{collectionId} - Удаление коллекции")
	class DeleteCollectionTests {
		
		@Test
		@DisplayName("Успешное удаление коллекции (204)")
		void deleteCollection_ValidId_Returns204() throws Exception {
			// Arrange
			Long collectionId = 1L;
			
			// Act & Assert
			mockMvc.perform(delete("/api/v1/collections/{collectionId}", collectionId)
							.with(user("testuser"))
							.with(csrf()))
					.andExpect(status().isNoContent()); // 204
			
			verify(collectionService).delete(eq(collectionId), any());
		}
	}
	
	@Nested
	@DisplayName("POST /api/v1/collections/{collectionId}/movies/{movieId} - Добавление фильма")
	class AddMovieToCollectionTests {
		
		@Test
		@DisplayName("Успешное добавление фильма в коллекцию (200)")
		void addMovieToCollection_ValidIds_Returns200() throws Exception {
			// Arrange
			Long collectionId = 1L;
			Long movieId = 42L;
			
			// Act & Assert
			mockMvc.perform(post("/api/v1/collections/{collectionId}/movies/{movieId}", collectionId, movieId)
							.with(user("testuser"))
							.with(csrf()))
					.andExpect(status().isOk()); // 200
			
			verify(collectionService).addMovie(eq(collectionId), eq(movieId), any());
		}
	}
	
	@Nested
	@DisplayName("DELETE /api/v1/collections/{collectionId}/movies/{movieId} - Удаление фильма")
	class RemoveMovieFromCollectionTests {
		
		@Test
		@DisplayName("Успешное удаление фильма из коллекции (204)")
		void removeMovieFromCollection_ValidIds_Returns204() throws Exception {
			// Arrange
			Long collectionId = 1L;
			Long movieId = 42L;
			
			// Act & Assert
			mockMvc.perform(delete("/api/v1/collections/{collectionId}/movies/{movieId}", collectionId, movieId)
							.with(user("testuser"))
							.with(csrf()))
					.andExpect(status().isNoContent()); // 204
			
			verify(collectionService).removeMovie(eq(collectionId), eq(movieId), any());
		}
	}
}
package com.movienotebook.api.integration;

import com.movienotebook.api.dto.collection.CollectionRequestDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.entity.enums.Role;
import com.movienotebook.api.repository.MovieRepository;
import com.movienotebook.api.repository.UserRepository;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class CollectionIntegrationTest extends BaseIntegrationTest {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private MovieRepository movieRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtService jwtService;
	
	private String validJwtToken;
	private Movie savedMovie;
	
	@BeforeEach
	void setUp() {
		User user = new User();
		user.setUsername("collector_user");
		user.setEmail("collector@mail.com");
		user.setPasswordHash(passwordEncoder.encode("password"));
		user.setRole(Role.ROLE_USER);
		User savedUser = userRepository.save(user);
		
		CustomUserDetails userDetails = new CustomUserDetails(
				savedUser.getId(),
				savedUser.getUsername(),
				savedUser.getPasswordHash(),
				List.of(new SimpleGrantedAuthority(savedUser.getRole().name()))
		);
		validJwtToken = "Bearer " + jwtService.generateToken(userDetails);
		
		Movie movie = new Movie();
		movie.setExternalId(999L);
		movie.setTitle("The Matrix Integration");
		savedMovie = movieRepository.save(movie);
	}
	
	@Test
	@DisplayName("Полный цикл: Создание коллекции, добавление фильма и получение данных")
	void shouldCreateCollectionAndAddMovieSuccessfully() throws Exception {
		CollectionRequestDto collectionRequest = new CollectionRequestDto(
				"Моя любимая фантастика",
				"Фильмы, которые стоит пересмотреть",
				false
		);
		
		String createResponse = mockMvc.perform(post("/api/v1/collections")
						.header(HttpHeaders.AUTHORIZATION, validJwtToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(collectionRequest)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.name").value("Моя любимая фантастика"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		
		Long collectionId = objectMapper.readTree(createResponse).get("id").asLong();
		
		mockMvc.perform(post("/api/v1/collections/{collectionId}/movies/{movieId}", collectionId, savedMovie.getId())
						.header(HttpHeaders.AUTHORIZATION, validJwtToken))
				.andExpect(status().isOk());
		
		mockMvc.perform(get("/api/v1/collections/{collectionId}", collectionId)
						.header(HttpHeaders.AUTHORIZATION, validJwtToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.movies").isArray())
				.andExpect(jsonPath("$.movies.length()").value(1))
				.andExpect(jsonPath("$.movies[0].title").value("The Matrix Integration"));
	}
	
	@Test
	@DisplayName("Ошибка при попытке добавить несуществующий фильм в коллекцию")
	void shouldFailToAddNonExistentMovieToCollection() throws Exception {
		CollectionRequestDto collectionRequest = new CollectionRequestDto(
				"Пустая коллекция",
				null,
				true
		);
		
		String createResponse = mockMvc.perform(post("/api/v1/collections")
						.header(HttpHeaders.AUTHORIZATION, validJwtToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(collectionRequest)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		
		Long collectionId = objectMapper.readTree(createResponse).get("id").asLong();
		Long nonExistentMovieId = 999999L;
		
		mockMvc.perform(post("/api/v1/collections/{collectionId}/movies/{movieId}", collectionId, nonExistentMovieId)
						.header(HttpHeaders.AUTHORIZATION, validJwtToken))
				.andExpect(status().isNotFound());
	}
}
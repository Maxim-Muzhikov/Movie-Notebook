package com.movienotebook.api.integration;

import com.movienotebook.api.dto.review.ReviewRequestDto;
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
class ReviewIntegrationTest extends BaseIntegrationTest {
	
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
	private User savedUser;
	
	@BeforeEach
	void setUp() {
		User user = new User();
		user.setUsername("reviewer");
		user.setEmail("reviewer@mail.com");
		user.setPasswordHash(passwordEncoder.encode("password"));
		user.setRole(Role.ROLE_USER);
		savedUser = userRepository.save(user);
		
		CustomUserDetails userDetails = new CustomUserDetails(
				savedUser.getId(),
				savedUser.getUsername(),
				savedUser.getPasswordHash(),
				List.of(new SimpleGrantedAuthority(savedUser.getRole().name()))
		);
		validJwtToken = "Bearer " + jwtService.generateToken(userDetails);
		
		Movie movie = new Movie();
		movie.setExternalId(12345L);
		movie.setTitle("Integration Test Movie");
		savedMovie = movieRepository.save(movie);
	}
	
	@Test
	@DisplayName("Создание отзыва и его успешное получение в списке")
	void shouldCreateAndRetrieveReview() throws Exception {
		ReviewRequestDto reviewRequest = new ReviewRequestDto("Этот фильм просто великолепен! Однозначно рекомендую.");
		
		mockMvc.perform(post("/api/v1/movies/{movieId}/reviews", savedMovie.getId())
						.header(HttpHeaders.AUTHORIZATION, validJwtToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reviewRequest)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.content").value(reviewRequest.content()))
				.andExpect(jsonPath("$.authorUsername").value(savedUser.getUsername()));
		
		mockMvc.perform(get("/api/v1/movies/{movieId}/reviews", savedMovie.getId())
						.header(HttpHeaders.AUTHORIZATION, validJwtToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].content").value(reviewRequest.content()));
	}
	
	@Test
	@DisplayName("Запрет создания отзыва без токена авторизации")
	void shouldFailCreateReviewWithoutAuth() throws Exception {
		ReviewRequestDto reviewRequest = new ReviewRequestDto("Отличный фильм!");
		
		mockMvc.perform(post("/api/v1/movies/{movieId}/reviews", savedMovie.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reviewRequest)))
				.andExpect(status().isForbidden());
	}
}
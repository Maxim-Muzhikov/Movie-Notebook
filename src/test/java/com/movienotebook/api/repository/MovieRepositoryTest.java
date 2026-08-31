package com.movienotebook.api.repository;

import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.Rating;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.entity.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MovieRepositoryTest {
	
	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
	
	@Autowired
	private MovieRepository movieRepository;
	
	@Autowired
	private TestEntityManager entityManager;
	
	@Nested
	@DisplayName("Тесты метода updateAverageRating")
	class UpdateAverageRatingTest {
		
		@Test
		@DisplayName("Должен корректно рассчитать и обновить средний рейтинг фильма с округлением до 2 знаков")
		void shouldUpdateAverageRatingCorrectly() {
			// Оценки: 4, 4, 5 -> Среднее: 13 / 3 = 4.3333... -> Округление: 4.33
			//Arrange
			User user1 = new User();
			user1.setUsername("user1");
			user1.setEmail("user1@matrix.com");
			user1.setPasswordHash("hash");
			user1.setRole(Role.ROLE_USER);
			entityManager.persist(user1);
			
			User user2 = new User();
			user2.setUsername("user2");
			user2.setEmail("user2@matrix.com");
			user2.setPasswordHash("hash");
			user2.setRole(Role.ROLE_USER);
			entityManager.persist(user2);
			
			User user3 = new User();
			user3.setUsername("user3");
			user3.setEmail("user3@matrix.com");
			user3.setPasswordHash("hash");
			user3.setRole(Role.ROLE_USER);
			entityManager.persist(user3);
			
			Movie movie = new Movie();
			movie.setTitle("Inception");
			movie.setExternalId(100L);
			entityManager.persist(movie);
			
			Rating rating1 = new Rating();
			rating1.setMovie(movie);
			rating1.setUser(user1);
			rating1.setScore(4);
			
			Rating rating2 = new Rating();
			rating2.setMovie(movie);
			rating2.setUser(user2);
			rating2.setScore(4);
			
			Rating rating3 = new Rating();
			rating3.setMovie(movie);
			rating3.setUser(user3);
			rating3.setScore(5);
			
			entityManager.persist(rating1);
			entityManager.persist(rating2);
			entityManager.persist(rating3);
			
			entityManager.flush();
			entityManager.clear();
			
			// Act
			movieRepository.updateAverageRating(movie.getId());
			
			entityManager.clear();
			
			// Assert
			Optional<BigDecimal> averageRating = movieRepository.findAverageRatingById(movie.getId());
			
			assertThat(averageRating).isPresent();
			assertThat(averageRating.get()).isEqualByComparingTo("4.33");
		}
		
		@Test
		@DisplayName("Должен установить рейтинг в null, если у фильма нет оценок")
		void shouldSetNullRatingWhenNoRatingsExist() {
			// Arrange
			Movie movie = new Movie();
			movie.setTitle("New Movie");
			movie.setExternalId(100L);
			movie.setAverageRating(new BigDecimal("5.00"));
			entityManager.persist(movie);
			
			entityManager.flush();
			entityManager.clear();
			
			// Act
			movieRepository.updateAverageRating(movie.getId());
			entityManager.clear();
			
			// Assert
			Movie updatedMovie = entityManager.find(Movie.class, movie.getId());
			assertThat(updatedMovie.getAverageRating()).isNull();
		}
		
		@Test
		@DisplayName("Должен обновить рейтинг только указанного фильма, не затрагивая другие")
		void shouldUpdateRatingOnlyForSpecifiedMovie() {
			// Arrange
			User user1 = new User();
			user1.setUsername("user1");
			user1.setEmail("user1@matrix.com");
			user1.setPasswordHash("hash");
			user1.setRole(Role.ROLE_USER);
			entityManager.persist(user1);
			
			User user2 = new User();
			user2.setUsername("user2");
			user2.setEmail("user2@matrix.com");
			user2.setPasswordHash("hash");
			user2.setRole(Role.ROLE_USER);
			entityManager.persist(user2);
			
			Movie movie1 = new Movie();
			movie1.setTitle("Movie 1");
			movie1.setExternalId(100L);
			entityManager.persist(movie1);
			
			Movie movie2 = new Movie();
			movie2.setTitle("Movie 2");
			movie2.setExternalId(200L);
			movie2.setAverageRating(new BigDecimal("2.00")); // Изначальный рейтинг
			entityManager.persist(movie2);
			
			Rating rating1 = new Rating();
			rating1.setMovie(movie1);
			rating1.setUser(user1);
			rating1.setScore(5);
			
			Rating rating2 = new Rating();
			rating2.setMovie(movie2);
			rating2.setUser(user2);
			rating2.setScore(1);
			
			entityManager.persist(rating1);
			entityManager.persist(rating2);
			
			entityManager.flush();
			entityManager.clear();
			
			// Act
			movieRepository.updateAverageRating(movie1.getId());
			entityManager.clear();
			
			// Assert
			Optional<BigDecimal> movie1Rating = movieRepository.findAverageRatingById(movie1.getId());
			Optional<BigDecimal> movie2Rating = movieRepository.findAverageRatingById(movie2.getId());
			
			assertThat(movie1Rating).hasValueSatisfying(r -> assertThat(r).isEqualByComparingTo("5.00"));
			assertThat(movie2Rating).hasValueSatisfying(r -> assertThat(r).isEqualByComparingTo("2.00"));
		}
		
	}
	
	@Nested
	@DisplayName("Тесты метода findAverageRatingById")
	class FindAverageRatingByIdTest {
		
		@Test
		@DisplayName("findAverageRatingById должен вернуть Optional.empty() для несуществующего фильма")
		void shouldReturnEmptyOptionalForNonExistingMovie() {
			// Arrange
			Long NotExistingMovieId = 999L;
			
			// Act
			Optional<BigDecimal> result = movieRepository.findAverageRatingById(NotExistingMovieId);
			
			// Assert
			assertThat(result).isEmpty();
		}
		
	}
}


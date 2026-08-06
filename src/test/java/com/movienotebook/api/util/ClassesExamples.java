package com.movienotebook.api.util;

import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.Role;
import com.movienotebook.api.entity.User;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;

@UtilityClass
public class ClassesExamples {
	
	private static final OffsetDateTime currentTime = OffsetDateTime.now();
	
	public Movie getExistingMovie() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setExternalId(10L);
		movie.setTitle("Название фильма");
		movie.setOriginalTitle("Оригинальное название фильма");
		movie.setDescription("Описание фильма");
		movie.setAverageRating(BigDecimal.valueOf(10));
		movie.setReleaseYear(1995);
		movie.setPosterUrl("Ссылка на постер");
		movie.setReviews(new ArrayList<>());
		movie.setRatings(new ArrayList<>());
		movie.setLastUpdate(currentTime.minusDays(1));
		movie.setCreatedAt(currentTime.minusDays(2));
		return movie;
	}
	
	public User getExistingUser() {
		User user = new User();
		user.setId(1L);
		user.setUsername("Имя пользователя");
		user.setPasswordHash("Хэш пароля");
		user.setRole(Role.ROLE_USER);
		user.setEmail("email@mail.ru");
		user.setRatings(new ArrayList<>());
		user.setReports(new ArrayList<>());
		user.setCollections(new ArrayList<>());
		user.setReviews(new ArrayList<>());
		return user;
	}
	
	public User getUserToSave() {
		User user = new User();
		user.setUsername("Имя пользователя");
		user.setPasswordHash("Хэш пароля");
		user.setRole(Role.ROLE_USER);
		user.setEmail("email@mail.ru");
		return user;
	}
	
	public Review getExistingReview() {
		Review review = new Review();
		review.setId(1L);
		review.setContent("Содержание отзыва о фильме");
		review.setMovie(new Movie());
		review.setUser(new User());
		review.setIsDeleted(false);
		review.setUpdatedAt(currentTime.minusDays(1));
		review.setCreatedAt(currentTime.minusDays(2));
		return review;
	}
	
}

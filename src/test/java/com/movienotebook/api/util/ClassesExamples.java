package com.movienotebook.api.util;

import com.movienotebook.api.entity.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;

@UtilityClass
public class ClassesExamples {
	
	private static final OffsetDateTime currentTime = OffsetDateTime.now();
	
	public Report getExistingReport() {
		Report report = new Report();
		report.setId(1L);
		report.setReason("Жалоба на плохой отзыв");
		report.setStatus("NEW");
		report.setReview(new Review());
		report.setReporter(new User());
		report.setCreatedAt(currentTime.minusDays(1));
		return report;
	}
	
	public Report getReportToSave() {
		Report report = new Report();
		report.setReason("Жалоба на плохой отзыв");
		report.setStatus("NEW");
		return report;
	}
	
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
	
	public Movie getMovieToSave() {
		Movie movie = new Movie();
		movie.setExternalId(10L);
		movie.setTitle("Название фильма");
		movie.setOriginalTitle("Оригинальное название фильма");
		movie.setDescription("Описание фильма");
		movie.setReleaseYear(1995);
		movie.setPosterUrl("Ссылка на постер");
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
	
	public Rating getExistingRating() {
		Rating rating = new Rating();
		rating.setId(1L);
		rating.setScore(10);
		rating.setMovie(new Movie());
		rating.setUser(new User());
		rating.setCreatedAt(currentTime);
		return rating;
	}
	
	public Rating getRatingToSave() {
		Rating rating = new Rating();
		rating.setScore(10);
		rating.setMovie(new Movie());
		rating.setUser(new User());
		return rating;
	}
}

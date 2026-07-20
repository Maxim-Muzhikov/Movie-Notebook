package com.movienotebook.api.service;

import com.movienotebook.api.dto.review.ReviewRequestDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.Role;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.repository.ReviewRepository;
import com.movienotebook.api.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
	
	private final ReviewRepository reviewRepository;
	private final MovieService movieService;
	private final UserService userService;
	
	// TODO Проверить нет ли ошибок при добавлении отзыва, если один отзыв от пользователя уже был удален
	@Transactional
	public Review addOrUpdateReview(ReviewRequestDto request, CustomUserDetails currentUser) {
		
		Movie movie = movieService.getById(request.movieId());
		
		Optional<Review> existingReview = reviewRepository.findByMovieIdAndUserId(movie.getId(), currentUser.getId());
		
		if (existingReview.isPresent()) {
			existingReview.get().setContent(request.content());
			reviewRepository.save(existingReview.get());
			return existingReview.get();
		} else {
			User user = userService.getReferenceById(currentUser.getId());
			Review newReview = new Review();
			newReview.setMovie(movie);
			newReview.setUser(user);
			newReview.setContent(request.content());
			reviewRepository.save(newReview);
			return newReview;
		}
	}
	
	public List<Review> getReviewsByMovieId(Long id) {
		return reviewRepository.findAllByMovieId(id);
	}
	
	@Transactional
	public void deleteReview(Long id, CustomUserDetails currentUser) {
	
		Review review = reviewRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Отзыв с идентификатором " + id + "не найден"));
		
		User author = review.getUser();
		
		// TODO Добавить в CustomUserDetails поле с ролью
		User user = userService.getById(currentUser.getId());
		
		if (author.getId().equals(currentUser.getId()) || user.getRole() == Role.ROLE_ADMIN) {
			reviewRepository.delete(review);
		} else {
			throw new org.springframework.security.access.AccessDeniedException("У вас нет прав на удаление этого отзыва");
		}
	
	}
	
	public Review getReviewById(Long id) {
		return reviewRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Отзыв с идентификатором " + id + " не найден"));
	}
}

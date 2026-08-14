package com.movienotebook.api.service;

import com.movienotebook.api.dto.review.ReviewRequestDto;
import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.Role;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.mapper.ReviewMapper;
import com.movienotebook.api.repository.ReportRepository;
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
	private final ReviewMapper reviewMapper;
	private final ReportRepository reportRepository;
	
	// TODO Проверить нет ли ошибок при добавлении отзыва, если один отзыв от пользователя уже был удален
	// TODO Реализовать, что невозможно восстановить удаленный отзыв без удаления новосозданного или невозможно восстановить удаленный отзыв
	@Transactional
	public ReviewResponseDto save(ReviewRequestDto request, CustomUserDetails currentUser) {
		
		Movie movie = movieService.getEntityById(request.movieId());
		
		Optional<Review> existingReview = reviewRepository.findByMovieIdAndUserId(movie.getId(), currentUser.getId());
		
		if (existingReview.isPresent()) {
			existingReview.get().setContent(request.content());
			reviewRepository.save(existingReview.get());
			return reviewMapper.toDto(existingReview.get());
		} else {
			User user = userService.getReferenceById(currentUser.getId());
			Review newReview = new Review();
			newReview.setMovie(movie);
			newReview.setUser(user);
			newReview.setContent(request.content());
			reviewRepository.save(newReview);
			return reviewMapper.toDto(newReview);
		}
	}
	
	@Transactional
	public void delete(Long id, CustomUserDetails currentUser) {
		
		Review review = reviewRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Отзыв с идентификатором " + id + "не найден"));
		
		User author = review.getUser();
		
		// TODO Добавить в CustomUserDetails поле с ролью
		User user = userService.getEntityById(currentUser.getId());
		
		if (author.getId().equals(currentUser.getId()) || user.getRole() == Role.ROLE_ADMIN) {
			reviewRepository.delete(review);
		} else {
			throw new org.springframework.security.access.AccessDeniedException("У вас нет прав на удаление этого отзыва");
		}
	}
	
	public List<ReviewResponseDto> findAllByMovieId(Long id) {
		return reviewRepository.findAllByMovieId(id).stream().map(reviewMapper::toDto).toList();
	}
	
	public ReviewResponseDto getById(Long id) {
		return reviewMapper.toDto(reviewRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Отзыв с идентификатором " + id + " не найден")));
	}
	
	List<Review> findAllEntityByMovieId(Long id) {
		return reviewRepository.findAllByMovieId(id);
	}
	
	Review getEntityById(Long id) {
		return reviewRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Отзыв с идентификатором " + id + " не найден"));
	}
}

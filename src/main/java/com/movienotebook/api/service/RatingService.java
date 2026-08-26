package com.movienotebook.api.service;

import com.movienotebook.api.dto.rating.RatingRequestDto;
import com.movienotebook.api.dto.rating.RatingResponseDto;
import com.movienotebook.api.entity.Rating;
import com.movienotebook.api.repository.RatingRepository;
import com.movienotebook.api.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingService {
	
	private final RatingRepository ratingRepository;
	private final MovieService movieService;
	private final UserService userService;
	
	@Transactional
	public RatingResponseDto save(Long movieId, RatingRequestDto request, CustomUserDetails currentUser) {
		Rating rating = ratingRepository.findByMovieIdAndUserId(movieId, currentUser.getId())
				.orElseGet(() -> {
					Rating r = new Rating();
					r.setMovie(movieService.getReferenceById(movieId));
					r.setUser(userService.getReferenceById(currentUser.getId()));
					return r;
				});
		
		rating.setScore(request.score());
		ratingRepository.saveAndFlush(rating);
		
		BigDecimal newAverage = movieService.updateAndGetAverageRating(movieId);
		
		return new RatingResponseDto(newAverage);
	}
	
}

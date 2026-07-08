package com.movienotebook.api.service;

import com.movienotebook.api.dto.rating.RatingRequestDto;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.Rating;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingService {
	
	private final RatingRepository ratingRepository;
	private final MovieService movieService;
	
	// TODO: Внедрить инкрементальное среднее
	@Transactional
	public BigDecimal addOrUpdateRating(RatingRequestDto request, User currentUser) {
		
		Movie movie = movieService.getById(request.movieId());
		
		Optional<Rating> existingRating = ratingRepository.findByMovieIdAndUserId(movie.getId(), currentUser.getId());
		
		if (existingRating.isPresent()) {
			existingRating.get().setScore(request.score());
			ratingRepository.save(existingRating.get());
		} else {
			Rating newRating = new Rating();
			newRating.setMovie(movie);
			newRating.setUser(currentUser);
			newRating.setScore(request.score());
			ratingRepository.save(newRating);
		}
		
		Double rawAverage = ratingRepository.calculateAverageScoreByMovieId(movie.getId());
		
		BigDecimal newAverage = BigDecimal.valueOf(rawAverage).setScale(2, RoundingMode.HALF_UP);
		
		movie.setAverageRating(newAverage);
		movieService.save(movie);
		
		return newAverage;
	}
	
}

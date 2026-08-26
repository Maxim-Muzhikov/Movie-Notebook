package com.movienotebook.api.repository;

import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.Rating;
import com.movienotebook.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
	
	List<Rating> findAllByUser(User user);
	
	List<Rating> findAllByUserId(Long userId);
	
	List<Rating> findAllByMovie(Movie movie);
	
	List<Rating> findAllByMovieId(Long movieId);
	
	Optional<Rating> findByMovieIdAndUserId(Long movieId, Long userId);
	
}

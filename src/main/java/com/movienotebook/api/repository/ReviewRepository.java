package com.movienotebook.api.repository;

import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.Rating;
import com.movienotebook.api.entity.Review;
import com.movienotebook.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	
	List<Review> findAllByMovie(Movie movie);
	
	List<Review> findAllByMovieId(Long movieId);
	
	List<Review> findAllByUser(User user);
	
	List<Review> findAllByUserId(Long userId);
	
	Optional<Review> findByMovieIdAndUserId(Long movieId, Long userId);
	
}

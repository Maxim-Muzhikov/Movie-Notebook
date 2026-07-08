package com.movienotebook.api.repository;

import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.Rating;
import com.movienotebook.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
	
	List<Rating> findAllByUser(User user);
	
	List<Rating> findAllByUserId(Long userId);
	
	List<Rating> findAllByMovie(Movie movie);
	
	List<Rating> findAllByMovieId(Long movieId);
	
	@Query("SELECT AVG(r.score) FROM Rating r WHERE r.movie.id = :movieId")
	Double calculateAverageScoreByMovieId(@Param("movieId") Long movieId);
	
	Optional<Rating> findByMovieIdAndUserId(Long movieId, Long userId);
	
}

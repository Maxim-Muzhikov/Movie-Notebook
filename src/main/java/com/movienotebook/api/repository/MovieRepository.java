package com.movienotebook.api.repository;

import com.movienotebook.api.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
	
	Page<Movie> findAllByTitleContainingIgnoreCaseOrOriginalTitleContainingIgnoreCase(String title, String originalTitle, Pageable pageable);
	
	List<Movie> findAllByTitleContainingIgnoreCase(String title);
	
	List<Movie> findAllByOriginalTitle(String title);
	
	List<Movie> findAllByExternalIdIn(List<Long> externalIds);
	
	Optional<Movie> findByExternalId(Long externalId);
	
	@Modifying
	@Query("UPDATE Movie m SET m.averageRating = " +
			"(SELECT ROUND(AVG(r.score), 2) FROM Rating r WHERE r.movie.id = :movieId) " +
			"WHERE m.id = :movieId")
	void updateAverageRating(@Param("movieId") Long movieId);
	
	@Query("SELECT m.averageRating FROM Movie m WHERE m.id = :movieId")
	Optional<BigDecimal> findAverageRatingById(@Param("movieId") Long movieId);
}

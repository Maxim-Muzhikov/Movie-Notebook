package com.movienotebook.api.repository;

import com.movienotebook.api.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
	
	Page<Movie> findAllByTitleContainingIgnoreCaseOrOriginalTitleContainingIgnoreCase(String title, String originalTitle, Pageable pageable);
	
	List<Movie> findAllByTitleContainingIgnoreCase(String title);
	
	List<Movie> findAllByOriginalTitle(String title);
	
	List<Movie> findAllByExternalIdIn(List<Long> externalIds);
	
	Optional<Movie> findByExternalId(Long externalId);
	
}

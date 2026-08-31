package com.movienotebook.api.repository;

import com.movienotebook.api.entity.Collection;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@NullMarked
public interface CollectionRepository extends JpaRepository<Collection, Long> {
	
	List<Collection> findAllByUserId(Long userId);
	
	@EntityGraph(attributePaths = {"collectionMovies", "collectionMovies.movie"})
	Optional<Collection> findById(Long id);
	
	@EntityGraph(attributePaths = {"collectionMovies", "collectionMovies.movie"})
	Optional<Collection> findByNameAndUserId(String name, Long userId);
	
	boolean existsByNameAndUserId(String name, Long userId);
}

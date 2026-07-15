package com.movienotebook.api.repository;

import com.movienotebook.api.entity.Collection;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// NOTE Аннотация @NullMarked указывается явно из-за переопределения метода findById
@NullMarked
public interface CollectionRepository extends JpaRepository<Collection, Long> {
	
	List<Collection> findAllByUserId(Long userId);
	
	// NOTE Используем EntityGraph для решения проблемы N+1
	// LEARN @EntityGraph
	@EntityGraph(attributePaths = {"collectionMovies", "collectionMovies.movie"})
	Optional<Collection> findById(Long id);
}

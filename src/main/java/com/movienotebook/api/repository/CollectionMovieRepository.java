package com.movienotebook.api.repository;

import com.movienotebook.api.entity.Collection;
import com.movienotebook.api.entity.CollectionMovie;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollectionMovieRepository extends JpaRepository<CollectionMovie, Long> {
	
	boolean existsByCollectionIdAndMovieId(Long collectionId, Long movieId);
	
	Optional<CollectionMovie> findByCollectionIdAndMovieId(Long collectionId, Long movieId);
}

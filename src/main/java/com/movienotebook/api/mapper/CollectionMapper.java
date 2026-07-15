package com.movienotebook.api.mapper;

import com.movienotebook.api.dto.collection.CollectionResponseDto;
import com.movienotebook.api.dto.movie.MovieResponseDto;
import com.movienotebook.api.entity.Collection;
import com.movienotebook.api.entity.CollectionMovie;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class CollectionMapper {
	
	protected final MovieMapper movieMapper;
	
	protected CollectionMapper(MovieMapper movieMapper) {
		this.movieMapper = movieMapper;
	}
	
	@Mapping(target = "movies", source = "collectionMovies", qualifiedByName = "mapCollectionMovies")
	public abstract CollectionResponseDto toDto(Collection collection);
	
	protected List<MovieResponseDto> mapCollectionMovies(Set<CollectionMovie> collectionMovies) {
		if (collectionMovies == null) {
			return List.of();
		}
		return collectionMovies.stream()
				.map(cm -> movieMapper.toDto(cm.getMovie()))
				.toList();
	}
}
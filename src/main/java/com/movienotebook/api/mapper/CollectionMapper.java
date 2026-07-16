package com.movienotebook.api.mapper;

import com.movienotebook.api.dto.collection.CollectionResponseDto;
import com.movienotebook.api.dto.collection.CollectionWithMoviesResponseDto;
import com.movienotebook.api.dto.movie.MovieShortResponseDto;
import com.movienotebook.api.entity.Collection;
import com.movienotebook.api.entity.CollectionMovie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// LEARN @Mapper(..., uses = {...})
@Mapper(componentModel = "spring", uses = {MovieMapper.class})
public interface CollectionMapper {
	// NOTE Этот маппинг нужен из-за особенностей совместимости Lombok и MapStruct
	@Mapping(target = "isPublic", source = "public")
	CollectionResponseDto toDto(Collection collection);
	
	@Mapping(target = "authorUsername", source = "user.username")
	@Mapping(target = "movies", source = "collectionMovies")
	@Mapping(target = "isPublic", source = "public")
	CollectionWithMoviesResponseDto toWithMoviesDto(Collection collection);
	
	// NOTE Вот что происходит в toMovieShortDto:
	// MapStruct видит, что источник - это CollectionMovie, а цель - MovieShortResponseDto.
	// Аннотация target = "." указывает: "возьми объект movie внутри CollectionMovie и замапь его свойства".
	// А так как в uses указан MovieMapper, MapStruct сам сгенерирует вызов movieMapper.toShortDto()!
	@Mapping(target = ".", source = "movie")
	MovieShortResponseDto toMovieShortDto(CollectionMovie collectionMovie);
}
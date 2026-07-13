package com.movienotebook.api.mapper;

import com.movienotebook.api.dto.movie.MovieResponseDto;
import com.movienotebook.api.dto.movie.MovieShortResponseDto;
import com.movienotebook.api.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovieMapper {
	
	MovieResponseDto toDto(Movie movie);
	
	MovieShortResponseDto toShortDto(Movie movie);
	
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "reviews", ignore = true)
	@Mapping(target = "ratings", ignore = true)
	@Mapping(target = "averageRating", ignore = true)
	void updateMovieFromExtracted(@MappingTarget Movie target, Movie source);
}
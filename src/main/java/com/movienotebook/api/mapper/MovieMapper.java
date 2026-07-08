package com.movienotebook.api.mapper;

import com.movienotebook.api.dto.movie.MovieResponseDto;
import com.movienotebook.api.dto.movie.MovieShortResponseDto;
import com.movienotebook.api.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovieMapper {
	
	MovieResponseDto toDto(Movie movie);
	
	MovieShortResponseDto toShortDto(Movie movie);
}
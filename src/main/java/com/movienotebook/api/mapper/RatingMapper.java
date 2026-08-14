package com.movienotebook.api.mapper;

import com.movienotebook.api.dto.rating.RatingResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.math.BigDecimal;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RatingMapper {
	
	@Mapping(source = "rating", target = "newAverageRating")
	RatingResponseDto toDto(BigDecimal rating);
}

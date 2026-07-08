package com.movienotebook.api.mapper;

import com.movienotebook.api.dto.rating.RatingRequestDto;
import com.movienotebook.api.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RatingMapper {
	
}

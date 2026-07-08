package com.movienotebook.api.mapper;

import com.movienotebook.api.dto.review.ReviewResponseDto;
import com.movienotebook.api.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewMapper {
	
	@Mapping(source = "user.username", target = "authorUsername")
	ReviewResponseDto toDto(Review review);
}
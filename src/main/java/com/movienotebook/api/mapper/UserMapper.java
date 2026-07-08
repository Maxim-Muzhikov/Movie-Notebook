package com.movienotebook.api.mapper;

import com.movienotebook.api.dto.user.UserResponseDto;
import com.movienotebook.api.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
	
	UserResponseDto toDto(User user);
}
package com.movienotebook.api.mapper;

import com.movienotebook.api.dto.report.ReportResponseDto;
import com.movienotebook.api.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReportMapper {
	
	@Mapping(source = "review.content", target = "reviewContent")

	@Mapping(source = "reporter.username", target = "reporterUsername")
	ReportResponseDto toDto(Report report);
}
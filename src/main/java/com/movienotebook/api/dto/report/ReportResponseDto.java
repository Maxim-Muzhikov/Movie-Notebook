package com.movienotebook.api.dto.report;

public record ReportResponseDto(
	Long id,
	String reason,
	String reviewContent,
	String reporterUsername,
	String status
) {
}

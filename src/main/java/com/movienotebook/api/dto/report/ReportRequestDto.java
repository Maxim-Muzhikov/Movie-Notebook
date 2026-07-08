package com.movienotebook.api.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportRequestDto(
		@NotNull(message = "ID отзыва не может быть пустым")
		Long reviewId,
		
		@NotBlank(message = "Причина не может быть пуста")
		String reason
) {
}
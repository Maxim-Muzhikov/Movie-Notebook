package com.movienotebook.api.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResolveReportRequestDto(
		@NotBlank(message = "Действие не может быть пустым")
		@Pattern(regexp = "^(DELETE_REVIEW|REJECT_REPORT)$", message = "Недопустимое действие. Разрешено: DELETE_REVIEW, REJECT_REPORT")
		String action
) {
}

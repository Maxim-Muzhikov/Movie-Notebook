package com.movienotebook.api.dto.report;

import com.movienotebook.api.entity.enums.ReportAction;
import jakarta.validation.constraints.NotBlank;

public record ResolveReportRequestDto(
		@NotBlank(message = "Действие не может быть пустым")
		ReportAction action
) {
}

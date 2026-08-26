package com.movienotebook.api.dto.report;

import com.movienotebook.api.entity.enums.ReportAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Решение модератора/администратора по жалобе")
public record ResolveReportRequestDto(
		@Schema(description = "Действие, применяемое к жалобе и отзыву", example = "DELETE_REVIEW", allowableValues = {"DELETE_REVIEW", "REJECT_REPORT", "CLAIM_REPORT"})
		@NotNull(message = "Действие не может быть пустым")
		ReportAction action
) {
}
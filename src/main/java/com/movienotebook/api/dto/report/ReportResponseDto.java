package com.movienotebook.api.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о жалобе на отзыв для модерации")
public record ReportResponseDto(
		@Schema(description = "Уникальный идентификатор жалобы", example = "1")
		Long id,
		
		@Schema(description = "Причина подачи жалобы", example = "Оскорбления, ненормативная лексика и спам в тексте отзыва")
		String reason,
		
		@Schema(description = "Содержимое отзыва, на который поступила жалоба", example = "Текст нерелевантного отзыва...")
		String reviewContent,
		
		@Schema(description = "Имя пользователя, подавшего жалобу", example = "jane_doe")
		String reporterUsername,
		
		@Schema(description = "Статус рассмотрения жалобы", example = "NEW", allowableValues = {"NEW", "IN_PROGRESS", "RESOLVED", "REJECTED"})
		String status
) {
}
package com.movienotebook.api.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос на отправку жалобы на отзыв")
public record ReportRequestDto(
		@Schema(description = "Идентификатор отзыва, на который подается жалоба", example = "1")
		@NotNull(message = "ID отзыва не может быть пустым")
		Long reviewId,
		
		@Schema(description = "Причина подачи жалобы", example = "Оскорбления, ненормативная лексика и спам в тексте отзыва")
		@NotBlank(message = "Причина не может быть пуста")
		String reason
) {
}
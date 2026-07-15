package com.movienotebook.api.dto.collection;

import javax.swing.text.StyledEditorKit;
import java.time.OffsetDateTime;

public record CollectionResponse(
		Long id,
		String name,
		String description,
		Boolean isPublic,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}

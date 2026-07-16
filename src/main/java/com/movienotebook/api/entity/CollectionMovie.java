package com.movienotebook.api.entity;

import com.movienotebook.api.entity.embeddedIds.CollectionMovieId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;


@Entity
@Table(name = "collection_movies")
@Getter
@Setter
@NoArgsConstructor
public class CollectionMovie {
	
	// NOTE Внедряем EmbeddedId для композитного ключа, так как JPA требует уникального идентификатора для каждой Entity.
	// NOTE Необязательно для малонагруженных сетей. Можно заменить суррогатным ключом
	@EmbeddedId
	private CollectionMovieId id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("collectionId")
	@JoinColumn(name = "collection_id", nullable = false)
	private Collection collection;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("movieId")
	@JoinColumn(name = "movie_id", nullable = false)
	private Movie movie;
	
	@CreationTimestamp
	@Column(name = "added_at", updatable = false)
	private OffsetDateTime addedAt;
}


package com.movienotebook.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "collections")
@Getter
@Setter
@NoArgsConstructor
public class Collection extends AbstractBase {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@Column(name ="name", nullable = false, length = 100)
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "is_public", nullable = false)
	private boolean isPublic;
	
	// NOTE Используем Set, а не List, чтобы Hibernate не генерировал лишние запросы (Cartesian product)
	// LEARN Cartesian product
	@OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<CollectionMovie> collectionMovies = new HashSet<>();
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private OffsetDateTime updatedAt;
	
	// NOTE Utility-методы обязательны для двунаправленных связей
	// LEARN Контекст персистентности Hibernate (L1 кэш)
	public void addMovie(CollectionMovie collectionMovie) {
		collectionMovies.add(collectionMovie);
		collectionMovie.setCollection(this);
	}
	
	public void removeMovie(CollectionMovie collectionMovie) {
		collectionMovies.remove(collectionMovie);
		collectionMovie.setCollection(null);
	}
	
	
}
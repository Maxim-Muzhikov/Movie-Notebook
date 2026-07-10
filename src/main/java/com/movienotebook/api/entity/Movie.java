package com.movienotebook.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
public class Movie extends AbstractBase {
	
	@Column(name = "external_id")
	private Long externalId;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "original_title")
	private String originalTitle;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "release_year")
	private Integer releaseYear;
	
	@Column(name = "poster_url")
	private String posterUrl;
	
	@Column(name = "average_rating")
	private BigDecimal averageRating;
	
	// Здесь -> @UpdateTimestamp
	@Column(name = "last_update")
	private OffsetDateTime lastUpdate;
	
	@OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Review> reviews = new ArrayList<>();
	
	@OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Rating> ratings = new ArrayList<>();
}
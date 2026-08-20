package com.movienotebook.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report extends AbstractBase {
	
	@Column(name = "reason")
	private String reason;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private ReportStatus status = ReportStatus.NEW;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "review_id")
	private Review review;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reporter_id")
	private User reporter;
}
package com.movienotebook.api.controller;

import com.movienotebook.api.dto.report.ReportRequestDto;
import com.movienotebook.api.dto.report.ReportResponseDto;
import com.movienotebook.api.dto.report.ResolveReportRequestDto;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
	
	private final ReportService reportService;
	
	@PostMapping()
	public ResponseEntity<Void> reporting(
			@Valid @RequestBody ReportRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		reportService.save(request, userDetails);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	@GetMapping
	public ResponseEntity<List<ReportResponseDto>> getAllReports() {
		
		List<ReportResponseDto> response = reportService.getAll();
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/{id}/resolve")
	public ResponseEntity<Void> resolveReport(
			@PathVariable Long id,
			@Valid @RequestBody ResolveReportRequestDto request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		reportService.resolve(id, request.action(), userDetails);
		return ResponseEntity.ok().build();
	}
}

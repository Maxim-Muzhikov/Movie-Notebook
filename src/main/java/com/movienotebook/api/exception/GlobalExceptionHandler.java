package com.movienotebook.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }
  
  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<Object> handleConflict(UserAlreadyExistsException ex) {
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
  }
  
  private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(Map.of(
            "timestamp", OffsetDateTime.now(),
            "status", status.value(),
            "error", status.getReasonPhrase(),
            "message", message
    ));
  }
}
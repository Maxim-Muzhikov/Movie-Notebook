package com.movienotebook.api.exception;

public class MovieAlreadyInCollectionException extends RuntimeException {
  public MovieAlreadyInCollectionException(String message) {
    super(message);
  }
}
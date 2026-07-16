package com.movienotebook.api.exception;

public class CollectionAlreadyExistsException extends RuntimeException {
  public CollectionAlreadyExistsException(String message) {
    super(message);
  }
}
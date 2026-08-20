package com.movienotebook.api.exception;

public class NoSuchReportActionAvailableException extends RuntimeException {
	public NoSuchReportActionAvailableException(String message) {
		super(message);
	}
}

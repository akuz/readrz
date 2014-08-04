package com.readrz.feedsget;

public final class AcceptableException extends Exception {

	private static final long serialVersionUID = 1L;

	public AcceptableException(String message) {
		super(message);
	}

	public AcceptableException(String message, Throwable cause) {
		super(message, cause);
	}
}

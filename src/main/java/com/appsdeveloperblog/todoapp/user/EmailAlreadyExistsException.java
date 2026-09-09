package com.appsdeveloperblog.todoapp.user;

public class EmailAlreadyExistsException extends RuntimeException {

	public EmailAlreadyExistsException(String email) {
		super("An account with email address '" + email + "' already exists");
	}

}

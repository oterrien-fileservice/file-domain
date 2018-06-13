package com.ote.file.api.exception;

public class UserNotFoundException extends Exception {

    private static final String MessageTemplate = "User '%s' is not found";

    public UserNotFoundException(String user) {
        super(String.format(MessageTemplate, user));
    }
}

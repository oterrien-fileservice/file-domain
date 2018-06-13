package com.ote.file.api.exception;

public class UnauthorizedException extends Exception {

    private static final String MessageTemplate = "User '%s' is not authorized to %s files on application '%s' and perimeter '%s'";

    public UnauthorizedException(String user, String application, String perimeter, String action) {
        super(String.format(MessageTemplate, user, action, application, perimeter));
    }
}

package com.ote.file.api.exception;

public class ApplicationNotFoundException extends Exception {

    private static final String MessageTemplate = "Application '%s' is not found";

    public ApplicationNotFoundException(String application) {
        super(String.format(MessageTemplate, application));
    }
}

package com.ote.file.api.exception;

public class PerimeterNotFoundException extends Exception {

    private static final String MessageTemplate = "Perimeter '%s' is not found for application '%s'";

    public PerimeterNotFoundException(String application, String perimeter) {
        super(String.format(MessageTemplate, perimeter, application));
    }
}

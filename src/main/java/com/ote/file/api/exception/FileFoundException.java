package com.ote.file.api.exception;

import com.ote.file.api.model.File;

public class FileFoundException extends Exception {

    private static final String MessageTemplate = "File '%s' should not be found for application '%s' and perimeter '%s'";

    public FileFoundException(String application, String perimeter, File file) {
        super(String.format(MessageTemplate, file.getPath(), application, perimeter));
    }
}


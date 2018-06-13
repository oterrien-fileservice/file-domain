package com.ote.file.api.exception;

import com.ote.file.api.model.File;

public class FileNotFoundException extends Exception {

    private static final String MessageTemplate = "File '%s' is not found for application '%s' and perimeter '%s'";

    public FileNotFoundException(String application, String perimeter, File file) {
        super(String.format(MessageTemplate, file.getPath(), application, perimeter));
    }
}


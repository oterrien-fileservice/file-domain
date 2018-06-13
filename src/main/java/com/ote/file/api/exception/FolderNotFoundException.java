package com.ote.file.api.exception;

import com.ote.file.api.model.Folder;

public class FolderNotFoundException extends Exception {

    private static final String MessageTemplate = "Folder '%s' is not found for application '%s' and perimeter '%s'";

    public FolderNotFoundException(String application, String perimeter, Folder folder) {
        super(String.format(MessageTemplate, folder.getPath(), application, perimeter));
    }
}


package com.ote.file.api.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class File {

    private final Folder folder;
    private final String name;

    @Override
    public String toString() {
        return getPath();
    }

    public String getPath() {
        return folder.getPath() + "/" + name;
    }
}
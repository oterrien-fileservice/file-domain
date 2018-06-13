package com.ote.file.spi;

import com.ote.file.api.model.File;
import com.ote.file.api.model.Folder;

import java.util.Set;

public interface IFileRepository {

    Set<Folder> getFolders(String application, String perimeter);

    Set<File> getFiles(String application, String perimeter, Folder folder);

    default boolean isFound(String application, String perimeter, Folder folder) {
        return getFolders(application, perimeter).
                stream().
                anyMatch(p -> p.equals(folder));
    }

    default boolean isFound(String application, String perimeter, File file) {
        return getFiles(application, perimeter, file.getFolder()).stream().
                anyMatch(p -> p.equals(file));
    }

    void write(File file, byte[] content);

    default void append(File file, byte[] content) {
        StringBuilder sb = new StringBuilder();
        byte[] current = read(file);
        if (current != null) {
            for (byte b : current) {
                sb.append((char) b);
            }
        }
        for (byte b : content) {
            sb.append((char) b);
        }
        write(file, sb.toString().getBytes());
    }

    byte[] read(File file);
}

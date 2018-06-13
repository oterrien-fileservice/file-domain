package com.ote.file.api.model;

import lombok.Data;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Folder {

    private final String[] path;

    public Folder(String... path) {
        this.path = path;
    }

    public Folder(String path) {
        this(Pattern.compile("\\\\|\\/").split(path));
    }

    public Folder(Folder parent, String... path) {
        this(Stream.concat(Arrays.stream(parent.path), Arrays.stream(path)).toArray(String[]::new));
    }

    @Override
    public String toString() {
        return getPath();
    }

    public String getPath() {
        return Stream.of(path).collect(Collectors.joining("/"));
    }
}

package com.ote.file.mock;

import com.ote.file.Utils;
import com.ote.file.api.model.File;
import com.ote.file.api.model.Folder;
import com.ote.file.spi.IFileRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class FileRepositoryMock implements IFileRepository {

    private final Map<Key, Set<Folder>> foldersMap = new HashMap<>();

    @Getter
    private final Map<Counter, Integer> counterMap = new HashMap<>();

    @Override
    public Set<Folder> getFolders(String application, String perimeter) {
        Key key = new Key(application, perimeter);
        return Optional.ofNullable(foldersMap.get(key)).orElse(Collections.emptySet());
    }

    @Override
    public Set<File> getFiles(String application, String perimeter, Folder folder) {
        try {
            return Files.list(Paths.get(folder.getPath())).map(p -> new File(folder, p.getFileName().toString())).collect(Collectors.toSet());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(File file, byte[] content) {
        increment(file, Type.WRITE);
        Utils.saveFile(file.getPath(), content);
    }

    @Override
    public void append(File file, byte[] content) {
        increment(file, Type.APPEND);
        StringBuilder sb = new StringBuilder().append(new String(read(file))).append(new String(content));
        write(file, sb.toString().getBytes());
    }

    @Override
    public byte[] read(File file) {
        increment(file, Type.READ);
        return Utils.readFile(file.getPath());
    }

    private synchronized void increment(File file, Type type) {
        Counter counter = new Counter(file.getName(), type);
        counterMap.merge(counter, 1, (a, b) -> a + b);
    }

    public void addFolder(String application, String perimeter, Folder folder) {

        Set<Folder> folders = getFolders(application, perimeter);
        if (!folders.isEmpty()) {
            folders.add(folder);
        } else {
            Key key = new Key(application, perimeter);
            foldersMap.put(key, Collections.singleton(folder));
        }
    }

    public void reset() {
        foldersMap.clear();
        counterMap.clear();
    }

    @Data
    @RequiredArgsConstructor
    public static class Key {
        private final String application;
        private final String perimeter;
    }

    @Data
    @RequiredArgsConstructor
    public static class Counter {
        private final String fileName;
        private final Type type;
    }

    public enum Type {
        READ, APPEND, WRITE;
    }
}

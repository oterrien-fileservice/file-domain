package com.ote.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Utils {

    public static String createDataSize(int msgSize) {
        StringBuilder sb = new StringBuilder(msgSize);
        for (int i = 0; i < msgSize; i++) {
            sb.append('0');
        }
        return sb.toString();
    }

    public static void saveFile(String path, byte[] content) {
        try {
            Files.write(Paths.get(path), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

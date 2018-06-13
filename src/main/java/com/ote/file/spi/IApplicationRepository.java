package com.ote.file.spi;

public interface IApplicationRepository {

    boolean isFound(String application);

    boolean isFound(String application, String perimeter);
}

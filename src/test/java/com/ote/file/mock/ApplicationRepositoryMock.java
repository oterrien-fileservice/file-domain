package com.ote.file.mock;

import com.ote.file.spi.IApplicationRepository;

public class ApplicationRepositoryMock implements IApplicationRepository {

    @Override
    public boolean isFound(String application) {
        return true;
    }

    @Override
    public boolean isFound(String application, String perimeter) {
        return true;
    }
}

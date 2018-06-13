package com.ote.file.mock;

import com.ote.file.spi.IUserRepository;

public class UserRepositoryMock implements IUserRepository {

    @Override
    public boolean isFound(String user) {
        return true;
    }
}

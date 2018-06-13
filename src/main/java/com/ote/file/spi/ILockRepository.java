package com.ote.file.spi;

import com.ote.file.api.model.File;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface ILockRepository extends ILock {

    ILock getLock();

    Optional<FileLock> getFileLock(KeyFileLock key);

    void lockFile(FileLock fileLock);

    void unlockFile(KeyFileLock key);


    default boolean tryLock(long timeout, TimeUnit timeUnit) {
        return getLock().tryLock(timeout, timeUnit);
    }

    default void unlock() {
        getLock().unlock();
    }

    @Data
    @RequiredArgsConstructor
    class FileLock {
        private final KeyFileLock key;
        private final String user;
        private final IUserRightRepository.Privilege privilege;
    }

    @Data
    @RequiredArgsConstructor
    class KeyFileLock {
        private final String application;
        private final String perimeter;
        private final File file;
    }

}

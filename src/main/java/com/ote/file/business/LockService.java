package com.ote.file.business;

import com.ote.file.api.exception.LockException;
import com.ote.file.api.model.File;
import com.ote.file.spi.ILockRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.ote.file.spi.ILockRepository.FileLock;
import static com.ote.file.spi.ILockRepository.KeyFileLock;
import static com.ote.file.spi.IUserRightRepository.Privilege;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class LockService {

    private final ILockRepository lockRepository;

    private final long timeout;

    private final TimeUnit timeUnit;

    /**
     * try to lock the file
     * this method is synchronized, hence it is executed between lockRepository.lock() and lockRepository.unlock()
     * each thread which call lockRepository.lock() should be blocked until thread which has taken the lock is unlocked
     */
    void lockFile(String user, String application, String perimeter, Privilege privilege, File file) throws LockException {
        withSynchronize(user, application, perimeter, privilege, file,
                () -> {
                    KeyFileLock keyFileLock = new KeyFileLock(application, perimeter, file);
                    FileLock fileLock = new FileLock(keyFileLock, user, privilege);
                    Optional<FileLock> currentLockOpt = lockRepository.getFileLock(keyFileLock);
                    if (currentLockOpt.isPresent()) {
                        FileLock currentLock = currentLockOpt.get();
                        if (currentLock.getPrivilege() != Privilege.READ || fileLock.getPrivilege() != Privilege.READ) {
                            throw new LockException(user, application, perimeter, privilege.getAction(), file, currentLock.getUser());
                        }
                    }
                    lockRepository.lockFile(fileLock);
                });
    }

    /**
     * release lock on the file
     * this method is synchronized, hence it is executed between lockRepository.lock() and lockRepository.unlock()
     */
    void unlockFile(String user, String application, String perimeter, Privilege privilege, File file) throws LockException {
        withSynchronize(user, application, perimeter, privilege, file,
                () -> {
                    KeyFileLock keyFileLock = new KeyFileLock(application, perimeter, file);
                    lockRepository.unlockFile(keyFileLock);
                });
    }

    private void withSynchronize(String user, String application, String perimeter, Privilege privilege, File file, LockedAction action) throws LockException {
        try {
            if (lockRepository.tryLock(timeout, timeUnit)) {
                action.execute();
            } else {
                throw new LockException(user, application, perimeter, privilege.getAction(), file, timeout, timeUnit);
            }
        } finally {
            lockRepository.unlock();
        }
    }

    @FunctionalInterface
    interface LockedAction {
        void execute() throws LockException;
    }
}

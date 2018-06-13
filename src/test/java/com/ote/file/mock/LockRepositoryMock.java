package com.ote.file.mock;

import com.ote.file.spi.ILock;
import com.ote.file.spi.ILockRepository;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class LockRepositoryMock implements ILockRepository {

    private final List<FileLock> keyList = Collections.synchronizedList(new ArrayList<>());

    private final Map<String, Long> timeToWait = new ConcurrentHashMap<>();

    @Getter
    private final ILock lock = new SynchronizedMock();

    @Override
    public Optional<FileLock> getFileLock(KeyFileLock key) {
        return keyList.stream().
                filter(p -> p.getKey().equals(key)).
                findAny();
    }

    @Override
    public void lockFile(FileLock key) {
        Long duration = timeToWait.get(key.getUser());
        if (duration != null) {
            try {
                Thread.sleep(duration);
            } catch (Exception ignored) {
            }
        }
        keyList.add(key);
    }

    @Override
    public void unlockFile(KeyFileLock key) {
        keyList.removeIf(p -> p.getKey().equals(key));
    }

    class SynchronizedMock implements ILock {

        private final java.util.concurrent.locks.Lock lock = new ReentrantLock();

        @Override
        public boolean tryLock(long timeout, TimeUnit timeUnit) {

            try {
                return lock.tryLock(timeout, timeUnit);
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void unlock() {
            try {
                lock.unlock();
            } catch (Exception ignored) {
                // Avoid error when lock had not been acquired
            }
        }
    }

    public void addSleepTime(String user, long duration) {
        timeToWait.put(user, duration);
    }

    public void reset() {
        keyList.clear();
        timeToWait.clear();
    }
}


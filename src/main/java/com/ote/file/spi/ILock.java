package com.ote.file.spi;

import java.util.concurrent.TimeUnit;

public interface ILock {

    boolean tryLock(long timeout, TimeUnit timeUnit);

    void unlock();
}
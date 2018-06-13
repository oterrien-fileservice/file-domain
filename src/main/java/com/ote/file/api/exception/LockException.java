package com.ote.file.api.exception;

import com.ote.file.api.model.File;

import java.util.concurrent.TimeUnit;

public class LockException extends Exception {

    private static final String MessageTemplate_AlreadyLocked = "User '%s' cannot %s the file '%s' for application '%s' and perimeter '%s' because this file is already locked by user '%s'";
    private static final String MessageTemplate_LockedTimeout = "User '%s' cannot %s the file '%s' for application '%s' and perimeter '%s' because the timeout exceeded (%d %s)";

    public LockException(String user, String application, String perimeter, String action, File file, String lockedUser) {
        super(String.format(MessageTemplate_AlreadyLocked, user, action, file.getPath(), application, perimeter, lockedUser));
    }

    public LockException(String user, String application, String perimeter, String action, File file, long timeout, TimeUnit timeUnit) {
        super(String.format(MessageTemplate_LockedTimeout, user, action, file.getPath(), application, perimeter, timeout, timeUnit.name()));
    }
}

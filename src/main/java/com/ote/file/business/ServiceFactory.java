package com.ote.file.business;

import com.ote.file.api.IFileService;
import com.ote.file.spi.*;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

@NoArgsConstructor
public final class ServiceFactory {

    public IFileService createFileService(IUserRepository userRepository,
                                          IApplicationRepository applicationRepository,
                                          IUserRightRepository userRightRepository,
                                          IFileRepository fileRepository,
                                          ILockRepository lockRepository,
                                          long timeout,
                                          TimeUnit timeUnit) {

        LockService lockService = new LockService(lockRepository, timeout, timeUnit);
        return new FileService(userRepository, applicationRepository, userRightRepository, fileRepository, lockService);
    }
}

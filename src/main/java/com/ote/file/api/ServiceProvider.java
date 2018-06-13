package com.ote.file.api;

import com.ote.file.business.ServiceFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServiceProvider {

    @Getter
    private static final ServiceProvider Instance = new ServiceProvider();

    @Getter
    private final ServiceFactory fileServiceFactory = new ServiceFactory();
}

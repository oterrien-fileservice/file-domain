package com.ote.file.business;

import com.ote.file.api.IFileService;
import com.ote.file.api.exception.*;
import com.ote.file.api.model.File;
import com.ote.file.api.model.Folder;
import com.ote.file.spi.IApplicationRepository;
import com.ote.file.spi.IFileRepository;
import com.ote.file.spi.IUserRepository;
import com.ote.file.spi.IUserRightRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.ote.file.spi.IUserRightRepository.Privilege;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class FileService implements IFileService {

    private final IUserRepository userRepository;

    private final IApplicationRepository applicationRepository;

    private final IUserRightRepository userRightRepository;

    private final IFileRepository fileRepository;

    private final LockService lockService;

    @Override
    public Set<Folder> getFolders(String user, String application, String perimeter)
            throws UserNotFoundException, ApplicationNotFoundException, PerimeterNotFoundException, UnauthorizedException {

        assertUserFound(user);
        assertApplicationFound(application);
        assertPerimeterFound(application, perimeter);

        Privilege privilege = Privilege.READ;
        assertUserIsAuthorized(user, application, perimeter, privilege);

        return fileRepository.getFolders(application, perimeter);
    }

    @Override
    public Set<File> getFiles(String user, String application, String perimeter, Folder folder)
            throws UserNotFoundException, ApplicationNotFoundException, PerimeterNotFoundException, FolderNotFoundException, UnauthorizedException {

        assertUserFound(user);
        assertApplicationFound(application);
        assertPerimeterFound(application, perimeter);

        Privilege privilege = Privilege.READ;
        assertUserIsAuthorized(user, application, perimeter, privilege);
        assertFolderFound(application, perimeter, folder);

        return fileRepository.getFiles(application, perimeter, folder);
    }

    @Override
    public Set<File> getFiles(String user, String application, String perimeter)
            throws UserNotFoundException, ApplicationNotFoundException, PerimeterNotFoundException, UnauthorizedException {

        assertUserFound(user);
        assertApplicationFound(application);
        assertPerimeterFound(application, perimeter);

        Privilege privilege = Privilege.READ;
        assertUserIsAuthorized(user, application, perimeter, privilege);

        Set<Folder> folders = fileRepository.getFolders(application, perimeter);
        return folders.stream().
                map(folder -> fileRepository.getFiles(application, perimeter, folder)).
                flatMap(p -> p.stream()).
                collect(Collectors.toSet());
    }

    @Override
    public byte[] read(String user, String application, String perimeter, File file)
            throws UserNotFoundException, ApplicationNotFoundException, PerimeterNotFoundException, FolderNotFoundException, FileNotFoundException, UnauthorizedException,
            LockException {

        assertUserFound(user);
        assertApplicationFound(application);
        assertPerimeterFound(application, perimeter);

        Privilege privilege = Privilege.READ;
        assertUserIsAuthorized(user, application, perimeter, privilege);
        assertFolderFound(application, perimeter, file.getFolder());
        assertFileFound(application, perimeter, file);

        return withLockFile(user, application, perimeter, privilege, file, () -> fileRepository.read(file));
    }

    @Override
    public void save(String user, String application, String perimeter, File file, byte[] content, boolean replaceIfFound)
            throws UserNotFoundException, ApplicationNotFoundException, PerimeterNotFoundException, FolderNotFoundException, FileFoundException, UnauthorizedException, LockException {

        assertUserFound(user);
        assertApplicationFound(application);
        assertPerimeterFound(application, perimeter);

        Privilege privilege = Privilege.WRITE;
        assertUserIsAuthorized(user, application, perimeter, privilege);
        assertFolderFound(application, perimeter, file.getFolder());

        if (!replaceIfFound) {
            // do not replace file if found --> make sure file does not exist (so that it will be created)
            assertFileNotFound(application, perimeter, file);
        }

        withLockFile(user, application, perimeter, privilege, file, () -> fileRepository.write(file, content));
    }

    @Override
    public void append(String user, String application, String perimeter, File file, byte[] content, boolean createIfNotFound)
            throws UserNotFoundException, ApplicationNotFoundException, PerimeterNotFoundException, FolderNotFoundException, FileNotFoundException, UnauthorizedException, LockException {

        assertUserFound(user);
        assertApplicationFound(application);
        assertPerimeterFound(application, perimeter);

        Privilege privilege = Privilege.WRITE;
        assertUserIsAuthorized(user, application, perimeter, privilege);
        assertFolderFound(application, perimeter, file.getFolder());

        if (!createIfNotFound) {
            // do not create file if not found --> make sure file exists (so that it will be updated)
            assertFileFound(application, perimeter, file);
        }

        withLockFile(user, application, perimeter, privilege, file,
                () -> {
                    if (fileRepository.isFound(application, perimeter, file)) {
                        fileRepository.append(file, content);
                    } else {
                        fileRepository.write(file, content);
                    }
                });
    }

    private void withLockFile(String user, String application, String perimeter, Privilege privilege, File file, Runnable runnable) throws LockException {
        withLockFile(user, application, perimeter, privilege, file, () -> {
            runnable.run();
            return null;
        });
    }

    private <T> T withLockFile(String user, String application, String perimeter, Privilege privilege, File file, Supplier<T> supplier) throws LockException {
        try {
            lockService.lockFile(user, application, perimeter, privilege, file);
            return supplier.get();
        } finally {
            lockService.unlockFile(user, application, perimeter, privilege, file);
        }
    }

    //region Assertions
    private void assertUserFound(String user) throws UserNotFoundException {

        if (!userRepository.isFound(user)) {
            throw new UserNotFoundException(user);
        }
    }

    private void assertApplicationFound(String application) throws ApplicationNotFoundException {

        if (!applicationRepository.isFound(application)) {
            throw new ApplicationNotFoundException(application);
        }
    }

    private void assertPerimeterFound(String application, String perimeter) throws PerimeterNotFoundException {

        if (!applicationRepository.isFound(application, perimeter)) {
            throw new PerimeterNotFoundException(application, perimeter);
        }
    }

    private void assertUserIsAuthorized(String user, String application, String perimeter, Privilege privilege) throws UnauthorizedException {

        if (!userRightRepository.isAuthorized(user, application, perimeter, privilege)) {
            throw new UnauthorizedException(user, application, perimeter, privilege.getAction());
        }
    }

    private void assertFolderFound(String application, String perimeter, Folder folder) throws FolderNotFoundException {

        if (!fileRepository.isFound(application, perimeter, folder)) {
            throw new FolderNotFoundException(application, perimeter, folder);
        }
    }

    private void assertFileFound(String application, String perimeter, File file) throws FileNotFoundException {

        if (!fileRepository.isFound(application, perimeter, file)) {
            throw new FileNotFoundException(application, perimeter, file);
        }
    }

    private void assertFileNotFound(String application, String perimeter, File file) throws FileFoundException {

        if (fileRepository.isFound(application, perimeter, file)) {
            throw new FileFoundException(application, perimeter, file);
        }
    }
    //endregion
}

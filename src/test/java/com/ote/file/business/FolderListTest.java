package com.ote.file.business;

import com.ote.file.api.IFileService;
import com.ote.file.api.ServiceProvider;
import com.ote.file.api.exception.ApplicationNotFoundException;
import com.ote.file.api.exception.PerimeterNotFoundException;
import com.ote.file.api.exception.UnauthorizedException;
import com.ote.file.api.exception.UserNotFoundException;
import com.ote.file.api.model.File;
import com.ote.file.api.model.Folder;
import com.ote.file.mock.LockRepositoryMock;
import com.ote.file.spi.*;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ote.file.spi.IUserRightRepository.Privilege;

public class FolderListTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IApplicationRepository applicationRepository;

    @Mock
    private IUserRightRepository userRightRepository;

    @Mock
    private IFileRepository fileRepository;

    private ILockRepository lockRepository = new LockRepositoryMock();

    private IFileService fileService;

    @Before
    public void init() throws Exception {

        MockitoAnnotations.initMocks(this);

        Mockito.when(userRightRepository.isAuthorized(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Privilege.class))).thenCallRealMethod();
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenCallRealMethod();
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(File.class))).thenCallRealMethod();

        fileService = ServiceProvider.getInstance().
                getFileServiceFactory().
                createFileService(userRepository, applicationRepository, userRightRepository, fileRepository, lockRepository, 1000, TimeUnit.MILLISECONDS);
    }

    @Test(expected = UserNotFoundException.class)
    public void userNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(false);

        fileService.getFolders(user, application, perimeter);
    }

    @Test(expected = ApplicationNotFoundException.class)
    public void applicationNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(false);

        fileService.getFolders(user, application, perimeter);
    }

    @Test(expected = PerimeterNotFoundException.class)
    public void perimeterNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        fileService.getFolders(user, application, perimeter);
    }

    @Test(expected = UnauthorizedException.class)
    public void readNotGranted() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(new HashSet<>());

        fileService.getFolders(user, application, perimeter);
    }

    @Test
    public void listFolderOK() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";

        Folder listFolder = new Folder(".", "target", "listFolderOK");
        Folder folder1 = new Folder(listFolder, "1");
        Folder folder2 = new Folder(listFolder, "2");
        Folder folder3 = new Folder(listFolder, "3");

        Files.deleteIfExists(Paths.get(folder1.getPath()));
        Files.deleteIfExists(Paths.get(folder2.getPath()));
        Files.deleteIfExists(Paths.get(folder3.getPath()));
        Files.deleteIfExists(Paths.get(listFolder.getPath()));
        Files.createDirectory(Paths.get(listFolder.getPath()));
        Files.createDirectory(Paths.get(folder1.getPath()));
        Files.createDirectory(Paths.get(folder2.getPath()));
        Files.createDirectory(Paths.get(folder3.getPath()));

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.READ));
        Mockito.when(fileRepository.getFolders(Mockito.anyString(), Mockito.anyString())).thenReturn(Files.list(Paths.get(listFolder.getPath())).map(p -> new Folder(p.toString())).collect(Collectors.toSet()));

        Set<Folder> folders = fileService.getFolders(user, application, perimeter);

        Assertions.assertThat(folders).contains(folder1, folder2, folder3);
    }
}

package com.ote.file.business;

import com.ote.file.api.IFileService;
import com.ote.file.api.ServiceProvider;
import com.ote.file.api.exception.*;
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

public class FolderFileListTest {

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
        Folder folder = new Folder("target");

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(false);

        fileService.getFiles(user, application, perimeter, folder);
    }

    @Test(expected = ApplicationNotFoundException.class)
    public void applicationNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder("target");

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(false);

        fileService.getFiles(user, application, perimeter, folder);
    }

    @Test(expected = PerimeterNotFoundException.class)
    public void perimeterNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder("target");

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        fileService.getFiles(user, application, perimeter, folder);
    }

    @Test(expected = UnauthorizedException.class)
    public void readNotGranted() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder("target");

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(new HashSet<>());

        fileService.getFiles(user, application, perimeter, folder);
    }

    @Test(expected = FolderNotFoundException.class)
    public void folderNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder("notExist");

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.READ));
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(false);

        fileService.getFiles(user, application, perimeter, folder);
    }

    @Test
    public void listFileOK() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";

        Folder listFolder = new Folder(".", "target", "listFileOK");
        File file1 = new File(listFolder, "1.txt");
        File file2 = new File(listFolder, "2.txt");
        File file3 = new File(listFolder, "3.txt");

        Files.deleteIfExists(Paths.get(file1.getPath()));
        Files.deleteIfExists(Paths.get(file2.getPath()));
        Files.deleteIfExists(Paths.get(file3.getPath()));
        Files.deleteIfExists(Paths.get(listFolder.getPath()));
        Files.createDirectory(Paths.get(listFolder.getPath()));
        Files.createFile(Paths.get(file1.getPath()));
        Files.createFile(Paths.get(file2.getPath()));
        Files.createFile(Paths.get(file3.getPath()));

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.READ));
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(true);
        Mockito.when(fileRepository.getFiles(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).
                thenReturn(Files.list(Paths.get(listFolder.getPath())).map(p -> new File(listFolder, p.getFileName().toString())).collect(Collectors.toSet()));

        Set<File> files = fileService.getFiles(user, application, perimeter, listFolder);

        Assertions.assertThat(files).contains(file1, file2, file3);
    }
}

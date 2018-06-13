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
import org.mockito.stubbing.Answer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ote.file.spi.IUserRightRepository.Privilege;

public class FileListTest {

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
                createFileService(userRepository, applicationRepository, userRightRepository, fileRepository, lockRepository, 100, TimeUnit.MILLISECONDS);
    }

    @Test(expected = UserNotFoundException.class)
    public void userNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(false);

        fileService.getFiles(user, application, perimeter);
    }

    @Test(expected = ApplicationNotFoundException.class)
    public void applicationNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(false);

        fileService.getFiles(user, application, perimeter);
    }

    @Test(expected = PerimeterNotFoundException.class)
    public void perimeterNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        fileService.getFiles(user, application, perimeter);
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

        fileService.getFiles(user, application, perimeter);
    }

    @Test
    public void listFolderFileOK() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";

        Folder listFolder = new Folder(".", "target", "listFolderFileOK");
        Folder folder1 = new Folder(listFolder, "1");
        File file1 = new File(folder1, "1.txt");
        File file2 = new File(folder1, "2.txt");
        File file3 = new File(folder1, "3.txt");
        Folder folder2 = new Folder(listFolder, "2");
        File file4 = new File(folder2, "1.txt");
        File file5 = new File(folder2, "2.txt");
        File file6 = new File(folder2, "3.txt");

        Files.deleteIfExists(Paths.get(file1.getPath()));
        Files.deleteIfExists(Paths.get(file2.getPath()));
        Files.deleteIfExists(Paths.get(file3.getPath()));
        Files.deleteIfExists(Paths.get(file4.getPath()));
        Files.deleteIfExists(Paths.get(file5.getPath()));
        Files.deleteIfExists(Paths.get(file6.getPath()));
        Files.deleteIfExists(Paths.get(folder1.getPath()));
        Files.deleteIfExists(Paths.get(folder2.getPath()));
        Files.deleteIfExists(Paths.get(listFolder.getPath()));

        Files.createDirectory(Paths.get(listFolder.getPath()));
        Files.createDirectory(Paths.get(folder1.getPath()));
        Files.createDirectory(Paths.get(folder2.getPath()));
        Files.createFile(Paths.get(file1.getPath()));
        Files.createFile(Paths.get(file2.getPath()));
        Files.createFile(Paths.get(file3.getPath()));
        Files.createFile(Paths.get(file4.getPath()));
        Files.createFile(Paths.get(file5.getPath()));
        Files.createFile(Paths.get(file6.getPath()));

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.READ));
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(true);

        Mockito.when(fileRepository.getFolders(Mockito.anyString(), Mockito.anyString())).thenReturn(Files.list(Paths.get(listFolder.getPath())).map(p -> new Folder(p.toString())).collect(Collectors.toSet()));
        Mockito.when(fileRepository.getFiles(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenAnswer(listFolder(folder1, folder2));

        Set<File> files = fileService.getFiles(user, application, perimeter);

        Assertions.assertThat(files).contains(file1, file2, file3, file4, file5, file6);
    }

    private static Answer<Set<File>> listFolder(Folder folder1, Folder folder2) {
        return invocation -> {
            Folder folderArgument = invocation.getArgument(2);
            if (folderArgument.equals(folder1)) {
                return Files.list(Paths.get(folder1.getPath())).map(p -> new File(folder1, p.getFileName().toString())).collect(Collectors.toSet());
            } else {
                return Files.list(Paths.get(folder2.getPath())).map(p -> new File(folder2, p.getFileName().toString())).collect(Collectors.toSet());
            }
        };
    }
}

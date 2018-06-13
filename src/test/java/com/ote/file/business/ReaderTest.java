package com.ote.file.business;

import com.ote.file.Utils;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static com.ote.file.spi.IUserRightRepository.Privilege;

public class ReaderTest {

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
        File file = new File(null, "a file");

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(false);

        fileService.read(user, application, perimeter, file);
    }

    @Test(expected = ApplicationNotFoundException.class)
    public void applicationNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        File file = new File(null, "a file");

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(false);

        fileService.read(user, application, perimeter, file);
    }

    @Test(expected = PerimeterNotFoundException.class)
    public void perimeterNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        File file = new File(null, "a file");

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        fileService.read(user, application, perimeter, file);
    }

    @Test(expected = UnauthorizedException.class)
    public void readNotGranted() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        File file = new File(null, "a file");

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(new HashSet<>());

        fileService.read(user, application, perimeter, file);
    }

    @Test(expected = FolderNotFoundException.class)
    public void FolderNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder(".", "target");
        File file = new File(folder, "a file");

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.READ));
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(false);

        fileService.read(user, application, perimeter, file);
    }

    @Test(expected = FileNotFoundException.class)
    public void pathNotDefined() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder(".", "target");
        File file = new File(folder, "a file");

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.READ));
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(true);
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(File.class))).thenReturn(false);

        fileService.read(user, application, perimeter, file);
    }

    @Test
    public void readOK() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder(".", "target");
        File file = new File(folder, "readOK.txt");

        String expected = "readOK";
        Utils.saveFile(file.getPath(), expected.getBytes());

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.READ));
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(true);
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(File.class))).thenReturn(true);
        Mockito.when(fileRepository.read(Mockito.any(File.class))).thenReturn(Utils.readFile(file.getPath()));

        byte[] actual = fileService.read(user, application, perimeter, file);

        Assertions.assertThat(actual).isEqualTo(expected.getBytes());
    }

    @Test
    public void readOK_WithWriteRight() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder(".", "target");
        File file = new File(folder, "readOK_WithWriteRight.txt");

        String expected = "readOK_WithWriteRight";
        Utils.saveFile(file.getPath(), expected.getBytes());

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.WRITE));
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(true);
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(File.class))).thenReturn(true);
        Mockito.when(fileRepository.read(Mockito.any(File.class))).thenReturn(Utils.readFile(file.getPath()));

        byte[] actual = fileService.read(user, application, perimeter, file);

        Assertions.assertThat(actual).isEqualTo(expected.getBytes());
    }
}

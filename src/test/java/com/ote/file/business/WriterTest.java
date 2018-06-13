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
import org.mockito.Spy;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static com.ote.file.spi.IUserRightRepository.Privilege;

public class WriterTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IApplicationRepository applicationRepository;

    @Mock
    private IUserRightRepository userRightRepository;

    @Mock
    private IFileRepository fileRepository;

    @Spy
    private ILockRepository lockRepository = new LockRepositoryMock();

    private IFileService fileService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        Mockito.doCallRealMethod().when(userRightRepository).isAuthorized(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Privilege.class));
        Mockito.doCallRealMethod().when(fileRepository).isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class));
        Mockito.doCallRealMethod().when(fileRepository).isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(File.class));
        Mockito.doCallRealMethod().when(fileRepository).append(Mockito.any(File.class), Mockito.any(byte[].class));

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
        String expected = "test";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(false);

        fileService.save(user, application, perimeter, file, expected.getBytes(), true);
    }

    @Test(expected = ApplicationNotFoundException.class)
    public void applicationNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        File file = new File(null, "a file");
        String expected = "test";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(false);

        fileService.save(user, application, perimeter, file, expected.getBytes(), true);
    }

    @Test(expected = PerimeterNotFoundException.class)
    public void perimeterNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        File file = new File(null, "a file");
        String expected = "test";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        fileService.save(user, application, perimeter, file, expected.getBytes(), true);
    }

    @Test(expected = UnauthorizedException.class)
    public void writeNotGranted() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        File file = new File(null, "a file");
        String expected = "test";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(new HashSet<>());

        fileService.save(user, application, perimeter, file, expected.getBytes(), true);
    }

    @Test(expected = UnauthorizedException.class)
    public void writeNotGranted_onlyRead() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        File file = new File(null, "a file");
        String expected = "test";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.READ));

        fileService.save(user, application, perimeter, file, expected.getBytes(), true);
    }

    @Test(expected = FolderNotFoundException.class)
    public void FolderNotFound() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder(".", "target");
        File file = new File(folder, "a file");
        String expected = "test";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.WRITE));
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(false);

        fileService.save(user, application, perimeter, file, expected.getBytes(), true);
    }

    @Test(expected = FileFoundException.class)
    public void FileFound_ButNoReplace() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder(".", "target");
        File file = new File(folder, "a file");
        String expected = "test";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.WRITE));
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(true);
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(File.class))).thenReturn(true);

        fileService.save(user, application, perimeter, file, expected.getBytes(), false);
    }

    @Test(expected = FileNotFoundException.class)
    public void Append_FileNotFound_ButNoCreate() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder(".", "target");
        File file = new File(folder, "a file");
        String expected = "test";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.WRITE));
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(true);
        Mockito.when(fileRepository.isFound(Mockito.anyString(), Mockito.anyString(), Mockito.any(File.class))).thenReturn(false);

        fileService.append(user, application, perimeter, file, expected.getBytes(), false);
    }

    @Test
    public void writeOK() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder(".", "target");
        File file = new File(folder, "writeOK.txt");
        String expected = "writeOK";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.WRITE));
        Mockito.when(fileRepository.getFolders(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(folder));
        Mockito.doAnswer(invocation -> answerSaveFile(invocation.getArgument(0), invocation.getArgument(1))).
                when(fileRepository).write(Mockito.any(File.class), Mockito.any(byte[].class));

        fileService.save(user, application, perimeter, file, expected.getBytes(), true);

        byte[] actual = Utils.readFile(file.getPath());

        Assertions.assertThat(actual).isEqualTo(expected.getBytes());
    }

    @Test
    public void replaceOK() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder(".", "target");
        File file = new File(folder, "replaceOK.txt");
        String expected = "replaceOK";

        String currentContent = "current";
        Utils.saveFile(file.getPath(), currentContent.getBytes());
        byte[] actual = Utils.readFile(file.getPath());
        Assertions.assertThat(actual).isEqualTo(currentContent.getBytes());

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.WRITE));
        Mockito.when(fileRepository.getFolders(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(folder));
        Mockito.when(fileRepository.getFiles(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(Collections.singleton(file));
        Mockito.when(fileRepository.read(Mockito.any(File.class))).thenReturn(Utils.readFile(file.getPath()));
        Mockito.doAnswer(invocation -> answerSaveFile(invocation.getArgument(0), invocation.getArgument(1))).
                when(fileRepository).write(Mockito.any(File.class), Mockito.any(byte[].class));

        fileService.save(user, application, perimeter, file, expected.getBytes(), true);

        actual = Utils.readFile(file.getPath());

        Assertions.assertThat(actual).isEqualTo(expected.getBytes());
    }

    @Test
    public void appendOK_FileExist() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder(".", "target");
        File file = new File(folder, "appendOK_FileExist.txt");
        String expected = "appendOK_FileExist";

        String currentContent = "current";
        Utils.saveFile(file.getPath(), currentContent.getBytes());
        byte[] actual = Utils.readFile(file.getPath());
        Assertions.assertThat(actual).isEqualTo(currentContent.getBytes());

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.WRITE));
        Mockito.when(fileRepository.getFolders(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(folder));
        Mockito.when(fileRepository.getFiles(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(Collections.singleton(file));
        Mockito.when(fileRepository.read(Mockito.any(File.class))).thenReturn(Utils.readFile(file.getPath()));
        Mockito.doAnswer(invocation -> answerSaveFile(invocation.getArgument(0), invocation.getArgument(1))).
                when(fileRepository).write(Mockito.any(File.class), Mockito.any(byte[].class));

        fileService.append(user, application, perimeter, file, expected.getBytes(), true);

        actual = Utils.readFile(file.getPath());

        StringBuilder sb = new StringBuilder().append(currentContent).append(expected);

        Assertions.assertThat(actual).isEqualTo(sb.toString().getBytes());
    }

    @Test
    public void appendOK_Create() throws Exception {

        String user = "a user";
        String application = "an application";
        String perimeter = "a perimeter";
        Folder folder = new Folder(".", "target");
        File file = new File(folder, "appendOK_Create.txt");
        String expected = "appendOK_FileExist";

        Mockito.when(userRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString())).thenReturn(true);
        Mockito.when(applicationRepository.isFound(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(userRightRepository.getPrivileges(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(Privilege.WRITE));
        Mockito.when(fileRepository.getFolders(Mockito.anyString(), Mockito.anyString())).thenReturn(Collections.singleton(folder));
        Mockito.when(fileRepository.getFiles(Mockito.anyString(), Mockito.anyString(), Mockito.any(Folder.class))).thenReturn(Collections.emptySet());
        Mockito.doAnswer(invocation -> answerSaveFile(invocation.getArgument(0), invocation.getArgument(1))).
                when(fileRepository).write(Mockito.any(File.class), Mockito.any(byte[].class));
        Mockito.when(fileRepository.read(Mockito.any(File.class))).thenAnswer(inv -> Utils.readFile(file.getPath()));

        fileService.append(user, application, perimeter, file, expected.getBytes(), true);

        byte[] actual = Utils.readFile(file.getPath());

        Assertions.assertThat(actual).isEqualTo(expected.getBytes());
    }

    private static Answer answerSaveFile(File file, byte[] content) {
        Utils.saveFile(file.getPath(), content);
        return null;
    }
}

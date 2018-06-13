package com.ote.file.business;

import com.ote.file.api.exception.LockException;
import com.ote.file.api.model.File;
import com.ote.file.api.model.Folder;
import com.ote.file.mock.LockRepositoryMock;
import com.ote.file.spi.ILockRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.ote.file.spi.IUserRightRepository.Privilege;

public class LockServiceTest {

    @Spy
    private ILockRepository lockRepository = new LockRepositoryMock();

    private LockService lockService;

    @Before
    public void init() throws Exception {

        MockitoAnnotations.initMocks(this);

        lockService = new LockService(lockRepository, 1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void twoUsersCanReadSameFileAtSameTime() throws Throwable {
        try {
            File file = new File(new Folder(".", "target", "testLockService"), "test.txt");

            CompletableFuture cf1 = CompletableFuture.runAsync(() -> run("user1", Privilege.READ, file, 100));
            Thread.sleep(10); // to enforce cf1 is executed before cf2
            CompletableFuture cf2 = CompletableFuture.runAsync(() -> run("user2", Privilege.READ, file, 1));

            CompletableFuture.allOf(cf1, cf2).get();
        } catch (Exception e) {
            if (e instanceof ExecutionException) {
                if (e.getCause() instanceof RuntimeException) {
                    throw e.getCause().getCause();
                }
            }
            throw e;
        }
    }

    @Test(expected = LockException.class)
    public void userCannotWriteFileWhichIsBeingRead() throws Throwable {
        try {
            File file = new File(new Folder(".", "target", "testLockService"), "test.txt");

            CompletableFuture cf1 = CompletableFuture.runAsync(() -> run("user1", Privilege.READ, file, 100));
            Thread.sleep(10); // to enforce cf1 is executed before cf2
            CompletableFuture cf2 = CompletableFuture.runAsync(() -> run("user2", Privilege.WRITE, file, 1));

            CompletableFuture.allOf(cf1, cf2).get();
        } catch (Exception e) {
            if (e instanceof ExecutionException) {
                if (e.getCause() instanceof RuntimeException) {
                    throw e.getCause().getCause();
                }
            }
            throw e;
        }
    }

    @Test(expected = LockException.class)
    public void userCannotReadFileWhichIsBeingWriten() throws Throwable {
        try {
            File file = new File(new Folder(".", "target", "testLockService"), "test.txt");

            CompletableFuture cf1 = CompletableFuture.runAsync(() -> run("user1", Privilege.WRITE, file, 100));
            Thread.sleep(10); // to enforce cf1 is executed before cf2
            CompletableFuture cf2 = CompletableFuture.runAsync(() -> run("user2", Privilege.READ, file, 1));

            CompletableFuture.allOf(cf1, cf2).get();
        } catch (Exception e) {
            if (e instanceof ExecutionException) {
                if (e.getCause() instanceof RuntimeException) {
                    throw e.getCause().getCause();
                }
            }
            throw e;
        }
    }

    @Test(expected = LockException.class)
    public void twoUsersCannotWriteSameFileAtSameTime() throws Throwable {
        try {
            File file = new File(new Folder(".", "target", "testLockService"), "test.txt");

            CompletableFuture cf1 = CompletableFuture.runAsync(() -> run("user1", Privilege.WRITE, file, 100));
            Thread.sleep(10); // to enforce cf1 is executed before cf2
            CompletableFuture cf2 = CompletableFuture.runAsync(() -> run("user2", Privilege.WRITE, file, 1));

            CompletableFuture.allOf(cf1, cf2).get();
        } catch (Exception e) {
            if (e instanceof ExecutionException) {
                if (e.getCause() instanceof RuntimeException) {
                    throw e.getCause().getCause();
                }
            }
            throw e;
        }
    }

    private void run(String user, Privilege privilege, File file, long wait) {
        try {
            try {
                lockService.lockFile(user, "application", "perimeter", privilege, file);
                Thread.sleep(wait);
            } finally {
                lockService.unlockFile(user, "application", "perimeter", privilege, file);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

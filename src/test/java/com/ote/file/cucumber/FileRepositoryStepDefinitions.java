package com.ote.file.cucumber;

import com.ote.file.Utils;
import com.ote.file.api.IFileService;
import com.ote.file.api.ServiceProvider;
import com.ote.file.api.model.File;
import com.ote.file.api.model.Folder;
import com.ote.file.mock.*;
import com.ote.file.spi.IUserRightRepository;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.Data;
import org.assertj.core.api.Assertions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ote.file.mock.FileRepositoryMock.Counter;
import static com.ote.file.mock.FileRepositoryMock.Type;

public class FileRepositoryStepDefinitions {

    private static final String APPLICATION = "application";
    private static final String PERIMETER = "perimeter";
    private static final String FOLDER = "./target/test_cucumber";

    private UserRepositoryMock userRepository = new UserRepositoryMock();

    private ApplicationRepositoryMock applicationRepository = new ApplicationRepositoryMock();

    private UserRightRepositoryMock userRightRepository = new UserRightRepositoryMock();

    private FileRepositoryMock fileRepository = new FileRepositoryMock();

    private LockRepositoryMock lockRepository = new LockRepositoryMock();

    private IFileService fileService;

    private Map<String, Object> scenarioContext = new ConcurrentHashMap<>();

    @Before
    public void init() throws Exception {

        fileService = ServiceProvider.getInstance().
                getFileServiceFactory().
                createFileService(userRepository, applicationRepository, userRightRepository, fileRepository, lockRepository, 10000, TimeUnit.MILLISECONDS);

        String folder = getFolder().getPath();
        if (!Files.exists(Paths.get(folder))) {
            Files.createDirectory(Paths.get(folder));
        }

        fileRepository.addFolder(APPLICATION, PERIMETER, getFolder());
    }

    @After
    public void tearDown() throws Exception {

        String folder = getFolder().getPath();
        if (Files.exists(Paths.get(folder))) {
            Files.list(Paths.get(folder)).forEach(p -> deleteFile(p));
        }

        scenarioContext.clear();
        userRightRepository.reset();
        fileRepository.reset();
        lockRepository.reset();
    }

    private static void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Folder getFolder() {
        return new Folder(FOLDER);
    }

    private static File getFile(String fileName) {
        return new File(getFolder(), fileName);
    }

    //region GIVEN...
    @Given("user \"(.*)\" granted to (READ|WRITE)")
    public void theUserIsGranted(String user, IUserRightRepository.Privilege privilege) throws Throwable {
        userRightRepository.addPrivilege(user, APPLICATION, PERIMETER, privilege);
    }

    @Given("file \"(.*)\" containing \"(.*)\"")
    public void theFileWhichContains(String fileName, String content) throws Throwable {
        File file = getFile(fileName);
        Utils.saveFile(file.getPath(), content.getBytes());
    }
    //endregion

    //region WHEN...
    @When("user \"(.*)\" requests to read the file \"(.*)\"")
    public void userRequestsToReadTheFile(String user, String fileName) throws Throwable {
        byte[] content = fileService.read(user, APPLICATION, PERIMETER, getFile(fileName));
        scenarioContext.put(user + "_READ_RESULT", Optional.ofNullable(content).map(p -> new String(p)).orElse(null));
    }

    @When("user \"(.*)\" requests to write \"(.*)\" to the file \"(.*)\"")
    public void userRequestsToWriteToTheFile(String user, String content, String fileName) throws Throwable {
        fileService.save(user, APPLICATION, PERIMETER, getFile(fileName), content.getBytes(), true);
    }

    @When("user \"(.*)\" requests to append \"(.*)\" to the file \"(.*)\"")
    public void userRequestsToAppendToTheFile(String user, String content, String fileName) throws Throwable {
    }

    @When("following requests are executed at the same time in the following order:")
    public void followingRequestsAreExecutedAtTheSameTimeInTheFollowingOrder(List<Request> requestList) throws Throwable {

        List<Runnable> runnables = requestList.stream().
                sorted(Comparator.comparing(Request::getOrder)).
                peek(request -> lockRepository.addSleepTime(request.getUser(), request.getDuration())).
                map(request -> execute(request)).
                collect(Collectors.toList());

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Runnable runnable : runnables) {
            futures.add(CompletableFuture.runAsync(runnable));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
    }

    private Runnable execute(Request request) {
        return () -> {
            File file = getFile(request.getFile());
            String user = request.getUser();
            byte[] content;
            try {
                // In order to keep the order, add a sleep which is longer when order is high
                Thread.sleep(request.getOrder());
                switch (request.getAction()) {
                    case READ:
                        content = fileService.read(user, APPLICATION, PERIMETER, file);
                        scenarioContext.put(user + "_READ_RESULT", Optional.ofNullable(content).map(p -> new String(p)).orElse(null));
                        break;
                    case CREATE_OR_REPLACE:
                        content = request.getContent().getBytes();
                        fileService.save(user, APPLICATION, PERIMETER, file, content, true);
                        break;
                    case CREATE_ONLY:
                        content = request.getContent().getBytes();
                        fileService.save(user, APPLICATION, PERIMETER, file, content, false);
                        break;
                    case APPEND_ONLY:
                        content = request.getContent().getBytes();
                        fileService.append(user, APPLICATION, PERIMETER, file, content, false);
                        break;
                    case APPEND_OR_CREATE:
                        content = request.getContent().getBytes();
                        fileService.append(user, APPLICATION, PERIMETER, file, content, true);
                        break;
                }
            } catch (Exception e) {
                scenarioContext.put(user + "_EXCEPTION_RESULT", e.getClass().getSimpleName());
                e.printStackTrace();
            }
        };
    }
    //endregion

    //region THEN...
    @Then("user \"(.*)\" received value \"(.*)\"")
    public void userReceivedValue(String user, String expectedValue) throws Throwable {
        String actualContent = (String) scenarioContext.get(user + "_READ_RESULT");
        Assertions.assertThat(actualContent).isEqualTo(expectedValue);
    }

    @Then("file \"(.*)\" has been saved (\\d+) times")
    public void theFileHasBeenSaved(String fileName, int expectedCount) throws Throwable {
        int actualCount = Optional.ofNullable(fileRepository.getCounterMap().get(new Counter(fileName, Type.WRITE))).orElse(0);
        Assertions.assertThat(actualCount).isEqualTo(expectedCount);
    }

    @Then("file \"(.*)\" has not been saved")
    public void theFileHasNotBeenSaved(String fileName) throws Throwable {
        theFileHasBeenSaved(fileName, 0);
    }

    @Then("file \"(.*)\" contains \"(.*)\"")
    public void theFileContains(String fileName, String expectedContent) throws Throwable {
        File file = getFile(fileName);
        byte[] actualContent = Utils.readFile(file.getPath());
        Assertions.assertThat(actualContent).isEqualTo(expectedContent.getBytes());
    }

    @Then("user \"(.*)\" received exception \"(.*)\"")
    public void userReceivedExceptionLockException(String user, String expectedException) throws Throwable {
        String actualException = (String) scenarioContext.get(user + "_EXCEPTION_RESULT");
        Assertions.assertThat(actualException).isEqualTo(expectedException);
    }
    //endregion
}

@Data
class Request {

    private Integer order;
    private String user;
    private Action action;
    private String file;
    private String content;
    private Long duration;

    enum Action {
        READ, CREATE_OR_REPLACE, CREATE_ONLY, APPEND_OR_CREATE, APPEND_ONLY;
    }
}

package com.ote.file.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty"},
        features = "src/test/resources/cucumber/FileRepository.feature",
        tags = {"~@Ignore", "@NoConcurrency, @Concurrency"},
        glue = "com.ote.file.cucumber")
public class FileRepositoryCucumberRunner {
}

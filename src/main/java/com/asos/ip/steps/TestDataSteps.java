package com.asos.ip.steps;

import com.asos.ip.helper.Constants.FrameworkConstants;
import com.asos.ip.helper.TestData.TestDataHelper;
import io.cucumber.java.en.Given;


public class TestDataSteps {

    TestDataHelper testDataHelper = new TestDataHelper();

    @Given("I have a file at {string} to upload")
    public void testDataFileExists(String filePath) {
        StringBuilder filePathBuilder = new StringBuilder();
        filePathBuilder.append(FrameworkConstants.DEFAULT_RESOURCES_INPUT_DIRECTORY).append(filePath);

        testDataHelper.fileExistsAtPath(filePathBuilder.toString());
    }
}

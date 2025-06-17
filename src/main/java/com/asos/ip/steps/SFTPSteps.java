package com.asos.ip.steps;

import com.asos.ip.config.EndpointManager;
import com.asos.ip.helper.SFTP.SFTPConnection;
import com.asos.ip.helper.SFTP.SFTPController;
import com.asos.ip.helper.SFTP.SFTPConnectionManager;
import com.asos.ip.helper.TestData.TestDataHelper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class SFTPSteps {

    private static final Logger logger = LoggerFactory.getLogger(SFTPSteps.class);
    private SFTPConnectionManager sftpConnectionManager;
    TestDataHelper testDataHelper = new TestDataHelper();

    // Constructor for dependency injection
    @Autowired
    public SFTPSteps(EndpointManager endpointManager) {
        this.sftpConnectionManager = endpointManager.getSftpConnectionManager();
    }

    // Uploads a file to the SFTP server using the specified path and to a specified host
    @When("I upload a file to the SFTP Host tagged as {string}, to the SFTP Path tagged as {string}, the file named {string}")
    public void uploadFileToSftp(String sftpHostTag, String remotePathTag, String fileName) throws IOException {

        String filePath = testDataHelper.returnFilePathFromInputResourcesDirectory(fileName);

        SFTPConnection sftpConnection = sftpConnectionManager.setActiveSFTPConnection(sftpHostTag);
        sftpConnection.setActiveRemotePath(remotePathTag);
        SFTPController sftpController = new SFTPController(sftpConnection);

        logger.info("Uploading file: " + filePath + " to the SFTP path tagged as: " + remotePathTag + "to the SFTP host tagged as " + sftpHostTag);
        sftpController.insertDataIntoSFTP(filePath);
        logger.info("File upload completed.");

        sftpController.disconnectSFTPChannel();
    }

    @When("I generate a {long} megabyte test file in the {string} SFTP server, at the path {string}, with name {string}")
    public void threadSplittingTest(long totalFileSize, String sftpHostTag, String remotePathTag, String fileName) {

        SFTPConnection sftpConnection = sftpConnectionManager.setActiveSFTPConnection(sftpHostTag);
        sftpConnection.setActiveRemotePath(remotePathTag);
        SFTPController sftpController = new SFTPController(sftpConnection);

        sftpController.generateTestFileInSFTPServer(fileName, totalFileSize, 5);

        sftpController.disconnectSFTPChannel();
    }

    @Then("I should see in the SFTP Host tagged as {string}, in the SFTP Path tagged as {string}, the file named {string}")
    public void checkFileExistsInSftp(String sftpHostTag, String remotePathTag, String fileName) throws IOException {

        SFTPConnection sftpConnection = sftpConnectionManager.setActiveSFTPConnection(sftpHostTag);
        sftpConnection.setActiveRemotePath(remotePathTag);
        SFTPController sftpController = new SFTPController(sftpConnection);

        boolean fileExists;
        logger.info("Checking if file exists: {}", fileName);
        fileExists = sftpController.checkFileExists(fileName);
        if (!fileExists) {
            throw new IllegalStateException("File does not exist in SFTP server");
        }

        assertTrue(fileExists);

        sftpController.disconnectSFTPChannel();
    }

}
package com.asos.ip.steps;

import com.asos.ip.config.EndpointManager;
import com.asos.ip.helper.BlobStorage.BlobStorageConnection;
import com.asos.ip.helper.BlobStorage.BlobStorageConnectionManager;
import com.asos.ip.helper.BlobStorage.BlobStorageController;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.asos.ip.helper.Constants.FrameworkConstants.DEFAULT_RESOURCES_INPUT_DIRECTORY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobStorageSteps {

    BlobStorageConnectionManager blobStorageConnectionManager;
    BlobContainerClient activeBlobStorageContainerClient;
    BlobStorageConnection activeBlobStorageConnection;

    private static final String APPLICATION_GZIP = "application/gzip";

    @Autowired
    public BlobStorageSteps(EndpointManager endpointManager) {
        blobStorageConnectionManager = endpointManager.getBlobStorageConnectionManager();
    }

    @When("I upload file {string} to Blob Storage Account {string} to Container {string} at path {string}")
    public void uploadFileToBlobStorage(String localFileName, String databaseName, String containerName, String blobFilePath) {

        activeBlobStorageConnection = blobStorageConnectionManager.setActiveBlobStorageConnection(databaseName);

        activeBlobStorageContainerClient = activeBlobStorageConnection.setActiveBlobContainerClient(containerName);

        BlobStorageController blobStorageController = new BlobStorageController(activeBlobStorageContainerClient);

        String localFilePath = DEFAULT_RESOURCES_INPUT_DIRECTORY + localFileName;

        blobStorageController.uploadFileToBlobStorage(blobFilePath, localFilePath, APPLICATION_GZIP);
    }

    @Then("There should be a file in Blob Storage Account {string} in Container {string} that contains string {string}")
    public void checkBlobFileInContainerMatchesExpectedContent(String databaseName, String containerName, String searchString) {

        activeBlobStorageConnection = blobStorageConnectionManager.setActiveBlobStorageConnection(databaseName);

        activeBlobStorageContainerClient = activeBlobStorageConnection.setActiveBlobContainerClient(containerName);

        BlobStorageController blobStorageController = new BlobStorageController(activeBlobStorageContainerClient);

        List<BlobItem> blobFilesMatchingContent = blobStorageController.getBlobFilesMatchingContent(searchString);

        assertFalse(blobFilesMatchingContent.isEmpty());
    }

    @Then("There should be a file in Blob Storage Account {string} in Container {string} with the filename {string}")
    public void checkBlobFileInContainerMatchesExpectedName(String databaseName, String containerName, String blobFileName) {

        activeBlobStorageConnection = blobStorageConnectionManager.setActiveBlobStorageConnection(databaseName);

        activeBlobStorageContainerClient = activeBlobStorageConnection.setActiveBlobContainerClient(containerName);

        BlobStorageController blobStorageController = new BlobStorageController(activeBlobStorageContainerClient);

        assertTrue(blobStorageController.doesBlobExist(blobStorageController.returnBlob(blobFileName)));
    }
}

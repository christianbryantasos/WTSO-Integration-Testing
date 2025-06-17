package com.asos.ip.helper.BlobStorage;

import com.asos.ip.helper.TestData.TestDataHelper;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for interacting with Azure Blob Storage.
 * This class provides methods for connecting to Blob Storage,
 * managing blobs, and performing file uploads with logging capabilities.
 */
public class BlobStorageController {

    private static final Logger logger = LoggerFactory.getLogger(BlobStorageController.class);

    BlobContainerClient blobContainerClient;
    TestDataHelper testDataHelper = new TestDataHelper();

    public BlobStorageController(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    /**
     * Retrieves a BlobClient for the specified blob in the container.
     *
     * @param blobName The name of the blob to retrieve.
     * @return BlobClient instance for the specified blob.
     */
    public BlobClient returnBlob(String blobName) {
        if (testDataHelper.doesStringContainDynamicDateTime(blobName)) {
            blobName = testDataHelper.returnStringWithFormatedDateTime(blobName);
        };

        logger.info("Fetching Blob: {}", blobName);
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            logger.info("Successfully returned Blob: {}", blobName);
            return blobClient;
        } catch (Exception e) {
            logger.error("Error fetching Blob {}: {}", blobName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Checks if the specified blob exists in the Blob Storage.
     *
     * @param blobClient The BlobClient instance for the blob to check.
     * @return True if the blob exists, false otherwise.
     */
    public boolean doesBlobExist(BlobClient blobClient) {
        logger.info("Checking if Blob exists: {}", blobClient.getBlobName());
        try {
            boolean exists = blobClient.exists();
            logger.info("Blob {} existence: {}", blobClient.getBlobName(), exists);
            return exists;
        } catch (Exception e) {
            logger.error("Error checking Blob existence {}: {}", blobClient.getBlobName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Uploads a file to Azure Blob Storage in the specified container.
     *

     * @param fileNameBlob The name to assign to the blob in Blob Storage.
     * @param filePath The local file path of the file to upload.
     * @param contentType The content type of the file being uploaded.
     */
    public void uploadFileToBlobStorage(String fileNameBlob, String filePath, String contentType) {
        try {

            if (testDataHelper.doesStringContainDynamicDateTime(fileNameBlob)) {
                fileNameBlob = testDataHelper.returnStringWithFormatedDateTime(fileNameBlob);
            };

            logger.info("Uploading file {} to Blob Storage in container {}", filePath, blobContainerClient.getBlobContainerName());
            blobContainerClient.getBlobClient(fileNameBlob).uploadFromFile(filePath);
            blobContainerClient.getBlobClient(fileNameBlob).setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
            logger.info("File {} successfully uploaded to Blob {}", filePath, fileNameBlob);
        } catch (Exception e) {
            logger.error("Error uploading file {} to Blob {}: {}", filePath, fileNameBlob, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes a specific Blob file.
     *
     * @param blobItem The blobItem to delete.
     */
    public void deleteBlobFile(BlobItem blobItem) {

        String blobName = blobItem.getName();

        if (blobItem.isDeleted()) {
            logger.info("Blob File {} is already deleted, cannot delete", blobName);
            return;
        }

        try {
            BlockBlobClient blobClient = blobContainerClient.getBlobClient(blobName).getBlockBlobClient();
            blobClient.delete();
            logger.info("Successfully deleted Blob File {}", blobName);
        } catch (Exception e) {
            logger.info("Error when deleting blob file {}: {}", blobName, e.getMessage());
        }
    }

    /**
     * Checks if a blob exists and uploads a new file to Blob Storage,
     * deleting the existing blob if it exists.
     *

     * @param fileNameBlob The name to assign to the blob in Blob Storage.
     * @param filePath The local file path of the file to upload.
     * @param contentType The content type of the file being uploaded.
     */
    public void checkIfBlobExistsAndUpload(String fileNameBlob, String filePath, String contentType) {
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(fileNameBlob);
            if (doesBlobExist(blobClient)) {
                logger.info("Blob {} exists, deleting it before uploading new file", fileNameBlob);
                blobClient.delete();
            }
            blobClient.uploadFromFile(filePath);
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
            logger.info("File {} successfully uploaded to Blob {}", filePath, fileNameBlob);
        } catch (Exception e) {
            logger.error("Error during check and upload for Blob {}: {}", fileNameBlob, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns all files in a container than have content matching a specific string
     * @param searchString The string content used to match files on
     * @return
     */
    public List<BlobItem> getBlobFilesMatchingContent(String searchString) {

        List<BlobItem> matchingBlobItems = new ArrayList<>();

        for (BlobItem blobItem : blobContainerClient.listBlobs()) {

            if(blobItemIsCompressedFile(blobItem)) {
                logger.info("Skipping Compressed File: " + blobItem.getName());
                continue;
            }

            BlobClient blobClient = blobContainerClient.getBlobClient(blobItem.getName());

            try (InputStream blobInputStream = blobClient.openInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(blobInputStream));
                StringBuilder contentBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line);
                }

                if (contentBuilder.toString().contains(searchString)) {
                    matchingBlobItems.add(blobItem);
                }
            } catch (Exception e) {
                logger.error("Error during attempt to read blob file {}: {}", blobItem.getName(), e.getMessage());
            }
        }

        return matchingBlobItems;
    }

    public List<BlobItem> searchListOfBlobItemsForMatchingContent(List<BlobItem> blobItems, String searchString) {
        List<BlobItem> matchingBlobItems = new ArrayList<>();

        for (BlobItem blobItem : blobItems) {

            if(blobItemIsCompressedFile(blobItem)) {
                logger.info("Skipping Compressed File: " + blobItem.getName());
                continue;
            }

            BlobClient blobClient = blobContainerClient.getBlobClient(blobItem.getName());
            try (InputStream blobInputStream = blobClient.openInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(blobInputStream));
                StringBuilder contentBuilder = new StringBuilder();
                String line;

                // Read the blob content line by line
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line);
                }

                // Check if the content contains the search string
                if (contentBuilder.toString().contains(searchString)) {
                    matchingBlobItems.add(blobItem);
                }
            } catch (Exception e) {
                logger.error("Error during attempt to read blob file {}: {}", blobItem.getName(), e.getMessage());
            }
        }

        return matchingBlobItems;
    }

    /**
     * Returns all files in a container that match a specific heirarchy
     * @param searchString The string heirarchy used to match files on
     * @return
     */
    public List<BlobItem> getBlobFilesMatchingHeirarchy(String searchString) {

        List<BlobItem> matchingBlobItems = new ArrayList<>();

        if (searchString.equalsIgnoreCase("")) {
            logger.info("No search string provided, returning all blob files.");
            blobContainerClient.listBlobs().forEach(blobItem -> {
                
                matchingBlobItems.add(blobItem);
            });
        }

        ListBlobsOptions options = new ListBlobsOptions().setPrefix(searchString + "/");

        blobContainerClient.listBlobsByHierarchy("/", options, Duration.ofSeconds(20)).forEach(blobItem -> {

            if (!blobItem.isPrefix()) {
                matchingBlobItems.add(blobItem);
            }
        });

        matchingBlobItems.forEach(blobItem -> {
            logger.info("Matching blob: {}", blobItem.getName());
        });

        return matchingBlobItems;
    }

    public boolean blobItemIsCompressedFile(BlobItem blobItem) {
        return blobItem.getName().endsWith(".tar.gz");
    }
}

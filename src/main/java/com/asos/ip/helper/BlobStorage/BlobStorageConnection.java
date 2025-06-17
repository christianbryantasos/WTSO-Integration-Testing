package com.asos.ip.helper.BlobStorage;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.slf4j.LoggerFactory;
import java.util.HashMap;


public class BlobStorageConnection {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BlobStorageConnection.class);
    private final BlobServiceClientBuilder blobServiceClientBuilder = new BlobServiceClientBuilder();

    private final BlobServiceClient blobServiceClient;

    public HashMap<String, BlobContainerClient> getBlobContainerClients() {
        return blobContainerClients;
    }

    private final HashMap<String, BlobContainerClient> blobContainerClients = new HashMap<>();


    public BlobStorageConnection(String blobAccountNameConnectionString) {
        blobServiceClient = connectToBlobStorageAccount(blobAccountNameConnectionString);
    }


    private BlobServiceClient connectToBlobStorageAccount(String connectionString) {

        logger.info("Attempting to connect to Blob Storage");

        try {
            BlobServiceClient blobServiceClient = blobServiceClientBuilder
                    .connectionString(connectionString)
                    .buildClient();
            logger.info("Successfully connected to Blob Storage");
            return blobServiceClient;
        } catch (Exception e) {
            logger.error("Failed to connect to Blob Storage: {}", e.getMessage(), e);
            throw e;
        }
    }

    public BlobContainerClient returnBlobContainer(String containerName) {

        logger.info("Fetching Blob Container: {} from database {}", containerName, blobServiceClient.getAccountName());

        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
            logger.info("Successfully returned container: {} ", containerName);
            return blobContainerClient;
        } catch (Exception e) {
            logger.error("Error fetching Blob Container {}: {}", containerName, e.getMessage(), e);
            throw e;
        }
    }

    public BlobServiceClient getBlobServiceClient() {
        return blobServiceClient;
    }

    public BlobContainerClient setActiveBlobContainerClient(String containerName) {
        containerName = containerName.toLowerCase();

        if (!blobContainerClients.containsKey(containerName)) {
            logger.error("No Blob Container Client found for: {}", containerName);
        }
        if (blobContainerClients.get(containerName) == null) {
            logger.error("No active connection found for Container Client: {}", containerName);
        } else {
            logger.info("Setting active Container Client to: {}", containerName);
        }

        return blobContainerClients.get(containerName);
    }


    public BlobContainerClient getBlobContainerClient(String containerName) {
        return blobContainerClients.get(containerName);
    }
}

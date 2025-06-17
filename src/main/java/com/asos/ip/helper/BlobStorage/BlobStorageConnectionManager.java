package com.asos.ip.helper.BlobStorage;

import com.asos.ip.config.LoadConfigurations;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import java.util.HashMap;
import java.util.Properties;

public class BlobStorageConnectionManager {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BlobStorageConnectionManager.class);

    private final Properties properties;

    BlobStorageConnection activeBlobStorageConnection;
    HashMap<String, BlobStorageConnection> blobStorageConnections = new HashMap<>();

    public BlobStorageConnectionManager(LoadConfigurations loadConfigurations) {
        this.properties = loadConfigurations.getConfigProperties();
    }

    @Bean
    @Lazy
    public BlobStorageConnection createBlobStorageConnection(String storageAccountName) {
        logger.info("Creating connection to Blob Storage Account: {}", storageAccountName);

        String blobAccountConnectionStringPlaceholder = "azure.storage.queue.{storageAccountName}.connection.string";
        String blobAccountNameConnectionString = properties.getProperty(blobAccountConnectionStringPlaceholder.replace("{storageAccountName}", storageAccountName));

        return new BlobStorageConnection(blobAccountNameConnectionString);
    }

    public BlobStorageConnection setActiveBlobStorageConnection(String storageAccountName) {

        storageAccountName = storageAccountName.toLowerCase();

        if (!blobStorageConnections.containsKey(storageAccountName)) {
            activeBlobStorageConnection = createBlobStorageConnection(storageAccountName);
            blobStorageConnections.put(storageAccountName, activeBlobStorageConnection);
            addMatchingContainersForStorageAccountToConnection(storageAccountName);
        } else {
            activeBlobStorageConnection = blobStorageConnections.get(storageAccountName);
        }

        return activeBlobStorageConnection;
    }

    private void addMatchingContainersForStorageAccountToConnection(String blobStorageAccountTag) {

        blobStorageAccountTag = blobStorageAccountTag.toLowerCase();

        String blobContainerNamePlaceholder = "azure.storage.blob.{storageAccountName}.container.name.";
        String blobContainerPathKey = blobContainerNamePlaceholder.replace("{storageAccountName}", blobStorageAccountTag);

        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(blobContainerPathKey)) {
                String blobContainerNameTag = key.replace(blobContainerPathKey, "");
                String blobContainerNameValue = properties.getProperty(key);
                activeBlobStorageConnection.getBlobContainerClients().put(blobContainerNameTag, activeBlobStorageConnection.returnBlobContainer(blobContainerNameValue));
            }
        }
    }
}

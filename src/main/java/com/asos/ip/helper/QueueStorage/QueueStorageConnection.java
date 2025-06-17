package com.asos.ip.helper.QueueStorage;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class QueueStorageConnection {

    private final QueueServiceClient storageQueueConnection;
    private final HashMap<String, QueueClient> queueClientHashmap = new HashMap<>();
    private QueueClient activeQueueClient;

    private static final Logger logger = LoggerFactory.getLogger(QueueStorageConnection.class);

    public QueueStorageConnection(String connectionString) {
        storageQueueConnection = connectToQueueStorage(connectionString);
    }

    public QueueServiceClient connectToQueueStorage(String connectionString) {
        logger.info("Attempting to connect to Queue Storage");
        try {
            QueueServiceClient queueServiceClient = new QueueServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            logger.info("Successfully connected to Queue Storage");
            return queueServiceClient;
        } catch (Exception e) {
            logger.error("Failed to connect to Queue Storage: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void addQueueClient(String queueName, String queueNameValue) {
        queueClientHashmap.put(queueName, storageQueueConnection.getQueueClient(queueNameValue));
        logger.info("Successfully added Queue Client with Key: {}", queueName);
    }

    public void setActiveQueueClient(String queueName) {
        activeQueueClient = queueClientHashmap.get(queueName);
        logger.info("Active Queue Client set to: {}", queueName);
    }

    public QueueClient getActiveQueueClient() {
        return activeQueueClient;
    }
}
package com.asos.ip.helper.QueueStorage;

import com.asos.ip.config.LoadConfigurations;
import com.azure.storage.queue.QueueClient;

import java.util.HashMap;
import java.util.Properties;

public class QueueStorageConnectionManager {

    private Properties properties;
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(QueueStorageConnectionManager.class);

    QueueStorageConnection activeQueueStorageConnection;
    HashMap<String, QueueStorageConnection> queueStorageConnections = new HashMap<>();

    public QueueStorageConnectionManager(LoadConfigurations loadConfigurations) {
        this.properties = loadConfigurations.getConfigProperties();
    }

    public void setActiveQueueStorageConnection(String queueStorageAccountName) {

        if(queueStorageConnections.isEmpty()) {
            preloadQueueStorageConnections();
        }

        String queueStorageAccountNameLowerCase = queueStorageAccountName.toLowerCase();
        activeQueueStorageConnection = queueStorageConnections.get(queueStorageAccountNameLowerCase);
    }

    public QueueClient returnActiveQueueClient(String queueName) {

        String queueNameLowerCase = queueName.toLowerCase();

        activeQueueStorageConnection.setActiveQueueClient(queueNameLowerCase);
        return activeQueueStorageConnection.getActiveQueueClient();
    }

    private void preloadQueueStorageConnections() {
        // Queue Storage Connection String Yaml Keys Must Follow The Following Format: "azure.storage.queue.<storage_account_name>.connection.string"

        String storageAccountKeyBeginning = "azure.storage.queue.";
        String storageAccountKeyEnding = ".connection.string";

        for (String propertyKey : properties.stringPropertyNames()) {

            if (propertyKey.startsWith(storageAccountKeyBeginning) && propertyKey.endsWith(storageAccountKeyEnding)) {

                String storageAccountName = propertyKey.substring(storageAccountKeyBeginning.length(), propertyKey.length() - storageAccountKeyEnding.length());
                queueStorageConnections.put(storageAccountName.toLowerCase(), new QueueStorageConnection(properties.getProperty(propertyKey)));
                logger.info("Added Queue Storage Connection: {}", storageAccountName);

                preloadQueueStorageClientsForConnection(storageAccountName);
            }
        }
    }

    private void preloadQueueStorageClientsForConnection(String storageAccountName) {
        // Queue Storage Client Yaml Keys Must Follow The Following Format: "azure.storage.queue.<storage_account_name>.<queue_name>.queue.name"
        String queueNameKeyBeginning = "azure.storage.queue." + storageAccountName + ".";
        String queueNameKeyEnding = ".queue.name";

        for (String propertyKey : properties.stringPropertyNames()) {

            if (propertyKey.startsWith(queueNameKeyBeginning) && propertyKey.endsWith(queueNameKeyEnding)) {

                String queueName = propertyKey.substring(queueNameKeyBeginning.length(), propertyKey.length() - queueNameKeyEnding.length());
                String queueNameValue = properties.getProperty(propertyKey);
                queueStorageConnections.get(storageAccountName.toLowerCase()).addQueueClient(queueName, queueNameValue);
            }
        }
    }
}

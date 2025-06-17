package com.asos.ip.helper.TableStorage;

import com.asos.ip.config.LoadConfigurations;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.Properties;

public class TableStorageConnectionManager {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TableStorageConnectionManager.class);

    private final Properties properties;

    TableStorageConnection activeAzureTableStorageConnection;
    HashMap<HashMap<String, String>, TableStorageConnection> storageAccountConnections = new HashMap<>();

    public TableStorageConnectionManager(LoadConfigurations loadConfigurations) {
        this.properties = loadConfigurations.getConfigProperties();
    }

    @Bean
    @Lazy
    public TableStorageConnection connectToTableStorage(HashMap<String, String> tableConnectionPair) {
        String storageAccountName = (String) tableConnectionPair.keySet().toArray()[0];
        String storageTableName = tableConnectionPair.get(storageAccountName);

        logger.info("Loading Azure Storage Account for {} & Table {}", storageAccountName, storageTableName );
        String azureStorageAccountConnectionStringPlaceholder = "azure.storage.queue.{storageAccountName}.connection.string";

        String storageAccountNameConnectionString = properties.getProperty(azureStorageAccountConnectionStringPlaceholder.replace("{storageAccountName}", storageAccountName));

        return new TableStorageConnection(storageAccountNameConnectionString, storageTableName);
    }

    public TableStorageConnection setActiveStorageAccountConnection(String storageAccountName, String tableName) {
        String storageAccountNameLowercase = storageAccountName.toLowerCase();

        HashMap<String, String> tableConnectionPair = new HashMap<>();
        tableConnectionPair.put(storageAccountNameLowercase, tableName);

        if (storageAccountConnections.containsKey(tableConnectionPair)) {
            activeAzureTableStorageConnection = storageAccountConnections.get(tableConnectionPair);
            return activeAzureTableStorageConnection;
        } else {

            storageAccountConnections.put(tableConnectionPair, connectToTableStorage(tableConnectionPair));
            activeAzureTableStorageConnection = storageAccountConnections.get(tableConnectionPair);
        }
        return activeAzureTableStorageConnection;
    }
}
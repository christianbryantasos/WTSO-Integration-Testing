package com.asos.ip.helper.TableStorage;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import org.slf4j.LoggerFactory;

public class TableStorageConnection {

    private final TableClient tableClient;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TableStorageConnection.class);


    public TableStorageConnection(String connectionString, String tableName) {
        this.tableClient = connectToTableStorage(connectionString, tableName);
        logger.info("Connected to Table Storage: {}", tableClient.getTableName());
    }

    public TableClient connectToTableStorage(String connectionString, String tableName) {
        try {
            return new TableClientBuilder()
                    .connectionString(connectionString)
                    .tableName(tableName)
                    .buildClient();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to Table Storage: " + e.getMessage(), e);
        }
    }

    public TableClient getTableStorageConnection() {
        return tableClient;
    }
}
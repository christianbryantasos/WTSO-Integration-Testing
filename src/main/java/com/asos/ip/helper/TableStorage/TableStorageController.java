package com.asos.ip.helper.TableStorage;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;

public class TableStorageController {

    private final TableClient tableClient;

    public TableStorageController(TableStorageConnection tableStorageConnection) {
        this.tableClient = tableStorageConnection.getTableStorageConnection();
    }

    public void updateCurrentlyRunning(String partitionKey, String rowKey, String currentlyRunning) {

        boolean currentlyRunningBoolean = currentlyRunning.equalsIgnoreCase("true");

        System.out.println("Updating the table storage with the currently running status: " + currentlyRunningBoolean);

        TableEntity entity = tableClient.getEntity(partitionKey, rowKey);
        if (entity != null) {
            entity.addProperty("CurrentlyRunning", currentlyRunningBoolean);
            tableClient.updateEntity(entity);
        }
    }
}
package com.asos.ip.steps;

import com.asos.ip.config.EndpointManager;
import com.asos.ip.helper.TableStorage.TableStorageConnection;
import com.asos.ip.helper.TableStorage.TableStorageConnectionManager;
import com.asos.ip.helper.TableStorage.TableStorageController;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TableStorageSteps {

    private static final Logger logger = LoggerFactory.getLogger(TableStorageSteps.class);
    private TableStorageConnectionManager tableStorageConnectionManager;
    TableStorageConnection tableStorageConnection;

    @Autowired
    public TableStorageSteps(EndpointManager endpointManager) {
        this.tableStorageConnectionManager = endpointManager.getTableStorageConnectionManager();
    }

    @When("I update the currently running status to {string} in the {string} storage account at the {string} table for partition key {string} and row key {string}")
    public void updateTableStorage(String currentlyRunning, String storageAccount, String tableName, String partitionKey, String rowKey) {

        logger.info("Updating the currently running status to {} ", currentlyRunning);

        try {

            tableStorageConnection = tableStorageConnectionManager.setActiveStorageAccountConnection(storageAccount, tableName);
            TableStorageController tableStorageController = new TableStorageController(tableStorageConnection);
            tableStorageController.updateCurrentlyRunning(partitionKey, rowKey, currentlyRunning);
            logger.info("Table storage updated...");

        } catch (Exception e) {
            logger.error("Failed to update table storage: {}", e.getMessage());
        }
    }

}
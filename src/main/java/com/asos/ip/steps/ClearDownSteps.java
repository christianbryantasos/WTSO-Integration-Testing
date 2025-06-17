package com.asos.ip.steps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.asos.ip.helper.ClearDown.ClearDown;
import com.asos.ip.config.EndpointManager;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;

public class ClearDownSteps {

    private final ClearDown clearDown;
    private static final Logger logger = LoggerFactory.getLogger(ClearDownSteps.class);

    private Map<String, Map<String, Map<String, List<String>>>> mongoDataToBeCleared = new HashMap<>();
    private Map<String, Map<String, List<String>>> sftpDataMultipleHostsToBeCleared = new HashMap<>();
    private Map<String, Map<String, List<String>>> blobStorageDataToBeCleared = new HashMap<>();
    private Map<String, Map<String, List<String>>> queueStorageDataToBeCleared = new HashMap<>();
    private Map<Map<String, String>, List<String>> serviceBusSubscriptionDataToBeCleared = new HashMap<>();

    @Autowired
    public ClearDownSteps(EndpointManager endpointManager) {

        clearDown = new ClearDown(endpointManager);
    }

    @Given("I have the following MongoDB collection test data to clear down:")
    public void runMongoClearDownBeforeScenario(DataTable dataTable) {

        mongoDataToBeCleared = clearDown.createMapOfDataFromFourColumns(dataTable);
        System.out.println("Mongo Data to be cleared: " + mongoDataToBeCleared);
        clearDown.runClearDownForMongoDB(mongoDataToBeCleared);
    }

    @Given("I have the following SFTP hosts, their path tags and respective test data to clear down:")
    public void runSFTPClearDownMultipleHostsBeforeScenario(DataTable dataTable) {

        sftpDataMultipleHostsToBeCleared = clearDown.createMapOfDataFromThreeColumns(dataTable);
        clearDown.runClearDownForSFTP(sftpDataMultipleHostsToBeCleared);
    }

    @Given("I have the following Service Bus Subscription test data to clear down:")
    public void runSBSubscriptionClearDownBeforeScenario(DataTable dataTable) {

        serviceBusSubscriptionDataToBeCleared = clearDown.createMapOfDataFromThreeColumnsKeyPairAsKey(dataTable);
        clearDown.runClearDownForServiceBus(serviceBusSubscriptionDataToBeCleared);
    }

    @Given("I have the following Blob Storage test data to clear down:")
    public void runBlobStorageClearDownBeforeScenario(DataTable dataTable) {

        blobStorageDataToBeCleared = clearDown.createMapOfDataFromThreeColumns(dataTable);
        clearDown.runClearDownForBlobStorage(blobStorageDataToBeCleared);
    }

    @Given("I have the following Queue Storage test data to clear down:")
    public void runQueueStorageClearDownBeforeScenario(DataTable dataTable) {

        queueStorageDataToBeCleared = clearDown.createMapOfDataFromThreeColumns(dataTable);
        clearDown.runClearDownForQueueStorage(queueStorageDataToBeCleared);
    }

    @After
    public void runClearDownAfterScenario() {

        logger.info("Running Clear Down After Scenario...");

        if (!mongoDataToBeCleared.isEmpty()) {
            clearDown.runClearDownForMongoDB(mongoDataToBeCleared);
        }

        if (!blobStorageDataToBeCleared.isEmpty()) {
            clearDown.runClearDownForBlobStorage(blobStorageDataToBeCleared);
        }

        if (!serviceBusSubscriptionDataToBeCleared.isEmpty()) {
            clearDown.runClearDownForServiceBus(serviceBusSubscriptionDataToBeCleared);
        }

        if(!sftpDataMultipleHostsToBeCleared.isEmpty()) {
            clearDown.runClearDownForSFTP(sftpDataMultipleHostsToBeCleared);
            clearDown.clearDownSftpOpenConnections();
        }

        if (!queueStorageDataToBeCleared.isEmpty()) {
            clearDown.runClearDownForQueueStorage(queueStorageDataToBeCleared);
        }

    }

}

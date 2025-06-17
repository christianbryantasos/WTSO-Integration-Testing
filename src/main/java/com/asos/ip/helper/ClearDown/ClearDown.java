package com.asos.ip.helper.ClearDown;

import com.asos.ip.config.EndpointManager;
import com.asos.ip.helper.BlobStorage.BlobStorageConnection;
import com.asos.ip.helper.BlobStorage.BlobStorageController;
import com.asos.ip.helper.Constants.FrameworkConstants;
import com.asos.ip.helper.JSONTools.JSONTools;
import com.asos.ip.helper.QueueStorage.QueueStorageController;
import com.asos.ip.helper.SFTP.SFTPConnection;
import com.asos.ip.helper.SFTP.SFTPController;
import com.asos.ip.helper.ServiceBus.ServiceBusConnection;
import com.asos.ip.helper.ServiceBus.ServiceBusController;
import com.asos.ip.helper.TestData.TestDataHelper;
import com.asos.ip.helper.mongoDB.MongoDBController;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import com.mongodb.client.MongoCollection;
import io.cucumber.datatable.DataTable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ClearDown {

    private final EndpointManager endpointManager;
    private static final TestDataHelper testDataHelper = new TestDataHelper();
    private static final Logger logger = LoggerFactory.getLogger(ClearDown.class);

    @Autowired
    public ClearDown(EndpointManager endpointManager) {
        this.endpointManager = endpointManager;
    }

    public Map<Map<String, String>, List<String>> createMapOfDataFromThreeColumnsKeyPairAsKey(DataTable dataTable) {
        Map<Map<String, String>, List<String>> dataToBeCleared = new HashMap<>();

        List<List<String>> rows = dataTable.asLists(String.class);

        for (List<String> row : rows.subList(0, rows.size())) {
            String container = row.get(0); // First column
            String subgroup = row.get(1);   // Second column
            String id = row.get(2);         // Third column

            // If the endpoint already exists, append the new ID to its list
            dataToBeCleared.computeIfAbsent(Map.of(container, subgroup), k -> new ArrayList<>())
                    .add(id);
        };

        logger.info("Following Data to be cleared: {}", dataToBeCleared);
        return dataToBeCleared;
    }

    public Map<String, Map<String, List<String>>> createMapOfDataFromThreeColumns(DataTable dataTable) {
        // The Container-<List of Strings to be cleared> data from the Two Column method, are given their own key - your Database to clear
        Map<String, Map<String, List<String>>> dataToBeCleared = new HashMap<>();

        List<List<String>> rows = dataTable.asLists(String.class);

        for (List<String> row : rows.subList(0, rows.size())) {
            String container = row.get(0); // First column
            String subgroup = row.get(1);   // Second column
            String id = row.get(2);         // Third column

            // If the endpoint already exists, append the new ID to its list
            dataToBeCleared
                    .computeIfAbsent(container, k -> new HashMap<>())
                    .computeIfAbsent(subgroup, k -> new ArrayList<>())
                    .add(id);
        };
        logger.info("Following Data to be cleared: {}", dataToBeCleared);
        return dataToBeCleared;
    }

    public Map<String, Map<String, Map<String, List<String>>>> createMapOfDataFromFourColumns(DataTable dataTable) {
        // The Container-<List of Strings to be cleared> data from the Two Column method, are given their own key - your Database to clear, and then broken further into separate containers representing different servers
        Map<String, Map<String, Map<String, List<String>>>> dataToBeCleared = new HashMap<>();

        List<List<String>> rows = dataTable.asLists(String.class);

        for (List<String> row : rows.subList(0, rows.size())) {
            String server = row.get(0); // First column
            String container = row.get(1); // Second column
            String subgroup = row.get(2);   // Third column
            String id = row.get(3);         // Fourth column

            // If the endpoint already exists, append the new ID to its list
            dataToBeCleared
                    .computeIfAbsent(server, k -> new HashMap<>())
                    .computeIfAbsent(container, k -> new HashMap<>())
                    .computeIfAbsent(subgroup, k -> new ArrayList<>())
                    .add(id);
        };
        return dataToBeCleared;
    }

    public void runClearDownForSFTP(Map<String, Map<String, List<String>>> dataSet) {

        System.out.println("Running SFTP Clear Down...");

        for (String sftpHostName : dataSet.keySet()) {
            logger.info("Now running clear down against SFTP Host: " + sftpHostName);

            SFTPConnection sftpConnection = endpointManager.getSftpConnectionManager().setActiveSFTPConnection(sftpHostName);

            for (String sftpPathTag : dataSet.get(sftpHostName).keySet()) {
                logger.info("Running clear down for remote path: " + sftpPathTag);
                sftpConnection.setActiveRemotePath(sftpPathTag);

                for (String id : dataSet.get(sftpHostName).get(sftpPathTag)) {
                    logger.info("Clearing down SFTP Path with ID: {}", id);
                    try {
                        clearDownSFTP(sftpConnection, id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            sftpConnection.closeSftpConnection();
        }
    }

    public void runClearDownForMongoDB(Map<String, Map<String, Map<String, List<String>>>> dataSet) {

        logger.info("Running MongoDB Clear Down...");

        for(String mongoServerName : dataSet.keySet()) {
            logger.info("Now running cleardown against MongoDB Server: {}", mongoServerName);

            endpointManager.getMongoDBConnectionManager().setActiveMongoConnection(mongoServerName);

            for (String mongoDatabaseName : dataSet.get(mongoServerName).keySet()) {
                logger.info("Now running cleardown against MongoDB Database: {}", mongoDatabaseName);

                for (String mongoCollectionName : dataSet.get(mongoServerName).get(mongoDatabaseName).keySet()) {
                    logger.info("Now running cleardown against MongoDB Collection: {}", mongoCollectionName);
                    MongoCollection<Document> mongoDBCollection = endpointManager.getMongoDBConnectionManager().getActiveMongoCollection(mongoDatabaseName, mongoCollectionName);

                    for (String id : dataSet.get(mongoServerName).get(mongoDatabaseName).get(mongoCollectionName)) {
                        logger.info("Clearing down MongoDB file with ID {} from Mongo Server {}, database {} inside collection: {}", id, mongoServerName, mongoDatabaseName, mongoCollectionName);
                        clearDownMongoDB(mongoDBCollection, id);
                    }
                }
            }
        }
    }

    public void runClearDownForServiceBus(Map<Map<String, String>, List<String>> dataSet) {

        System.out.println("Running Service Bus Subscription Clear Down...");

        dataSet.forEach((key, valueList) -> {

            Map.Entry<String, String> entry = key.entrySet().iterator().next();

            ServiceBusConnection serviceBusConnection = endpointManager.getServiceBusConnectionManager().setActiveServiceBusSubscription(entry.getKey(), entry.getValue());

            ServiceBusController serviceBusController = new ServiceBusController(serviceBusConnection);

            valueList.forEach(messageToClear -> {
                logger.info("Clearing down Service Bus Subscription with message: {}", messageToClear);
                try {
                    clearDownServiceBus(serviceBusController, messageToClear);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

    }

    public void runClearDownForBlobStorage(Map<String, Map<String, List<String>>> dataSet) {

        System.out.println("Running Blob Storage Clear Down...");

        for (String blobStorageAccount : dataSet.keySet()) {
            logger.info("Now accessing Blob Database: {}", blobStorageAccount);
            BlobStorageConnection blobStorageConnection = endpointManager.getBlobStorageConnectionManager().setActiveBlobStorageConnection(blobStorageAccount);

            for (String blobContainerName : dataSet.get(blobStorageAccount).keySet()) {
                logger.info("Now accessing Blob Container: {}", blobContainerName);
                BlobContainerClient blobContainerClient = blobStorageConnection.getBlobContainerClient(blobContainerName);

                logger.info("Clearing down data {} from Container {}",dataSet.get(blobStorageAccount).get(blobContainerName), blobContainerName);
                clearDownBlobStorage(blobContainerClient, dataSet.get(blobStorageAccount).get(blobContainerName));
            }
        }
    }

    public void clearDownMongoDB(MongoCollection<Document> mongoDBCollection, String testDocumentID) {

        MongoDBController mongoDBController = new MongoDBController(mongoDBCollection);
        mongoDBController.deleteFile(testDocumentID);
    }

    public void clearDownSFTP(SFTPConnection sftpConnection, String filePath) throws IOException {
        SFTPController sftpController = new SFTPController(sftpConnection);
        sftpController.deleteDataFromSFTP(filePath);
    }

    public void clearDownServiceBus(ServiceBusController serviceBusController, String clearDownScope) throws IOException {

        if(clearDownScope.equals("completeAllMessagesInSubscription")) {
            serviceBusController.receiveMessage().forEach((message, messageBody) -> {
                serviceBusController.deleteMessage(message);

            });
        }  if (clearDownScope.contains(".json")) {

            JSONObject messageToClearDownJSON = JSONTools.getJSONFromResources(FrameworkConstants.DEFAULT_RESOURCES_OUTPUT_DIRECTORY + clearDownScope);

            List<ServiceBusReceivedMessage> messagesToClear = serviceBusController.receiveTargetMessagesFromSubscriptionMatchingExpectedJson(messageToClearDownJSON, "", "");
            for (ServiceBusReceivedMessage message : messagesToClear) {
                serviceBusController.deleteMessage(message);
                logger.info("Completed message with message: {}", messageToClearDownJSON.toString());
            };
        } else {
            List<ServiceBusReceivedMessage> messagesToclear = serviceBusController.receiveTargetMessagesFromSubscriptionMatchingBodyContent(clearDownScope, null);
            for (ServiceBusReceivedMessage message : messagesToclear) {
                serviceBusController.deleteMessage(message);
                logger.info("Completed message with message: {}", message.getBody());
            }
        }
    }

    public void clearDownBlobStorage(BlobContainerClient blobContainerClient, List<String> valuesToRemove) {

        BlobStorageController blobStorageController = new BlobStorageController(blobContainerClient);

        for (String valueToRemove : valuesToRemove) {

            valueToRemove = testDataHelper.returnStringWithFormatedDateTime(valueToRemove);

            String heirachySearchString = TestDataHelper.splitStringAtLastSlashReturnAsArray(valueToRemove)[0];

            searchBlobAndDelete(blobStorageController, heirachySearchString, valueToRemove);
        }

    }

    private void searchBlobAndDelete(BlobStorageController blobStorageController, String heirachySearchString, String valueToRemove) {
        List<BlobItem> blobItems = blobStorageController.getBlobFilesMatchingHeirarchy(heirachySearchString);

        String fileSearchString = TestDataHelper.splitStringAtLastSlashReturnAsArray(valueToRemove)[1];
        List<BlobItem> blobItemsSearchContent = blobStorageController.searchListOfBlobItemsForMatchingContent(blobItems, fileSearchString);
        for (BlobItem blobItem : blobItemsSearchContent) {
                blobStorageController.deleteBlobFile(blobItem);
                blobItems.remove(blobItem);
                logger.info("Deleted Blob: {}", blobItem.getName());
        }
        blobItems.forEach(blobItem -> {
            if (blobStorageController.doesBlobExist(blobStorageController.returnBlob(valueToRemove))) {
               BlobClient blobClient = blobStorageController.returnBlob(valueToRemove);
               blobClient.deleteIfExists();
                logger.info("Deleted Blob: {}", blobClient.getBlobName());
            }
        });
    }

    public void runClearDownForQueueStorage(Map<String, Map<String, List<String>>> dataSet) {

        logger.info("Running Queue Storage Clear Down...");

        for (String queueStorageAccountName : dataSet.keySet()) {
            logger.info("Now accessing Queue Storage Account: {}", queueStorageAccountName);
            endpointManager.getQueueStorageConnectionManager().setActiveQueueStorageConnection(queueStorageAccountName);

            for (String queueName : dataSet.get(queueStorageAccountName).keySet()) {
                logger.info("Now accessing Queue Storage Queue: {}", queueName);
                QueueClient queueClient = endpointManager.getQueueStorageConnectionManager().returnActiveQueueClient(queueName);
                clearDownQueueStorage(queueClient, dataSet.get(queueStorageAccountName).get(queueName));
            }
        }
    }

    public void clearDownQueueStorage(QueueClient queueClient, List<String> valuesToRemove) {

        List<QueueMessageItem> messagesToCleanUp = new ArrayList<>();
        QueueStorageController queueStorageController = new QueueStorageController(queueClient);

        for (String valueToRemove : valuesToRemove) {
            messagesToCleanUp = queueStorageController.receiveAndReturnMatchingQueueMessageItems(valueToRemove);
        }

        for (QueueMessageItem message : messagesToCleanUp) {
            queueStorageController.deleteMessage(queueClient, message);
        }
    }

    public void clearDownSftpOpenConnections() {

        logger.info("Disconnecting from SFTP");
        endpointManager.getSftpConnectionManager().disconnectAllSftpConnections();
    }
}

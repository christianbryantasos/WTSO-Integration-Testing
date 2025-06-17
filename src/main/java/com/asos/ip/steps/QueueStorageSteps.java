package com.asos.ip.steps;

import com.asos.ip.config.EndpointManager;
import com.asos.ip.helper.QueueStorage.QueueStorageConnectionManager;
import com.asos.ip.helper.QueueStorage.QueueStorageController;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.PeekedMessageItem;
import io.cucumber.java.en.Then;
import java.util.List;

import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class QueueStorageSteps {

    private final QueueStorageConnectionManager queueStorageConnectionManager;
    private QueueStorageController queueStorageController;


    @Autowired
    public QueueStorageSteps(EndpointManager endpointManager) {
        this.queueStorageConnectionManager = endpointManager.getQueueStorageConnectionManager();
    }

    @When("I send a message with the string {string} to the queue {string} in the queue storage account {string}")
    public void uploadMessageToQueue(String messageContent, String queueName, String queueStorageAccountName) {

        queueStorageController = returnQueueStorageController(queueStorageAccountName, queueName);

        queueStorageController.sendMessage(messageContent);
    }

    @Then("A new message containing the string {string} should be found in the queue {string} in the queue storage account {string}")
    public void checkMessageInQueueMatchesExceptionContent(String expectedMessageContent, String queueName, String queueStorageAccountName) {

        queueStorageController = returnQueueStorageController(queueStorageAccountName, queueName);
        List<PeekedMessageItem> messagesMatchingExpectedContent = queueStorageController.peekAndReturnMatchingPeekedMessageItems(expectedMessageContent);

        assertFalse(messagesMatchingExpectedContent.isEmpty());
    }

    public QueueStorageController returnQueueStorageController(String queueStorageAccountName, String queueName) {
        queueStorageConnectionManager.setActiveQueueStorageConnection(queueStorageAccountName);
        QueueClient activeQueueClient = queueStorageConnectionManager.returnActiveQueueClient(queueName);

        return new QueueStorageController(activeQueueClient);
    }
}




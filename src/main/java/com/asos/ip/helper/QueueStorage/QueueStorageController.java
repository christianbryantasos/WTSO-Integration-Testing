package com.asos.ip.helper.QueueStorage;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueStorageException;
import com.azure.core.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


/**
 * Helper class for interacting with Azure Queue Storage.
 * This class provides methods for connecting to Queue Storage,
 * managing queues, and comparing the contents of the queue messages with test data.
 */

public class QueueStorageController {

    private static final Logger logger = LoggerFactory.getLogger(QueueStorageController.class);

    private QueueClient queueClient;

    public QueueStorageController(QueueClient queueClient) {
        this.queueClient = queueClient;
    }


    public void deleteMessage(QueueClient queueClient, QueueMessageItem queueMessage) {

        String messageId = queueMessage.getMessageId();
        String popReceipt = queueMessage.getPopReceipt();

        if (messageId == null || popReceipt == null) {
            logger.info("Message ID or Pop Receipt is null");
            return;
        }

        queueClient.deleteMessage(messageId, popReceipt);
        logger.info("Message deleted successfully.");
    }

    public void sendMessage(String message) {
        queueClient.sendMessage(message);
    }

    public List<PeekedMessageItem> peekAndReturnMatchingPeekedMessageItems(String searchString) {

        List<PeekedMessageItem> matchingMessageItems = new ArrayList<>();
        int maxMessages = 100;
        int messagesChecked = 0;

        try {
            while (messagesChecked < maxMessages) {

                Iterable<PeekedMessageItem> peekedMessages = queueClient.peekMessages(32, Duration.ofSeconds(10), Context.NONE).stream().toList();
                int batchSize = 0;

                for (PeekedMessageItem message : peekedMessages) {
                    batchSize++;
                    messagesChecked++;

                    String messageContent = message.getBody().toString();
                    if (messageContent.contains(searchString)) {
                        matchingMessageItems.add(message);
                    }
                    byte[] decodedBytes = Base64.getDecoder().decode(messageContent);
                    String decodedString = new String(decodedBytes);
                    if (decodedString.contains(searchString)) {
                        matchingMessageItems.add(message);
                    }

                    if (messagesChecked >= maxMessages) {
                        break;
                    }
                }

                if (batchSize < 32) {
                    break;
                }
            }
        } catch (QueueStorageException e) {
            logger.error("Error attempting to peek messages from the queue: {}", e.getMessage());
            throw new QueueStorageException("Queue not found", e.getResponse(), null);
        }
        logger.info("peeked {} message(s)", messagesChecked);
        logger.info("found {} matching peeked message(s)", matchingMessageItems.size());

        return matchingMessageItems;
    }

    public List<QueueMessageItem> receiveAndReturnMatchingQueueMessageItems(String searchString) {

        List<QueueMessageItem> matchingMessageItems = new ArrayList<>();
        int maxMessages = 100;
        int messagesChecked = 0;

        try {
            while (messagesChecked < maxMessages) {

                Iterable<QueueMessageItem> messages = queueClient.receiveMessages(32, Duration.ofSeconds(1), Duration.ofSeconds(10), Context.NONE);
                int batchSize = 0;

                for (QueueMessageItem message : messages) {
                    batchSize++;
                    messagesChecked++;

                    String messageContent = message.getBody().toString();
                    if (messageContent.contains(searchString)) {
                        matchingMessageItems.add(message);
                    }

                    byte[] decodedBytes = Base64.getDecoder().decode(messageContent);
                    String decodedString = new String(decodedBytes);
                    if (decodedString.contains(searchString)) {
                        matchingMessageItems.add(message);
                    }

                    if (messagesChecked >= maxMessages) {
                        break;
                    }
                }

                if (batchSize < 32) {
                    break;
                }
            }
        } catch (QueueStorageException e) {
            logger.error("Error attempting to receive messages from the queue: {}", e.getMessage());
            throw new QueueStorageException("Queue not found", e.getResponse(), null);
        }
        logger.info("checked {} message(s)", messagesChecked);
        logger.info("found {} matching message(s)", matchingMessageItems.size());

        return matchingMessageItems;
    }
}

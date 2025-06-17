package com.asos.ip.helper.ServiceBus;

import com.asos.ip.helper.TestData.TestDataHelper;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for interacting with Azure Service Bus.
 * This class provides methods to connect to Service Bus topics, send and receive messages,
 * and manage message processing.
 */
public class ServiceBusController {
    
    private ServiceBusReceiverClient receiverServiceBusConnection;
    private ServiceBusSenderClient senderServiceBusConnection;

    private static final Logger logger = LoggerFactory.getLogger(ServiceBusController.class);

    /*
        * Constructor for ServiceBusHelper class for connecting to a Service Bus queue.
        *
        * @param connectionString The connection string for the Service Bus.
        * @param queueName The name of the queue to connect to.
     */

    public ServiceBusController(ServiceBusConnection serviceBusConnection) {

        this.receiverServiceBusConnection = serviceBusConnection.getServiceBusReceiverConnection();
        this.senderServiceBusConnection = serviceBusConnection.getServiceBusSenderConnection();
    }

    /**
     * Creates a Service Bus message from a string.
     *
     * @param message The message content to be sent.
     * @return A ServiceBusMessage instance.
     */
    public ServiceBusMessage createMessage(String message) {
        logger.debug("Creating message for Service Bus: {}", message);
        return new ServiceBusMessage(message);
    }

    /**
     * Sends a message to the specified Service Bus topic.
     *
     * @param message      The message content to be sent.
     */
    public void sendMessage(String message) {
        try {
            logger.info("Sending message to Service Bus: {}", message);
            senderServiceBusConnection.sendMessage(createMessage(message));
            logger.info("Message sent to Service Bus: {}", message);
        } catch (Exception e) {
            logger.error("Error sending message to Service Bus: {}", message, e);
        }
    }

    /**
     * Receives messages from the Service Bus and returns them as a map.
     *
     * @return A map of received messages and their string content.
     */

    public Map<ServiceBusReceivedMessage, String> receiveMessage() {
        logger.info("Receiving messages from Service Bus");
        Map<ServiceBusReceivedMessage, String> messages = new HashMap<>();
        try {
            receiverServiceBusConnection.receiveMessages(100, Duration.ofSeconds(10)).forEach(message -> {
                logger.info("Received message: {}", message.getBody());
                messages.put(message, message.getBody().toString());
            });
        } catch (Exception e) {
            logger.error("Error receiving messages from Service Bus", e);
        }
        return messages;
    }

    /**
     * Deletes a specific message from the Service Bus.
     *
     * @param message      The message to be deleted.
     */
    public void deleteMessage(ServiceBusReceivedMessage message) {
        try {
            logger.info("Deleting message with ID: {}", message.getMessageId());
            receiverServiceBusConnection.complete(message);
            logger.info("Message deleted with ID: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Error deleting message with ID: {}", message.getMessageId(), e);
        }
    }

    public List<ServiceBusReceivedMessage> receiveTargetMessagesFromSubscriptionContentAndMessageProperties(
                                                                                 String targetContent,
                                                                                 String targetMessageField,
                                                                                 String targetPropertyValue,
                                                                                 String targetProperty) {
        logger.info("Receiving and filtering Target messages from Service Bus Subscription");

        ObjectMapper objectMapper = new ObjectMapper();
        List<ServiceBusReceivedMessage> filteredMessages = new ArrayList<>();

        try {
            receiverServiceBusConnection.receiveMessages(1000, Duration.ofSeconds(5)).forEach(message -> {
                try {
                    String messageBody = getMessageBody(message);
                    JsonNode url = objectMapper.readTree(messageBody).get(targetMessageField);
                    String urlString;

                    if (url == null) {
                        return;
                    } else {
                        urlString = objectMapper.readTree(messageBody).get(targetMessageField).asText();
                    }

                    Map<String, Object> customProperties = message.getApplicationProperties();

                    if (urlString.contains(targetContent) && targetPropertyValue.equals(customProperties.get(targetProperty))) {
                        filteredMessages.add(message);
                        logger.info("Received Target message with body: {}", messageBody);
                    }
                } catch (Exception e) {
                    logger.error("Error processing message", e);
                }
            });
        } catch (Exception e) {
            logger.error("Error receiving messages from Service Bus", e);
        }

        return filteredMessages;
    }

    public List<ServiceBusReceivedMessage> receiveTargetMessagesFromSubscriptionMatchingBodyContent(String targetContentValue, String targetContentKey) {
        logger.info ("Receiving and filtering messages from target Service Bus Subscription");

        List<ServiceBusReceivedMessage> filteredMessages = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        TestDataHelper testDataHelper = new TestDataHelper();

        String targetContentValueChecked = testDataHelper.returnStringWithFormatedDateTime(targetContentValue);

        logger.info("Searching Service Bus Subscription for messages with body containing: {}", targetContentValueChecked);

        try {
            receiverServiceBusConnection.receiveMessages(1000, Duration.ofSeconds(5)).forEach(message -> {
                try {
                    String messageBody = getMessageBody(message);
                    String url = messageBody;

                    if (targetContentKey == null) {

                        logger.info("Key is empty, searching whole message body...");
                    } else {
                         url = objectMapper.readTree(messageBody).get(targetContentKey).asText();
                    }

                    if (url.contains(targetContentValueChecked)) {
                        filteredMessages.add(message);
                        logger.info("Found matching target message with body: {}", messageBody);
                    }
                } catch (Exception e) {
                    logger.error("Error processing message", e);
                }
            });
        } catch (Exception e) {
            logger.error("Error receiving messages from Service Bus", e);
        }

        logger.info("Found {} target messages with body containing: {}", filteredMessages.size(), targetContentValueChecked);
        return filteredMessages;

    }

    public List<ServiceBusReceivedMessage> receiveTargetMessagesFromSubscriptionMatchingExpectedJson(JSONObject targetJson, String targetProperty, String targetPropertyValue) {

        List<ServiceBusReceivedMessage> filteredMessages = new ArrayList<>();

        try {
            receiverServiceBusConnection.receiveMessages(1000, Duration.ofSeconds(5)).forEach(message -> {
                try {
                    JSONObject messageBodyJsonObject = getMessageBodyJsonObject(message);
                    Map<String, Object> customProperties = message.getApplicationProperties();

                    if(targetProperty.equals("")) {
                        if (messageBodyJsonObject.similar(targetJson)) {
                            filteredMessages.add(message);
                            logger.info("Received Target message with JSON body: {}", messageBodyJsonObject);
                        }
                    } else {
                        if (messageBodyJsonObject.similar(targetJson) && customProperties.get(targetProperty).equals(targetPropertyValue)) {
                            filteredMessages.add(message);
                            logger.info("Received Target message with JSON body: {}", messageBodyJsonObject);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing message", e);
                }
            });
        } catch (Exception e) {
            logger.error("Error receiving messages from Service Bus", e);
        }

        return filteredMessages;
    }

    public List<JSONObject> receiveMessagesAsJsonObjects() {
        List<JSONObject> messagesAsJsonObjects = new ArrayList<>();

        receiverServiceBusConnection.receiveMessages(1000, Duration.ofSeconds(5)).forEach(message -> {
            JSONObject messageBodyJsonObject = getMessageBodyJsonObject(message);
            messagesAsJsonObjects.add(messageBodyJsonObject);
        });

        return messagesAsJsonObjects;
    }

    public List<ServiceBusReceivedMessage> getTargetErrorMessagesFromQueue(String expectedErrorMessageBody) {
        logger.info("Receiving and filtering messages from target Service Bus Queue");

        List<ServiceBusReceivedMessage> targetErrorMessages = new ArrayList<>();

        try {
            receiverServiceBusConnection.receiveMessages(10, Duration.ofSeconds(5)).forEach(message -> {
                try {
                    String messageBody = message.getBody().toString();

                    if (expectedErrorMessageBody.equals(messageBody)) {
                        targetErrorMessages.add(message);
                        logger.info("received target message: {}", messageBody);
                    }
                } catch (Exception e) {
                    logger.error("Error processing message", e);
                }
            });
        } catch (Exception e) {
            logger.error("Error receiving messages from Service Bus", e);
        }
        return targetErrorMessages;
    }

    /**
     * Closes a specific ServiceBusReceiverClient
     *
     * @param receiverClient The ServiceBusReceiverClient to close
     */
    public void closeServiceBusReceiverClient(ServiceBusReceiverClient receiverClient) {
        receiverClient.close();
    }

    private static String getMessageBody(ServiceBusReceivedMessage message) {
        String messageBody = null;
        if (message.getRawAmqpMessage().getBody().getBodyType() == AmqpMessageBodyType.DATA) {
            messageBody = message.getBody().toString();
        } else if (message.getRawAmqpMessage().getBody().getBodyType() == AmqpMessageBodyType.VALUE) {
            messageBody = message.getRawAmqpMessage().getBody().getValue().toString();
        }
        return messageBody;
    }

    private static JSONObject getMessageBodyJsonObject(ServiceBusReceivedMessage message) {
        AmqpMessageBodyType bodyType = message.getRawAmqpMessage().getBody().getBodyType();
        String messageBodyJsonString = null;

        if (bodyType == AmqpMessageBodyType.DATA) {
            messageBodyJsonString = message.getBody().toString();
        }

        if (bodyType == AmqpMessageBodyType.VALUE) {
            messageBodyJsonString = message.getRawAmqpMessage().getBody().getValue().toString();
        }

        try {
            JSONObject messageBodyAsJson = new JSONObject(messageBodyJsonString);
            return messageBodyAsJson;
        } catch (Exception e) {
            logger.error("Error converting message body to JSON: {}", e.getMessage(), e);
            return null;
        }
    }
}

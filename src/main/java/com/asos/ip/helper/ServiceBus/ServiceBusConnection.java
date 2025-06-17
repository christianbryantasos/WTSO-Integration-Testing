package com.asos.ip.helper.ServiceBus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class ServiceBusConnection {
    private static final Logger logger = LoggerFactory.getLogger(ServiceBusConnection.class);

    private ServiceBusReceiverClient serviceBusReceiverConnection;
    private ServiceBusSenderClient serviceBusSenderConnection;

    ServiceBusConnection(String connectionString, String queue) {
        this.serviceBusReceiverConnection = connectToServiceBusReceiverForQueue(connectionString, queue);

    }

    ServiceBusConnection(String connectionString, String topic, String subscription) {
        this.serviceBusReceiverConnection = connectToServiceBusReceiverForSubscription(connectionString, topic, subscription);
        this.serviceBusSenderConnection = connectToServiceBusSenderForSubscription(connectionString, topic);
    }

    /**
     * Connects to a Service Bus topic for sending messages.
     *
     * @param connectionString The connection string for the Service Bus.
     * @param topicName        The name of the topic to connect to.
     * @return A ServiceBusSenderClient instance for sending messages, or null if connection fails.
     */
    public ServiceBusSenderClient connectToServiceBus(String connectionString, String topicName) {
        logger.info("Connecting to Service Bus topic: {}", topicName);
        try {
            ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .sender()
                    .topicName(topicName)
                    .buildClient();
            logger.info("Successfully connected to Service Bus topic: {}", topicName);
            return senderClient;
        } catch (Exception e) {
            logger.error("Error connecting to Service Bus topic: {}", topicName, e);
            return null;
        }
    }

    /**
     * Connects to a Service Bus topic for receiving messages.
     *
     * @param connectionString The connection string for the Service Bus.
     * @param topicName        The name of the topic to connect to.
     * @param subscriptionName The name of the subscription for the topic.
     * @return A ServiceBusReceiverClient instance for receiving messages, or null if connection fails.
     */
    public ServiceBusReceiverClient connectToServiceBusReceiverForSubscription(String connectionString, String topicName, String subscriptionName) {
        logger.info("Connecting to Service Bus topic: {} with subscription: {}", topicName, subscriptionName);
        try {
            serviceBusReceiverConnection = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .receiver()
                    .topicName(topicName)
                    .subscriptionName(subscriptionName)
                    .maxAutoLockRenewDuration(Duration.ZERO)
                    .buildClient();
            logger.info("Successfully connected to Service Bus topic: {} with subscription: {}", topicName, subscriptionName);
            return serviceBusReceiverConnection;
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to Service Bus topic: " + topicName + " with subscription: " + subscriptionName, e);
        }
    }

    public ServiceBusSenderClient connectToServiceBusSenderForSubscription(String connectionString, String topicName) {
        logger.info("Connecting to Service Bus topic: {} with subscription: {}", topicName);
        try {
            ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .sender()
                    .topicName(topicName)
                    .buildClient();
            logger.info("Successfully connected to Service Bus topic: {}", topicName);
            return senderClient;
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to Service Bus topic: " + topicName + e);
        }
    }

    /**
     * Connects to a Service Bus queue for receiving messages.
     *
     * @param connectionString The connection string for the Service Bus.
     * @param queueName        The name of the queue to connect to.
     * @return
     */
    public ServiceBusReceiverClient connectToServiceBusReceiverForQueue(String connectionString, String queueName) {
        logger.info("Connecting to Service Bus queue: {}", queueName);

        try {
            serviceBusReceiverConnection = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .receiver()
                    .queueName(queueName)
                    .receiveMode(ServiceBusReceiveMode.PEEK_LOCK) // or RECEIVE_AND_DELETE
                    .buildClient();

            logger.info("Successfully connected to Service Bus queue: {}", queueName);
            return serviceBusReceiverConnection;
        } catch (Exception e) {
            logger.error("Error connecting to Service Bus queue: {}", queueName, e);
            return null;
        }
    }

    public ServiceBusReceiverClient getServiceBusReceiverConnection() {
        return serviceBusReceiverConnection;
    }
    public ServiceBusSenderClient getServiceBusSenderConnection() {
        return serviceBusSenderConnection;
    }
}

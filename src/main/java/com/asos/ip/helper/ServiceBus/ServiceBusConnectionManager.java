package com.asos.ip.helper.ServiceBus;

import com.asos.ip.config.LoadConfigurations;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ServiceBusConnectionManager {

    final Properties properties;

    ServiceBusConnection activeServiceBusConnectionSubscription;
    ServiceBusConnection activeServiceBusConnectionQueue;
    HashMap<Map<String, String>,  ServiceBusConnection> serviceBusSubscriptionConnections = new HashMap<>();
    HashMap<String, ServiceBusConnection> serviceBusQueueConnections = new HashMap<>();

    public ServiceBusConnectionManager(LoadConfigurations loadConfigurations) {
        this.properties = loadConfigurations.getConfigProperties();
    }

    public ServiceBusConnection connectToServiceBusSubscription(String connectionName, String targetName) {

        String connectionStringPlaceholder = "azure.service.bus.{connectionName}.connection.string";
        String subscriptionPlaceHolder = "azure.service.bus.{targetName}.subscription";
        String topicPlaceHolder = "azure.service.bus.{targetName}.topic";

        String connectionString = properties.getProperty(connectionStringPlaceholder.replace("{connectionName}", connectionName));
        String subscription = properties.getProperty(subscriptionPlaceHolder.replace("{targetName}", targetName));
        String topic = properties.getProperty(topicPlaceHolder.replace("{targetName}", targetName));

        return new ServiceBusConnection(
                connectionString,
                topic,
                subscription);
    }


    /* TODO - Refactor service bus connector methods to use the same method with different parameters
     */
    public ServiceBusConnection setActiveServiceBusSubscription(String connection, String subscription) {

        subscription = subscription.toLowerCase();
        connection = connection.toLowerCase();

        if (serviceBusSubscriptionConnections.containsKey(Map.of(connection, subscription))) {
            activeServiceBusConnectionSubscription = serviceBusSubscriptionConnections.get(Map.of(connection, subscription));
            return activeServiceBusConnectionSubscription;
        }
        else {
            serviceBusSubscriptionConnections.put(Map.of(connection, subscription), connectToServiceBusSubscription(connection, subscription));
            activeServiceBusConnectionSubscription = serviceBusSubscriptionConnections.get(Map.of(connection, subscription));
        }
        return activeServiceBusConnectionSubscription;
    }

    public ServiceBusConnection setActiveServiceBusQueue(String queue) {

            String queueLowerCase = queue.toLowerCase();

            if (serviceBusQueueConnections.containsKey(queueLowerCase)) {
                activeServiceBusConnectionQueue = serviceBusQueueConnections.get(queueLowerCase);
                return activeServiceBusConnectionQueue;
            }
            else {
                serviceBusQueueConnections.put(queueLowerCase, connectToServiceBusQueue(queue));
                activeServiceBusConnectionQueue = serviceBusQueueConnections.get(queueLowerCase);
            }
            return activeServiceBusConnectionQueue;
    }

    private ServiceBusConnection connectToServiceBusQueue(String queue) {

            String queuePlaceHolder = "azure.service.bus.{queue}.queue";

            String queueName = properties.getProperty(queuePlaceHolder.replace("{queue}", queue));

            return new ServiceBusConnection(
                    properties.getProperty("azure.service.bus.connection.string"),
                    queueName);
    }

}

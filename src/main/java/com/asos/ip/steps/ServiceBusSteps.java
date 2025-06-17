package com.asos.ip.steps;

import com.asos.ip.config.EndpointManager;
import com.asos.ip.helper.Constants.FrameworkConstants;
import com.asos.ip.helper.JSONTools.JSONTools;
import com.asos.ip.helper.ServiceBus.ServiceBusConnection;
import com.asos.ip.helper.ServiceBus.ServiceBusConnectionManager;
import com.asos.ip.helper.ServiceBus.ServiceBusController;
import com.asos.ip.helper.TestData.TestDataHelper;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import io.cucumber.java.en.When;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;

public class ServiceBusSteps {

    ServiceBusConnectionManager serviceBusConnectionManager;

    ServiceBusConnection activeServiceBusSubscription;
    ServiceBusConnection activeServiceBusQueue;
    ServiceBusController serviceBusController;
    TestDataHelper testDataHelper = new TestDataHelper();



    public ServiceBusSteps(EndpointManager endpointManager) {
        this.serviceBusConnectionManager = endpointManager.getServiceBusConnectionManager();

    }

    @Then("Target subscription {string} in {string} service bus should contain {int} new messages, with {string} in the {string} field in body, and {string} as the {string} message property")
    public void checkExpectedMessagesReceived(String targetSubscription, String connectionName, int expectedNumberOfMessages, String expectedBodyValue, String targetMessageBodyField, String targetPropertyValue, String targetMessageProperty) {

        activeServiceBusSubscription = serviceBusConnectionManager.setActiveServiceBusSubscription(connectionName, targetSubscription);

        serviceBusController = new ServiceBusController(activeServiceBusSubscription);

        List<ServiceBusReceivedMessage> serviceBusReceivedMessages = serviceBusController.receiveTargetMessagesFromSubscriptionContentAndMessageProperties(expectedBodyValue, targetMessageBodyField, targetPropertyValue, targetMessageProperty);

        Assertions.assertFalse(
                serviceBusReceivedMessages.isEmpty(),
                "Expected to receive target messages from the Service Bus, but no messages were found."
        );

        assertEquals(expectedNumberOfMessages, serviceBusReceivedMessages.size());
    }

    @Then("Target subscription {string} in {string} service bus should contain {int} new messages, with {string} in the {string} field in body")
    public void checkLatestMessageMatchingBody(String targetSubscription, String connectionName, int expectedMessageNumber, String expectedBodyValue, String expectedKeyValue) {

        activeServiceBusSubscription = serviceBusConnectionManager.setActiveServiceBusSubscription(connectionName, targetSubscription);

        serviceBusController = new ServiceBusController(activeServiceBusSubscription);

        List<ServiceBusReceivedMessage> serviceBusReceivedMessages = serviceBusController.receiveTargetMessagesFromSubscriptionMatchingBodyContent(expectedBodyValue, expectedKeyValue);

        Assertions.assertFalse(
                serviceBusReceivedMessages.isEmpty(),
                "Expected to receive target messages from the Service Bus, but no messages were found."
        );

        assertEquals(expectedMessageNumber, serviceBusReceivedMessages.size());
    }

    @Then("Target subscription {string} in {string} should contain {int} message and the expected JSON output in file {string} should match the actual output, and {string} as the {string} message property")
    public void verifyJsonDataFromServiceBusSubscription(String targetSubscription, String connectionName, int expectedNumberOfMessages, String expectedJsonFileName, String expectedPropertyValue, String propertyName) throws IOException {

        activeServiceBusSubscription = serviceBusConnectionManager.setActiveServiceBusSubscription(connectionName, targetSubscription);
        serviceBusController = new ServiceBusController(activeServiceBusSubscription);


        JSONObject expectedJson = JSONTools.getJSONFromResources("Output/" + expectedJsonFileName);

        List<ServiceBusReceivedMessage> serviceBusReceivedMessages = serviceBusController.receiveTargetMessagesFromSubscriptionMatchingExpectedJson(expectedJson, propertyName, expectedPropertyValue);

        Assertions.assertFalse(
                serviceBusReceivedMessages.isEmpty(),
                "Expected to receive target messages from the Service Bus, but no messages were found."
        );

        assertEquals(expectedNumberOfMessages, serviceBusReceivedMessages.size());

    }

    @Then("Target subscription {string} in {string} should contain 1 new message matching the JSON body in file {string} ignoring fields {string}")
    public void verifyJsonDataFromServiceBusSubscriptionIgnoreFields(String targetSubscription, String connectionName, String expectedJsonFileName, String ignoredFields) throws IOException {

        activeServiceBusSubscription = serviceBusConnectionManager.setActiveServiceBusSubscription(connectionName, targetSubscription);
        serviceBusController = new ServiceBusController(activeServiceBusSubscription);

        String expectedOutputFilePath = FrameworkConstants.DEFAULT_RESOURCES_OUTPUT_DIRECTORY + expectedJsonFileName;

        List<JSONObject> listOfServiceBusMessagesJSON = serviceBusController.receiveMessagesAsJsonObjects();

        String[] fieldsToIgnore = testDataHelper.parseIgnoredFields(ignoredFields);

        JSONTools.assertJsonEquality(listOfServiceBusMessagesJSON.get(listOfServiceBusMessagesJSON.size()-1), expectedOutputFilePath, fieldsToIgnore);
    }

    @Then("Target error queue should contain {int} new message matching body {string}")
    public void checkExpectedErrorMessagesReceived(int expectedNumberOfMessages, String expectedMessageBody) {

        activeServiceBusQueue = serviceBusConnectionManager.setActiveServiceBusQueue("error");
        serviceBusController = new ServiceBusController(activeServiceBusSubscription);

        List<ServiceBusReceivedMessage> serviceBusReceivedMessages = serviceBusController.getTargetErrorMessagesFromQueue(expectedMessageBody);


        Assertions.assertFalse(
                serviceBusReceivedMessages.isEmpty(),
                "Expected to receive target messages from the Service Bus, but no messages were found."
        );

        assertEquals(expectedNumberOfMessages, serviceBusReceivedMessages.size());
    }


    @Then("Target error topic should contain {int} new messages, with {string} in the {string} field in the body, and {string} as the {string} message property")
    public void checkExpectedErrorMessagesReceivedInTopic(int expectedNumberOfMessages, String expectedBodyValue, String targetMessageBodyField,
                                                          String targetPropertyValue, String targetMessageProperty) {

        activeServiceBusQueue = serviceBusConnectionManager.setActiveServiceBusQueue("error");
        serviceBusController = new ServiceBusController(activeServiceBusSubscription);

        List<ServiceBusReceivedMessage> serviceBusReceivedMessages =
                serviceBusController.receiveTargetMessagesFromSubscriptionContentAndMessageProperties(expectedBodyValue, targetMessageBodyField,
                        targetPropertyValue, targetMessageProperty);

        Assertions.assertFalse(
                serviceBusReceivedMessages.isEmpty(),
                "Expected to receive target messages from the Service Bus, but no messages were found."
        );

        assertEquals(expectedNumberOfMessages, serviceBusReceivedMessages.size());
    }

    @When("I send a message to the {string} service bus topic {string} with the body in file {string}")
    public void sendMessagesToServiceBusTopic(String connectionName, String targetTopic, String messageBodyFileName) throws IOException {

        activeServiceBusSubscription = serviceBusConnectionManager.setActiveServiceBusSubscription(connectionName, targetTopic);
        serviceBusController = new ServiceBusController(activeServiceBusSubscription);

        String messageBody = JSONTools.getJSONFromResources(FrameworkConstants.DEFAULT_RESOURCES_INPUT_DIRECTORY + messageBodyFileName).toString();

        serviceBusController.sendMessage(messageBody);
    }
}

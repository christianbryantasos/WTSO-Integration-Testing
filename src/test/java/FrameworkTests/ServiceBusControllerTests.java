package FrameworkTests;

import com.asos.ip.helper.ServiceBus.ServiceBusConnection;
import com.asos.ip.helper.ServiceBus.ServiceBusController;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceBusControllerTests {

    @Mock
    ServiceBusConnection mockServiceBusConnection;

    @Mock
    ServiceBusReceiverClient mockServiceBusReceiverClient;

    @Mock
    ServiceBusSenderClient mockServiceBusSenderClient;

    @Mock
    ServiceBusReceivedMessage mockServiceBusReceivedMessage1;

    @Mock
    ServiceBusReceivedMessage mockServiceBusReceivedMessage2;

    @Mock
    ServiceBusReceivedMessage mockServiceBusReceivedMessage3;

    ServiceBusController serviceBusController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockServiceBusConnection.getServiceBusReceiverConnection()).thenReturn(mockServiceBusReceiverClient);
        when(mockServiceBusConnection.getServiceBusSenderConnection()).thenReturn(mockServiceBusSenderClient);
        serviceBusController = spy(new ServiceBusController(mockServiceBusConnection));
    }

    @Test
    @DisplayName("Testing createMessage method returns ServiceBusMessage")
    void testingCreateMessageMethod() {
        ServiceBusMessage result = serviceBusController.createMessage("Hello World");

        assertEquals("Hello World", result.getBody().toString());
    }

    @Test
    @DisplayName("Testing sendMessage method calls createMessage with correct payload and sendClient method is triggered")
    void testingSendingMessageMethodHappyPath() {
        serviceBusController.sendMessage("Hello World");

        verify(serviceBusController, times(1)).createMessage("Hello World");
        verify(mockServiceBusSenderClient, times(1)).sendMessage(any(ServiceBusMessage.class));
    }

    @Test
    @DisplayName("Testing Receive Message Method")
    void testingReceiveMessageMethod() {

        IterableStream<ServiceBusReceivedMessage> serviceBusMessageIterableStream = new IterableStream<>(List.of(mockServiceBusReceivedMessage1, mockServiceBusReceivedMessage2));

        when(mockServiceBusReceiverClient.receiveMessages(anyInt(), any())).thenReturn(serviceBusMessageIterableStream);
        when(mockServiceBusReceivedMessage1.getBody()).thenReturn(BinaryData.fromString("Test Message 1"));
        when(mockServiceBusReceivedMessage2.getBody()).thenReturn(BinaryData.fromString("Test Message 2"));

        Map<ServiceBusReceivedMessage, String> expectedMethodOutput = Map.of(
                mockServiceBusReceivedMessage1, "Test Message 1",
                mockServiceBusReceivedMessage2, "Test Message 2"
        );

        Map<ServiceBusReceivedMessage, String> actualMethodOutput = serviceBusController.receiveMessage();

        assertEquals(expectedMethodOutput, actualMethodOutput);
    }

    @Test
    @DisplayName("Testing deleteMessage method happy path scenario")
    void testingDeleteMessageMethodHappyPath() {
        serviceBusController.deleteMessage(mockServiceBusReceivedMessage1);

        verify(mockServiceBusReceiverClient, times(1)).complete(mockServiceBusReceivedMessage1);
    }

    @Test
    @DisplayName("Testing receiveTargetMessagesFromSubscriptionContentAndMessageProperties method happy path")
    void testingReceiveTargetMessagesFromSubContentAndMessageProperties() {

        IterableStream<ServiceBusReceivedMessage> serviceBusMessageIterableStream = mockedServiceBusReceivedMessageStream();
        when(mockServiceBusReceiverClient.receiveMessages(anyInt(), any())).thenReturn(serviceBusMessageIterableStream);

        List<ServiceBusReceivedMessage> actualMessageList = serviceBusController.receiveTargetMessagesFromSubscriptionContentAndMessageProperties("sameBodyValue1","sameBodyKey1", "samePropValue1", "samePropKey1");

        assertEquals(2, actualMessageList.size());
    }

    @Test
    @DisplayName("Testing receiveTargetMessagesFromSubscriptionMatchingBodyContent method happy path")
    void testingReceiveTargetMessagesFromSubscriptionMatchingBodyContent() {

        IterableStream<ServiceBusReceivedMessage> serviceBusMessageIterableStream = mockedServiceBusReceivedMessageStream();
        when(mockServiceBusReceiverClient.receiveMessages(anyInt(), any())).thenReturn(serviceBusMessageIterableStream);

        List<ServiceBusReceivedMessage> actualMessageList = serviceBusController.receiveTargetMessagesFromSubscriptionMatchingBodyContent("sameBodyValue1", "sameBodyKey1");

        assertEquals(2, actualMessageList.size());
    }

    @Test
    @DisplayName("Testing receiveTargetMessagesFromSubscriptionMatchingExpectedJson method happy path with custom properties")
    void testingReceiveTargetMessagesFromSubscriptionMatchingExpectedJson() {

        IterableStream<ServiceBusReceivedMessage> serviceBusMessageIterableStream = mockedServiceBusReceivedMessageStream();
        when(mockServiceBusReceiverClient.receiveMessages(anyInt(), any())).thenReturn(serviceBusMessageIterableStream);

        JSONObject expectedJson = returnExpectedJsonObject();

        List<ServiceBusReceivedMessage> actualMessageList = serviceBusController.receiveTargetMessagesFromSubscriptionMatchingExpectedJson(expectedJson, "samePropKey1", "samePropValue1");

        assertEquals(2, actualMessageList.size());
    }

    @Test
    @DisplayName("Testing receiveTargetMessagesFromSubscriptionMatchingExpectedJson method happy path without checking properties")
    void testingReceiveTargetMessagesFromSubscriptionMatchingExpectedJsonWithoutCheckingProperties() {

        IterableStream<ServiceBusReceivedMessage> serviceBusMessageIterableStream = mockedServiceBusReceivedMessageStream();
        when(mockServiceBusReceiverClient.receiveMessages(anyInt(), any())).thenReturn(serviceBusMessageIterableStream);

        JSONObject expectedJson = returnExpectedJsonObject();

        List<ServiceBusReceivedMessage> actualMessageList = serviceBusController.receiveTargetMessagesFromSubscriptionMatchingExpectedJson(expectedJson, "", "");

        assertEquals(2, actualMessageList.size());
    }

    @Test
    @DisplayName("Testing receiveMessagesAsJsonObjects method happy path")
    void testingReceiveMessagesAsJsonObjects() {

        IterableStream<ServiceBusReceivedMessage> serviceBusMessageIterableStream = mockedServiceBusReceivedMessageStream();
        when(mockServiceBusReceiverClient.receiveMessages(anyInt(), any())).thenReturn(serviceBusMessageIterableStream);

        List<JSONObject> actualJsonList = serviceBusController.receiveMessagesAsJsonObjects();

        assertEquals(3, actualJsonList.size());
    }

    @Test
    @DisplayName("Testing getTargetErrorMessagesFromQueue happy path")
    void testingGetTargetErrorMessagesFromQueue() {

        IterableStream<ServiceBusReceivedMessage> serviceBusErrorMessageIterableStream = mockedServiceBusReceivedErrorMessageStream();
        when(mockServiceBusReceiverClient.receiveMessages(anyInt(), any())).thenReturn(serviceBusErrorMessageIterableStream);

        List<ServiceBusReceivedMessage> actualMessageList = serviceBusController.getTargetErrorMessagesFromQueue("stubbedErrorMessage1");

        assertEquals(2, actualMessageList.size());
    }


    private IterableStream<ServiceBusReceivedMessage> mockedServiceBusReceivedMessageStream() {

        IterableStream<ServiceBusReceivedMessage> serviceBusMessageIterableStream = new IterableStream<>(List.of(mockServiceBusReceivedMessage1, mockServiceBusReceivedMessage2, mockServiceBusReceivedMessage3));
        AmqpAnnotatedMessage mockAnnotatedMessage1 = mock(AmqpAnnotatedMessage.class);
        AmqpAnnotatedMessage mockAnnotatedMessage2 = mock(AmqpAnnotatedMessage.class);
        AmqpAnnotatedMessage mockAnnotatedMessage3 = mock(AmqpAnnotatedMessage.class);

        AmqpMessageBody mockAmqpMessageBody1 = mock(AmqpMessageBody.class);
        AmqpMessageBody mockAmqpMessageBody2 = mock(AmqpMessageBody.class);
        AmqpMessageBody mockAmqpMessageBody3 = mock(AmqpMessageBody.class);

        BinaryData stubbedBody1 = BinaryData.fromString("{\"sameBodyKey1\":\"sameBodyValue1\"}");
        BinaryData stubbedBody2 = BinaryData.fromString("{\"sameBodyKey1\":\"sameBodyValue1\"}");
        BinaryData stubbedBody3 = BinaryData.fromString("{\"differentBodyKey1\":\"differentBodyValue1\"}");

        when(mockServiceBusReceivedMessage1.getRawAmqpMessage()).thenReturn(mockAnnotatedMessage1);
        when(mockServiceBusReceivedMessage2.getRawAmqpMessage()).thenReturn(mockAnnotatedMessage2);
        when(mockServiceBusReceivedMessage3.getRawAmqpMessage()).thenReturn(mockAnnotatedMessage3);

        when(mockAnnotatedMessage1.getBody()).thenReturn(mockAmqpMessageBody1);
        when(mockAnnotatedMessage2.getBody()).thenReturn(mockAmqpMessageBody2);
        when(mockAnnotatedMessage3.getBody()).thenReturn(mockAmqpMessageBody3);

        when(mockAmqpMessageBody1.getBodyType()).thenReturn(AmqpMessageBodyType.DATA);
        when(mockAmqpMessageBody2.getBodyType()).thenReturn(AmqpMessageBodyType.DATA);
        when(mockAmqpMessageBody3.getBodyType()).thenReturn(AmqpMessageBodyType.DATA);

        when(mockServiceBusReceivedMessage1.getBody()).thenReturn(stubbedBody1);
        when(mockServiceBusReceivedMessage2.getBody()).thenReturn(stubbedBody2);
        when(mockServiceBusReceivedMessage3.getBody()).thenReturn(stubbedBody3);

        Map<String, Object> message1StubbedApplicationProperties = new HashMap<>();
        message1StubbedApplicationProperties.put("samePropKey1", "samePropValue1");
        lenient().when(mockServiceBusReceivedMessage1.getApplicationProperties()).thenReturn(message1StubbedApplicationProperties);

        Map<String, Object> message2StubbedApplicationProperties = new HashMap<>();
        message2StubbedApplicationProperties.put("samePropKey1", "samePropValue1");
        lenient().when(mockServiceBusReceivedMessage2.getApplicationProperties()).thenReturn(message2StubbedApplicationProperties);

        Map<String, Object> message3StubbedApplicationProperties = new HashMap<>();
        message3StubbedApplicationProperties.put("differentPropKey1", "differentPropValue1");
        lenient().when(mockServiceBusReceivedMessage3.getApplicationProperties()).thenReturn(message3StubbedApplicationProperties);

        return serviceBusMessageIterableStream;
    }

    private IterableStream<ServiceBusReceivedMessage> mockedServiceBusReceivedErrorMessageStream() {
        IterableStream<ServiceBusReceivedMessage> serviceBusMessageIterableStream = new IterableStream<>(List.of(mockServiceBusReceivedMessage1, mockServiceBusReceivedMessage2));

        BinaryData stubbedBody1 = BinaryData.fromString("stubbedErrorMessage1");
        BinaryData stubbedBody2 = BinaryData.fromString("stubbedErrorMessage1");

        when(mockServiceBusReceivedMessage1.getBody()).thenReturn(stubbedBody1);
        when(mockServiceBusReceivedMessage2.getBody()).thenReturn(stubbedBody2);

        return serviceBusMessageIterableStream;
    }

    private JSONObject returnExpectedJsonObject() {
        JSONObject expectedJson = new JSONObject();
        expectedJson.put("sameBodyKey1", "sameBodyValue1");
        return expectedJson;
    }
}

package FrameworkTests;

import com.asos.ip.helper.QueueStorage.QueueStorageController;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueStorageException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueueStorageControllerShould {

    @Mock
    QueueClient mockQueueClient;

    @Mock
    PagedIterable<QueueMessageItem> pagedQueueMessageItemIterable;

    @Mock
    PagedIterable<PeekedMessageItem> pagedPeekedMessageItemIterable;

    @Mock
    QueueMessageItem mockQueueMessageItem1;

    @Mock
    QueueMessageItem mockQueueMessageItem2;

    @Mock
    PeekedMessageItem mockPeekedMessageItem1;

    @Mock
    PeekedMessageItem mockPeekedMessageItem2;

    QueueStorageController queueStorageController;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        queueStorageController = spy(new QueueStorageController(mockQueueClient));
    }


    @ParameterizedTest
    @MethodSource("deleteMessageInputs")
    @DisplayName("Testing deleteMessage method - Various paths")
    void successfullyFinishDeleteMethod(String dummyMessageId, String dummyPopReceipt, int methodCallNumber) {

        when(mockQueueMessageItem1.getMessageId()).thenReturn(dummyMessageId);
        when(mockQueueMessageItem1.getPopReceipt()).thenReturn(dummyPopReceipt);

        queueStorageController.deleteMessage(mockQueueClient, mockQueueMessageItem1);

        verify(mockQueueClient, times(methodCallNumber)).deleteMessage(eq(dummyMessageId), eq(dummyPopReceipt));
    }

    @Test
    @DisplayName("Testing sendMessage method - Successfully calls send method")
    void successfullyCallsSendMethod() {
        queueStorageController.sendMessage("test");
        verify(mockQueueClient).sendMessage("test");
    }

    @ParameterizedTest
    @CsvSource({
            "'testMessage1', 1",
            "'testMessage2', 1",
            "'testMessage3', 0"
    })
    @DisplayName("Testing peekAndReturnMatchingPeekedMessageItems method - Confirming that matching messages from peeked method are returned")
    void returnCorrectNumberOfMatchingPeekedQueueMessages(String searchString, int expectedMatchingMessageNumber) {

        when(mockPeekedMessageItem1.getBody()).thenReturn(BinaryData.fromString("testMessage1"));
        when(mockPeekedMessageItem2.getBody()).thenReturn(BinaryData.fromString("testMessage2"));

        List<PeekedMessageItem> mockedPeekedMessageItems = Arrays.asList(mockPeekedMessageItem1, mockPeekedMessageItem2);

        lenient().when(pagedPeekedMessageItemIterable.iterator()).thenReturn(mockedPeekedMessageItems.iterator());
        when(pagedPeekedMessageItemIterable.stream()).thenReturn(mockedPeekedMessageItems.stream());
        when(mockQueueClient.peekMessages(eq(32), any(Duration.class), eq(Context.NONE))).thenReturn(pagedPeekedMessageItemIterable);

        List<PeekedMessageItem> matchingMessageItems = queueStorageController.peekAndReturnMatchingPeekedMessageItems(searchString);

        Assertions.assertEquals(expectedMatchingMessageNumber, matchingMessageItems.size());
        verify(mockQueueClient).peekMessages(eq(32), any(Duration.class), eq(Context.NONE));
    }

    @ParameterizedTest
    @CsvSource({
            "'testMessage1', 1",
            "'testMessage2', 1",
            "'testMessage3', 0"
    })
    @DisplayName("Testing receiveAndReturnMatchingQueueMessageItems method - Confirming that matching message items from receive method are returned")
    void returnCorrectNumberOfMatchingReceivedQueueItems(String searchString, int expectedMatchingMessageNumber) {

        when(mockQueueMessageItem1.getBody()).thenReturn(BinaryData.fromString("testMessage1"));
        when(mockQueueMessageItem2.getBody()).thenReturn(BinaryData.fromString("testMessage2"));

        when(pagedQueueMessageItemIterable.iterator()).thenReturn(Arrays.asList(mockQueueMessageItem1, mockQueueMessageItem2).iterator());
        when(mockQueueClient.receiveMessages(eq(32), any(Duration.class), any(Duration.class), eq(Context.NONE))).thenReturn(pagedQueueMessageItemIterable);

        List<QueueMessageItem> matchingMessageItems = queueStorageController.receiveAndReturnMatchingQueueMessageItems(searchString);

        Assertions.assertEquals(expectedMatchingMessageNumber, matchingMessageItems.size());
        verify(mockQueueClient).receiveMessages(eq(32), any(Duration.class), any(Duration.class), eq(Context.NONE));
    }

    @Test
    @DisplayName("Exception Testing - peekAndReturnMatchingQueueMessageItems method - QueueStorageException Correctly Thrown")
    void throwExceptionForNoQueueFoundInPeekAndReturnMethod() {

        when(mockQueueClient.peekMessages(eq(32), any(Duration.class), eq(Context.NONE))).thenThrow(new QueueStorageException("Queue not found", null, null));

        QueueStorageException thrown = assertThrows(QueueStorageException.class, () -> {
            queueStorageController.peekAndReturnMatchingPeekedMessageItems("testMessage");
        });

        Assertions.assertEquals("Queue not found", thrown.getMessage());
        verify(mockQueueClient).peekMessages(eq(32), any(Duration.class), eq(Context.NONE));
    }

    @Test
    @DisplayName("Exception Testing receiveAndReturnMatchingQueueMessageItems method - QueueStorageException Correctly Thrown")
    void throwExceptionForNoQueueFoundInReceiveAndReturnMethod() {

        when(mockQueueClient.receiveMessages(eq(32), any(Duration.class), any(Duration.class), eq(Context.NONE))).thenThrow(new QueueStorageException("Queue not found", null, null));

        QueueStorageException thrown = assertThrows(QueueStorageException.class, () -> {
            queueStorageController.receiveAndReturnMatchingQueueMessageItems("testMessage");
        });

        Assertions.assertEquals("Queue not found", thrown.getMessage());
        verify(mockQueueClient).receiveMessages(eq(32), any(Duration.class), any(Duration.class), eq(Context.NONE));
    }

    static Stream<Arguments> deleteMessageInputs() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("dummy_message_id", "dummy_pop_receipt", 1),
                org.junit.jupiter.params.provider.Arguments.of("dummy_message_id", null, 0),
                org.junit.jupiter.params.provider.Arguments.of(null, "dummy_pop_receipt", 0),
                org.junit.jupiter.params.provider.Arguments.of(null, null, 0)
        );
    }

}
package FrameworkTests;

import com.asos.ip.helper.BlobStorage.BlobStorageConnection;
import com.asos.ip.helper.BlobStorage.BlobStorageController;
import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BlobStorageControllerShould {

    @Mock
    private BlobServiceClient mockBlobServiceClient;

    @Mock
    private BlobStorageConnection mockBlobStorageConnection;

    @Mock
    private BlobContainerClient mockBlobContainerClient;

    @Mock
    private BlobClient mockBlobClient1;

    @Mock
    private BlobClient mockBlobClient2;

    @Mock
    private BlobItem mockBlobItem1;

    @Mock
    private BlobItem mockBlobItem2;

    private BlobStorageController blobStorageController;


    @BeforeEach
    void setUp() {
        when(mockBlobStorageConnection.getBlobServiceClient()).thenReturn(mockBlobServiceClient);
        when(mockBlobStorageConnection.getBlobContainerClient("dummy-container-name")).thenReturn(mockBlobContainerClient);
        blobStorageController = spy(new BlobStorageController(mockBlobContainerClient));
    }

    @Test
    @DisplayName("Testing returnBlob Method - Should return a BlobClient instance")
    void returnBlobTest() {

        when(mockBlobContainerClient.getBlobClient(anyString())).thenReturn(mockBlobClient1);

        assertEquals(mockBlobClient1, blobStorageController.returnBlob("dummy-blob-name"));
    }

    @Test
    @DisplayName("Testing doesBlobExist Method - Should return true with valid BlobClient")
    void blobDoesExist() {
        when(mockBlobClient1.exists()).thenReturn(true);
        assertTrue(blobStorageController.doesBlobExist(mockBlobClient1));
    }

    @Test
    @DisplayName("Testing uploadFileToBlobStorage Method - Should trigger methods to upload to Blob Storage")
    void uploadsFileToBlobStorageMocked() {
        String fileNameBlob = "fileNameBlob";
        String filePath = "filePath";
        String contentType = "contentType";

        when(mockBlobContainerClient.getBlobClient(fileNameBlob)).thenReturn(mockBlobClient1);

        blobStorageController.uploadFileToBlobStorage(fileNameBlob, filePath, contentType);

        verify(mockBlobContainerClient, times(2)).getBlobClient(fileNameBlob);
        verify(mockBlobClient1).uploadFromFile(filePath);
        verify(mockBlobClient1).setHttpHeaders(argThat(headers ->
                headers.getContentType().equals(contentType)
        ));
    }

    @Test
    @DisplayName("Testing deleteBlobFile Method - Verifies delete method called")
    void deletesBlobMock() {

        BlockBlobClient mockBlockBlobClient = mock(BlockBlobClient.class);

        when(mockBlobItem1.getName()).thenReturn("Dummy-Name");
        when(mockBlobContainerClient.getBlobClient("Dummy-Name")).thenReturn(mockBlobClient1);
        when(mockBlobClient1.getBlockBlobClient()).thenReturn(mockBlockBlobClient);

        blobStorageController.deleteBlobFile(mockBlobItem1);

        verify(mockBlockBlobClient).delete();
    }

    @Test
    @DisplayName("Testing checkIfBlobExistsAndUpload Method - Verifies final lines called and if statement to check if blob exists returns false")
    void checkIfBlobExistsAndUploadsDoesntExist() {

        String fileNameBlob = "fileNameBlob";
        String filePath = "filePath";
        String contentType = "contentType";

        when(mockBlobContainerClient.getBlobClient(fileNameBlob)).thenReturn(mockBlobClient1);

        blobStorageController.checkIfBlobExistsAndUpload(fileNameBlob, filePath, contentType);

        verify(mockBlobContainerClient).getBlobClient(fileNameBlob);
        verify(mockBlobClient1).uploadFromFile(filePath);
        verify(mockBlobClient1).setHttpHeaders(argThat(headers ->
                headers.getContentType().equals(contentType)
        ));
    }

    @Test
    @DisplayName("Testing checkIfBlobExistsAndUpload Method - if statement to check if blob exists returns true and blob delete call verified")
    void checkIfBlobExistsAndUploadsDoesExist() {

        String fileNameBlob = "fileNameBlob";
        String filePath = "filePath";
        String contentType = "contentType";

        when(mockBlobContainerClient.getBlobClient(fileNameBlob)).thenReturn(mockBlobClient1);
        when(mockBlobClient1.exists()).thenReturn(true);

        blobStorageController.checkIfBlobExistsAndUpload(fileNameBlob, filePath, contentType);

        verify(mockBlobClient1).delete();

    }

    @Test
    @DisplayName("Testing getBlobFilesMatchingContent - Mocking two BlobClients, one matches the search string, Mocking BlobContainerClient for fetching list of Blobs, test one reaches the final array and that it's the correct BlobClient.")
    void findBlobMatchingSearchStringFromMockedContainerClient() throws Exception {
        String searchString = "search";

        // Mock the PagedIterable to return the blob items
        PagedIterable<BlobItem> mockPagedIterable = mock(PagedIterable.class);
        when(mockPagedIterable.iterator()).thenReturn(Arrays.asList(mockBlobItem1, mockBlobItem2).iterator());

        // When listBlobs() is called, return the mock PagedIterable
        when(mockBlobContainerClient.listBlobs()).thenReturn(mockPagedIterable);

        mockTwoInputStreamsAndReadThem();

        when(blobStorageController.blobItemIsCompressedFile(mockBlobItem1)).thenReturn(false);
        when(blobStorageController.blobItemIsCompressedFile(mockBlobItem2)).thenReturn(false);

        // Run the method to test
        List<BlobItem> matchingBlobs = blobStorageController.getBlobFilesMatchingContent(searchString);

        assertEquals(1, matchingBlobs.size());
        assertTrue(matchingBlobs.contains(mockBlobItem2));
    }

    @Test
    @DisplayName("Testing searchListOfBlobItemsForMatchingContent - Mocking two BlobClients, one matches the search string, Mocking passed-in-list of Blobs, test one reaches the final array and that it's the correct BlobClient.")
    void findBlobMatchingSearchStringFromListOfBlobs() throws IOException {
        String searchString = "search";

        List<BlobItem> mockBlobItems = mock(List.class);
        when(mockBlobItems.iterator()).thenReturn(Arrays.asList(mockBlobItem1, mockBlobItem2).iterator());

        mockTwoInputStreamsAndReadThem();

        List<BlobItem> matchingBlobs = blobStorageController.searchListOfBlobItemsForMatchingContent(mockBlobItems, searchString);

        assertEquals(1, matchingBlobs.size());
        assertTrue(matchingBlobs.contains(mockBlobItem2));

    }

    @Test
    @DisplayName("Testing getBlobFilesMatchingHeirachy - Two blob items are mocked, one is a file, the other a prefix(folder), only the file should be added to the list")
    void returnOnlyBlobItemMatchingHierarchy() {

        String searchString = "search";

        when(mockBlobItem1.isPrefix()).thenReturn(false); // This represents a real blob, not a prefix
        when(mockBlobItem1.getName()).thenReturn("search/file1.txt");

        when(mockBlobItem2.isPrefix()).thenReturn(true); // This represents a folder

        PagedIterable<BlobItem> mockPagedIterable = mock(PagedIterable.class);

        when(mockBlobContainerClient.listBlobsByHierarchy(eq("/"), any(), eq(Duration.ofSeconds(20))))
                .thenReturn(mockPagedIterable);

        doAnswer(invocation -> {
            Consumer<BlobItem> consumer = invocation.getArgument(0);
            consumer.accept(mockBlobItem1); // Mock the consumer with mockBlobItem1
            consumer.accept(mockBlobItem2); // Mock the consumer with mockBlobItem2
            return null; // No return value, so we return null
        }).when(mockPagedIterable).forEach(any());

        List<BlobItem> matchingBlobs = blobStorageController.getBlobFilesMatchingHeirarchy(searchString);

        assertEquals(1, matchingBlobs.size(), "Only one blob should be added");
        assertTrue(matchingBlobs.contains(mockBlobItem1), "The blob item should be added");
        assertFalse(matchingBlobs.contains(mockBlobItem2), "The prefix should not be added");

    }

    @Test
    @DisplayName("Testing blobItemIsCompressedFile method - Test true returned for file ending in tar.gz")
    void returnsTrueForTarGzFile() {
        when(mockBlobItem1.getName()).thenReturn("mockFile.tar.gz");
        assertTrue(blobStorageController.blobItemIsCompressedFile(mockBlobItem1));
    }

    @Test
    @DisplayName("Testing blobItemIsCompressedFile method - Test true returned for file ending in tar.gz")
    void returnsFalseForNonCompressedFile() {
        when(mockBlobItem1.getName()).thenReturn("mockFile.txt");
        assertFalse(blobStorageController.blobItemIsCompressedFile(mockBlobItem1));
    }

    private void mockTwoInputStreamsAndReadThem() throws IOException {

        // Set the names of the blobs
        when(mockBlobItem1.getName()).thenReturn("blob1.txt");
        when(mockBlobItem2.getName()).thenReturn("blob2.txt");

        when(mockBlobContainerClient.getBlobClient("blob1.txt")).thenReturn(mockBlobClient1);
        when(mockBlobContainerClient.getBlobClient("blob2.txt")).thenReturn(mockBlobClient2);

        // Create InputStreams to simulate the content of the blobs
        byte[] blob1Bytes = "No match here".getBytes();
        BlobInputStream mockBlobInputStream1 = mock(BlobInputStream.class);

        byte[] blob2Bytes = "This is a match search".getBytes();
        BlobInputStream mockBlobInputStream2 = mock(BlobInputStream.class);

        int[] blob1Index = {0}; // Use array to allow modification inside lambda
        int[] blob2Index = {0};

        mockBlobRead(mockBlobInputStream1, blob1Bytes, blob1Index);
        mockBlobRead(mockBlobInputStream2, blob2Bytes, blob2Index);

        // Mock the openQueryInputStream() method with a String argument
        when(mockBlobClient1.openInputStream()).thenReturn(mockBlobInputStream1);
        when(mockBlobClient2.openInputStream()).thenReturn(mockBlobInputStream2);

    }

    private void mockBlobRead(InputStream mockBlobInputStream, byte[] blobBytes, int[] blobIndex) throws IOException {
        when(mockBlobInputStream.read(any(byte[].class), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    byte[] buffer = invocation.getArgument(0);
                    int offset = invocation.getArgument(1);
                    int length = invocation.getArgument(2);

                    if (blobIndex[0] >= blobBytes.length) {
                        return -1; // End of stream
                    }

                    int bytesRead = Math.min(length, blobBytes.length - blobIndex[0]);
                    System.arraycopy(blobBytes, blobIndex[0], buffer, offset, bytesRead);
                    blobIndex[0] += bytesRead; // Update position
                    return bytesRead;
                });
    }

}

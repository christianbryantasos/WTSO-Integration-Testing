package FrameworkTests;

import com.asos.ip.helper.SFTP.SFTPConnection;
import com.asos.ip.helper.SFTP.SFTPController;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class sftpControllerShould {

    public final String dummy_remote_path = "dummy_remote_path";
    public final String dummy_filepath = "dummy_filepath";

    @Mock
    private SFTPConnection mockSFTPConnection;

    @Mock
    private SSHClient mockSSHClient;

    @Mock
    private SFTPClient mockSftpClient;

    @Mock
    private RemoteResourceInfo mockRemoteResourceInfo;

    @Captor
    private ArgumentCaptor<String> argumentCaptor;

    private SFTPController sftpController;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this); // Ensures mocks are initialized
        when(mockSFTPConnection.getSFTPConnection()).thenReturn(mockSSHClient);
        when(mockSFTPConnection.getActiveRemotePath()).thenReturn(dummy_remote_path);
        when(mockSSHClient.newSFTPClient()).thenReturn(mockSftpClient);
        sftpController = spy(new SFTPController(mockSFTPConnection));
    }

    @Test
    @DisplayName("Testing insertDataIntoSFTP method - Asserts put method is reached with correct data")
    void insertFileIntoSFTPServerSuccessfully() throws IOException {

        sftpController.insertDataIntoSFTP(dummy_filepath);

        verify(mockSftpClient, times(1)).put(dummy_filepath, dummy_remote_path);
    }

    @Test
    @DisplayName("Testing insertDataIntoSFTP method - Assert IOException thrown")
    void failTheInsertionCorrectly() throws IOException {

        doThrow(new IOException("Simulated Failure")).when(mockSftpClient).put(dummy_filepath, dummy_remote_path);

        Assertions.assertThrows(IOException.class, () -> {
            sftpController.insertDataIntoSFTP(dummy_filepath);
        });
    }

    @Test
    @DisplayName("Testing deleteDataFromSFTP Method - Delete path line reached and the correct path assembled")
    void reachTheDeleteSuccessfullyAndWithTheCorrectPath() throws IOException {

        when(sftpController.checkFileExists(dummy_filepath)).thenReturn(true);

        sftpController.deleteDataFromSFTP(dummy_filepath);

        verify(mockSftpClient).rm(argumentCaptor.capture());

        assertEquals(dummy_remote_path + "/" + dummy_filepath, argumentCaptor.getValue());
    }

    @Test
    @DisplayName("Testing deleteDataFromSFTP Method - Else triggered as File Not Found")
    void raiseTheLogWhenFileToDeleteNotFound() throws IOException {

        when(sftpController.checkFileExists(dummy_filepath)).thenReturn(false);

        sftpController.deleteDataFromSFTP(dummy_filepath);

        verify(mockSftpClient, times(0)).rm(dummy_remote_path + "/" + dummy_filepath);
    }

    @Test
    @DisplayName("Testing deleteDataFromSFTP Method - IOException failure when remove method fails")
    void failWithIOExceptionWhenDeleteFails() throws IOException {

        doThrow(new IOException("Simulated Failure")).when(mockSftpClient).rm(dummy_remote_path + "/" + dummy_filepath);

        when(sftpController.checkFileExists(dummy_filepath)).thenReturn(true);

        Assertions.assertThrows(IOException.class, () -> {
            sftpController.deleteDataFromSFTP(dummy_filepath);
        });
    }

    @Test
    @DisplayName("Testing checkFileExistsInSftp Method - Finds matching copy of filepath in mocked search, returns true")
    void returnTrueWhenMatchingFileNameFromMockedSFTPFound() throws IOException {

        List<RemoteResourceInfo> listContainingMatchingResource = List.of(mockRemoteResourceInfo);

        when(mockSftpClient.ls(dummy_remote_path)).thenReturn(listContainingMatchingResource);
        when(mockRemoteResourceInfo.toString()).thenReturn(dummy_remote_path + "/" + dummy_filepath);

        assertTrue(sftpController.checkFileExists(dummy_filepath));
    }

    @Test
    @DisplayName("Testing checkFileExistsInSftp Method - Does Not find copy of filepath in mocked search, returns false")
    void returnFalseWhenNoMatchingFileNameFromMockedSFTPFound() throws IOException {

        List<RemoteResourceInfo> listContainingMatchingResource = List.of(mockRemoteResourceInfo);

        when(mockSftpClient.ls(dummy_remote_path)).thenReturn(listContainingMatchingResource);
        when(mockRemoteResourceInfo.toString()).thenReturn("No Match Here");

        assertFalse(sftpController.checkFileExists(dummy_filepath));
    }

    @Test
    @DisplayName("Testing checkFileExistsInSftp Method - IOException correctly thrown")
    void returnIOExceptionWhenCheckFileMethodErrorsAtLs() throws IOException {

        doThrow(new IOException("Simulated Failure")).when(mockSftpClient).ls(dummy_remote_path);

        Assertions.assertThrows(IOException.class, () -> {
            sftpController.checkFileExists(dummy_filepath);
        });
    }

}


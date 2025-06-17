package com.asos.ip.helper.SFTP;

import com.asos.ip.helper.threadHelper.ThreadHelper;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.connection.channel.direct.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import java.util.ArrayList;
import java.util.List;


public class SFTPController {
    private static final Logger logger = LoggerFactory.getLogger(SFTPController.class);
    public final String remotePath;
    SSHClient sshClient;
    SFTPClient sftpClient;

    public SFTPController(SFTPConnection sftpConnection) {
        this.sshClient = sftpConnection.getSFTPConnection();
        remotePath = sftpConnection.getActiveRemotePath();

        try {
            if (sshClient != null) {
                sftpClient = sshClient.newSFTPClient();
            }
        } catch (IOException e) {
            System.err.println("Failed to create SFTPClient: " + e.getMessage());
        }
    }

    public void insertDataIntoSFTP(String filePath) throws IOException {
        try {
            sftpClient.put(filePath, remotePath);
            logger.info("Data inserted into SFTP: {} -> {}", filePath, remotePath);
        } catch (IOException e) {
            logger.error("Error inserting data into SFTP: {} -> {}", filePath, remotePath, e);
            throw e;
        }
    }

    public void deleteDataFromSFTP(String filePath) throws IOException {
        try {
            if (checkFileExists(filePath)) {
                sftpClient.rm(remotePath + "/" + filePath);
                logger.info("Data deleted from SFTP: {}", filePath);
            } else {
                logger.warn("Data not found in SFTP: {}", filePath);
            }
        } catch (IOException e) {
            logger.error("Error deleting data from SFTP: {}", filePath, e);
            throw e;
        }
    }

    public boolean checkFileExists(String filePath) throws IOException {
        try {
            String completeFilePath = remotePath + "/" + filePath;
            for (RemoteResourceInfo fileName : sftpClient.ls(remotePath)){
              String stringToCompare = fileName.toString().replaceAll("\\[[^\\]]*\\]\\s*", "");
                if (stringToCompare.equals(completeFilePath)) {
                    logger.info("File exists in SFTP: {}", filePath);
                    return true;
                }
            }
            System.out.println("File does not exist in SFTP: " + filePath);
            return false;
        } catch (IOException e) {
            logger.error("Error checking if file exists in SFTP: {}", filePath, e);
            throw e;
        }
    }

    public void generateTestFileInSFTPServer(String fileName, long totalFileSizeInMB, int numberOfThreads) {

        String fullRemoteFilePath = remotePath + "/" + fileName;

        ThreadHelper threadHelper = new ThreadHelper();
        List<Thread> threads = new ArrayList<>();

        long totalFileSizeBytes = totalFileSizeInMB * 1024 * 1024;
        long chunkSize = totalFileSizeBytes / numberOfThreads;

        threadHelper.startThreadProcessTimer();

        for (int i = 0; i < numberOfThreads; i++) {
            long offset = i * chunkSize; // Calculate the starting position of each chunk
            long chunkFinalSize = (i == numberOfThreads - 1) ? totalFileSizeBytes - offset : chunkSize; // Adjust the size of the last chunk if it's not evenly divisible

            final int chunkIndex = i;
            Thread thread = new Thread(() -> {
                try {
                    generateChunkInSFTPServer(fullRemoteFilePath, offset, chunkFinalSize, chunkIndex);
                } catch (Exception e) {
                    logger.error("Error uploading chunk {}: {}", chunkIndex, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted: {}", e.getMessage());
            }
        }

        threadHelper.stopThreadProcessTimer();
        double totalFileGenerationTime = threadHelper.getElapsedThreadProcessTimeInSeconds();

        logger.info("File generation completed. Total time: {} seconds using {} threads", totalFileGenerationTime, numberOfThreads);
    }

    private void generateChunkInSFTPServer(String remoteFilePath, long offset, long chunkSize, int chunkIndex) {

        long offsetMB = offset / (1024 * 1024);  // Correctly converting offset to MB
        long chunkSizeMB = Math.max(1, chunkSize / (1024 * 1024));  // Correctly converting chunkSize to MB

        String command = "dd if=/dev/zero of=" + remoteFilePath +
                " bs=1M seek=" + offsetMB +
                " count=" + chunkSizeMB +
                " conv=notrunc status=progress";

        try (Session session = sshClient.startSession()) {
            logger.info("Uploading chunk {}...", chunkIndex);

            Session.Command cmd = session.exec(command);

            try (InputStream stderr = cmd.getErrorStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;

                // Read and ignore the output from stderr (this would contain progress info from dd)
                while ((bytesRead = stderr.read(buffer)) != -1) {
                    System.err.write(buffer, 0, bytesRead);
                }
            }

            cmd.join();  // Wait for command completion
            logger.info("Chunk {} uploaded.", chunkIndex);

        } catch (IOException e) {
            logger.error("Error executing dd for chunk {}: {}", chunkIndex, e.getMessage());
        }
    }


    public void disconnectSFTPChannel() {
        try {
            if (sftpClient != null) {
                logger.info("Disconnecting SFTP Connection");
                sftpClient.close();
            }
        } catch (IOException e) {
            logger.error("Error closing SFTP client: {}", e.getMessage());
        }
    }
}

package com.asos.ip.helper.SFTP;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SFTPConnection {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SFTPConnection.class);

    private final SSHClient sshClient;
    private final Map<String, String> remotePaths = new HashMap<>();
    private String activeRemotePath;
    Map<String, String> sftpConnectionDetails;

    public SFTPConnection(Map<String, String> sftpConnectionDetails) {
        sshClient = makeSFTPConnection(sftpConnectionDetails);
        this.sftpConnectionDetails = sftpConnectionDetails;
    }

    private SSHClient makeSFTPConnection(Map<String, String> sftpHostDetails) {
        SSHClient ssh = new SSHClient();
        String host = sftpHostDetails.get("host");
        String user = sftpHostDetails.get("user");
        String password = sftpHostDetails.get("password");
        int port = Integer.parseInt(sftpHostDetails.get("port"));

        try {
            ssh.addHostKeyVerifier(new PromiscuousVerifier()); // For simplicity, accepts any host key
            ssh.connect(host, port);
            ssh.authPassword(user, password);
            logger.info("SFTP Connection Established: {}", host);
            return ssh;
        } catch (IOException e) {
            logger.error("Failed to connect to SFTP server: {}", host, e);
        }
        logger.warn("Connection to SFTP server failed: {}", host);
        return null;
    }

    public SSHClient getSFTPConnection() {
        return sshClient;
    }

    public Map<String, String> getRemotePaths() {
        return remotePaths;
    }

    public void setActiveRemotePath(String remotePathTag) {
        activeRemotePath = getRemotePaths().get(remotePathTag);
    }

    public String getActiveRemotePath() {
        return activeRemotePath;
    }

    public void addNewRemotePath(String remotePathTag, String actualPathValue) {
        remotePaths.put(remotePathTag, actualPathValue);
    }

    public void closeSftpConnection() {
        try {
            if (sshClient != null && sshClient.isConnected()) {
                logger.info("Closing SSH Client Connection");
                sshClient.close();
            }
        } catch (IOException e) {
            logger.error("Error closing SSH Client: {}", e.getMessage());
        }
    }

}

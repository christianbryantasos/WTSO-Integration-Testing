package com.asos.ip.helper.SFTP;

import com.asos.ip.config.LoadConfigurations;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SFTPConnectionManager {

    private Properties properties;
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SFTPConnectionManager.class);

    SFTPConnection activeSFTPConnection;
    HashMap<String, SFTPConnection> sftpConnectionMap = new HashMap<>();

    public SFTPConnectionManager(LoadConfigurations loadConfigurations) {
        this.properties = loadConfigurations.getConfigProperties();
    }

    public SFTPConnection connectToSFTP(String sftpConnectionHost) {
        // Creating a map that holds the SFTP connection details

        String sftpHostPlaceholder = "sftp.{sftpDetailsTag}.host";
        String sftpUsernamePlaceholder = "sftp.{sftpDetailsTag}.username";
        String sftpPasswordPlaceholder = "sftp.{sftpDetailsTag}.password";
        String sftpPortPlaceholder = "sftp.{sftpDetailsTag}.port";

        String sftpHostKey = sftpHostPlaceholder.replace("{sftpDetailsTag}", sftpConnectionHost);
        String sftpUsernameKey = sftpUsernamePlaceholder.replace("{sftpDetailsTag}", sftpConnectionHost);
        String sftpPasswordKey = sftpPasswordPlaceholder.replace("{sftpDetailsTag}", sftpConnectionHost);
        String sftpPortKey = sftpPortPlaceholder.replace("{sftpDetailsTag}", sftpConnectionHost);

        Map<String, String> sftpConnectionDetails = generateSftpConnectionDetailsMap(
                properties.getProperty(sftpHostKey),
                properties.getProperty(sftpUsernameKey),
                properties.getProperty(sftpPasswordKey),
                properties.getProperty(sftpPortKey)
        );

        logger.info("SFTP Connection bean created");

        // Return a new instance of SFTPConnection with the details
        return new SFTPConnection(sftpConnectionDetails);
    }

    public SFTPConnection setActiveSFTPConnection(String sftpHostTag) {

        String sftpHostTagLowerCase = sftpHostTag.toLowerCase();

        if (sftpConnectionMap.containsKey(sftpHostTagLowerCase)) {
            activeSFTPConnection = sftpConnectionMap.get(sftpHostTagLowerCase);
            return activeSFTPConnection;

        } else {

            sftpConnectionMap.put(sftpHostTagLowerCase, connectToSFTP(sftpHostTagLowerCase));
            activeSFTPConnection = sftpConnectionMap.get(sftpHostTagLowerCase);
            createMapOfRemotePaths(sftpHostTagLowerCase);
        }

        return activeSFTPConnection;
    }

    public void createMapOfRemotePaths(String hostTag) {

        hostTag = hostTag.toLowerCase();

        String baseSftpPathPlaceholder = "sftp.{hostTag}.path.";
        String baseSftpPathKey = baseSftpPathPlaceholder.replace("{hostTag}", hostTag);

        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(baseSftpPathKey)) {
                String remotePathTag = key.replace(baseSftpPathKey, "");
                String actualPath = properties.getProperty(key);
                activeSFTPConnection.addNewRemotePath(remotePathTag, actualPath);
            }
        }
    }

    public static Map<String, String> generateSftpConnectionDetailsMap(String host, String username, String password, String port) {
        return Map.of(
                "host", host,
                "user", username,
                "password", password,
                "port", port
        );
    }

    public void disconnectAllSftpConnections() {
        for(SFTPConnection sftpConnection : sftpConnectionMap.values()) {
            sftpConnection.closeSftpConnection();
        }

        sftpConnectionMap.clear();
    }

}

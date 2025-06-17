package com.asos.ip.helper.JMS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Helper class for interacting with an AQ JMS Topic and handling standard message and xml message uploads
 */
public class JMSHelper {

    private static final Logger logger = LoggerFactory.getLogger(JMSHelper.class);

    private final OracleDataSource dataSource;

    private final AQJMSFactory aqjmsFactory;

    private TopicConnectionFactory topicConnectionFactory;

    private final String topicName;

    Properties properties;

    @Autowired
    public JMSHelper(Properties properties, AQJMSFactory aqjmsFactory, OracleDataSource dataSource) {

        this.dataSource = dataSource;
        this.aqjmsFactory = aqjmsFactory;

        this.properties = properties;

        this.topicName = properties.getProperty("oracle.aq.jms.topic.name");

        initializeTopicConnectionFactory();
    }

    /**
     * Puts a standard text message onto the AQ JMS topic.
     *
     * @param messageContent The message content
     */
    public void putTextMessage(String messageContent) {

        try (TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();
             TopicSession session = topicConnection.createTopicSession(true, Session.AUTO_ACKNOWLEDGE)) {

            Topic topic = session.createTopic(topicName);

            try (TopicPublisher publisher = session.createPublisher(topic)) {

                TextMessage message = session.createTextMessage(messageContent);
                publisher.publish(message);
                session.commit();
                logger.info("Successfully uploaded message: {} to topic: {} ", message, topicName);
            }

        } catch (JMSException e) {
            logger.error("Error uploading message {} to JMS topic: {} {}: {}", messageContent, topicName, e.getMessage(), e);
            throw new IllegalStateException("Failed to send message to the topic", e);
        }
    }

    /**
     * Puts an XML message onto the AQ JMS Topic using putTextMessage()
     *
     * @param xmlFile The XML file to be out onto Topic
     */
    public void putMessageFromXmlFile(File xmlFile) {

        String xmlContent;
        try {
            xmlContent = new String(Files.readAllBytes(xmlFile.toPath()));
        } catch (IOException e) {
            logger.error("Error reading xml file {}, {}: {}", xmlFile, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        putTextMessage(xmlContent);
    }

    /**
     * Initialises the TopicConnectionFactory using the provided Oracle Datasource
     */
    private void initializeTopicConnectionFactory() {
        try {
            topicConnectionFactory = aqjmsFactory.getTopicConnectionFactory(dataSource);
        } catch (JMSException e) {
            logger.error("Error initialising connection {}: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}

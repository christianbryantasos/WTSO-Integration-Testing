package FrameworkTests;

import com.asos.ip.helper.JMS.AQJMSFactory;
import com.asos.ip.helper.JMS.JMSHelper;
import com.asos.ip.helper.JMS.OracleDataSource;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JMSHelperTest {

    public static final String TEST_TOPIC_NAME = "testTopic";

    private JMSHelper jmsHelper;

    @Mock
    private AQJMSFactory aqjmsFactory;

    @Mock
    private OracleDataSource oracleDataSource;

    @Mock
    private TopicConnectionFactory topicConnectionFactory;

    @Mock
    private TopicConnection topicConnection;

    @Mock
    private TopicSession topicSession;

    @Mock
    private TopicPublisher topicPublisher;

    @Mock
    private TextMessage textMessage;

    @Mock
    Properties properties;

    @BeforeEach
    void setUp() throws JMSException {

        MockitoAnnotations.openMocks(this);

        when(properties.getProperty("oracle.aq.jms.url")).thenReturn("jdbc:oracle:thin:@localhost:1521:xe");
        when(properties.getProperty("oracle.aq.jms.username")).thenReturn("username");
        when(properties.getProperty("oracle.aq.jms.password")).thenReturn("password");
        when(properties.getProperty("oracle.aq.jms.topic.name")).thenReturn("topicName");

        when(aqjmsFactory.getTopicConnectionFactory(oracleDataSource)).thenReturn(topicConnectionFactory);

        when(topicConnectionFactory.createTopicConnection()).thenReturn(topicConnection);
        when(topicConnection.createTopicSession(true, Session.AUTO_ACKNOWLEDGE)).thenReturn(topicSession);
        when(topicSession.createTopic(anyString())).thenReturn(mock(Topic.class));
        when(topicSession.createPublisher(any(Topic.class))).thenReturn(topicPublisher);
        when(topicSession.createTextMessage(any(String.class))).thenReturn(textMessage);

        // class under test
        jmsHelper = new JMSHelper(properties, aqjmsFactory, oracleDataSource);

    }

    @Test
    void testPutTextMessageSuccess() throws JMSException {

        // Act
        jmsHelper.putTextMessage("Test message");

        // Assert
        verify(topicPublisher).publish(any(TextMessage.class));
    }

    @Test
    void testPutMessageFromXmlFileSuccess() throws JMSException, IOException {

        // Arrange
        String xmlContent = "<message>Test XML message</message>";
        File xmlFile = new File("test.xml");
        Files.write(xmlFile.toPath(), xmlContent.getBytes());

        // Act
        jmsHelper.putMessageFromXmlFile(xmlFile);

        // Assert
        verify(topicPublisher).publish(any(TextMessage.class));

        // Cleanup
        xmlFile.delete();
    }

    @Test
    void testPutTextMessageJMSException() throws JMSException {

        // Arrange
        doThrow(new JMSException("Sender error")).when(topicPublisher).publish(any(TextMessage.class));

        // Act and assert
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            jmsHelper.putTextMessage("Test message");
        });

        verify(topicPublisher).publish(any(TextMessage.class));
        assertTrue(thrown.getMessage().contains("Failed to send message to the topic"));
    }

    @Test
    void testPutMessageFromXmlFileIOException() {

        // Act and assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            jmsHelper.putMessageFromXmlFile(new File("invalid/path/to/nonexistent/file.xml"));
        });

        assertTrue(thrown.getCause() instanceof IOException);
    }

}

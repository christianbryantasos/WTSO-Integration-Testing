package com.asos.ip.config;

import com.asos.ip.helper.BlobStorage.BlobStorageConnectionManager;
import com.asos.ip.helper.JMS.AQJMSFactory;
import com.asos.ip.helper.JMS.JMSHelper;
import com.asos.ip.helper.JMS.OracleDataSource;
import com.asos.ip.helper.QueueStorage.QueueStorageConnectionManager;
import com.asos.ip.helper.SFTP.SFTPConnectionManager;
import com.asos.ip.helper.ServiceBus.ServiceBusConnectionManager;
import com.asos.ip.helper.TableStorage.TableStorageConnectionManager;
import com.asos.ip.helper.mongoDB.MongoDBConnectionManager;
import com.asos.ip.helper.threadHelper.ThreadHelper;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

import java.util.Properties;

@Configuration
public class SpringConfig {

    Properties properties;
    LoadConfigurations loadConfigurations = new LoadConfigurations();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpringConfig.class);


    @Bean("loadConfigurations")
    public LoadConfigurations loadConfigurations() {

        loadConfigurations.initialize();
        properties = loadConfigurations.getConfigProperties();
        logger.info("Properties Loaded...");
        return loadConfigurations;
    }

    @Bean
    public EndpointManager endpointManager() {
        return new EndpointManager(
                mongoDBConnectionManager(),
                serviceBusConnectionManager(),
                sftpConnectionManager(),
                blobStorageConnectionManager(),
                queueStorageConnectionManager(),
                tableStorageConnectionManager());
    }


    @Bean
    @DependsOn("loadConfigurations")
    public MongoDBConnectionManager mongoDBConnectionManager() {
        logger.info("MongoDBConnectionManager Loaded");
        return new MongoDBConnectionManager(loadConfigurations);
    }

    @Bean
    @DependsOn("loadConfigurations")
    public TableStorageConnectionManager tableStorageConnectionManager() {
        logger.info("TableStorageConnectionManager Loaded");
        return new TableStorageConnectionManager(loadConfigurations);
    }

    @Bean
    @DependsOn("loadConfigurations")
    public ServiceBusConnectionManager serviceBusConnectionManager() {
        logger.info("ServiceBusConnectionManager Loaded");
        return new ServiceBusConnectionManager(loadConfigurations);
    }

    @Bean
    @DependsOn("loadConfigurations")
    public BlobStorageConnectionManager blobStorageConnectionManager() {
        logger.info("BlobStorageConnectionManager Loaded");
        return new BlobStorageConnectionManager(loadConfigurations);
    }

    @Bean
    @DependsOn("loadConfigurations")
    public QueueStorageConnectionManager queueStorageConnectionManager() {
        logger.info("QueueStorageConnectionManager Loaded");
        return new QueueStorageConnectionManager(loadConfigurations);
    }

    @Bean
    @DependsOn("loadConfigurations")
    public SFTPConnectionManager sftpConnectionManager() {
        logger.info("SFTPConnectionManager Loaded");
        return new SFTPConnectionManager(loadConfigurations);
    }


    @Bean
    @Lazy
    public ThreadHelper threadHelper() {
       logger.info("threadHelper Loaded");

        return new ThreadHelper();
    }

    @Bean
    @Lazy
    public QueueClient errorQueueReceiverClient() {

        return new QueueClientBuilder()
            .connectionString(properties.getProperty("azure.storage.queue.connection.string"))
            .queueName(properties.getProperty("error.azure.queue.source.stage2.exception"))
            .buildClient();
    }

    @Bean
    @Lazy
    public OracleDataSource oracleDataSource() {

        String jdbcUrl = properties.getProperty("oracle.aq.jms.url");
        String username = properties.getProperty("oracle.aq.jms.username");
        String password = properties.getProperty("oracle.aq.jms.password");

       logger.info("OracleDataSource bean created");
        return new OracleDataSource(jdbcUrl, username, password);
    }

    @Bean
    @Lazy
    public JMSHelper jmsHelper() {
       logger.info("JMSHelper bean created");

        return new JMSHelper(properties, aqjmsFactory(), oracleDataSource());
    }

    @Bean
    @Lazy
    public AQJMSFactory aqjmsFactory() {
       logger.info("AQJMSFactory bean created");
        return new AQJMSFactory();
    }
}

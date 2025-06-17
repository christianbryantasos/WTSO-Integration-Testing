package com.asos.ip.config;

import com.asos.ip.helper.BlobStorage.BlobStorageConnectionManager;
import com.asos.ip.helper.QueueStorage.QueueStorageConnectionManager;
import com.asos.ip.helper.SFTP.SFTPConnectionManager;
import com.asos.ip.helper.ServiceBus.ServiceBusConnectionManager;
import com.asos.ip.helper.TableStorage.TableStorageConnectionManager;
import com.asos.ip.helper.mongoDB.MongoDBConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;

public class EndpointManager {

    public MongoDBConnectionManager mongoDBConnectionManager;
    public ServiceBusConnectionManager serviceBusConnectionManager;
    public SFTPConnectionManager sftpConnectionManager;
    public BlobStorageConnectionManager blobStorageConnectionManager;
    public QueueStorageConnectionManager queueStorageConnectionManager;
    public TableStorageConnectionManager tableStorageConnectionManager;

    @Autowired
    public EndpointManager(MongoDBConnectionManager mongoDBConnectionManager, ServiceBusConnectionManager serviceBusConnectionManager,
                           SFTPConnectionManager sftpConnectionManager, BlobStorageConnectionManager blobStorageConnectionManager,
                           QueueStorageConnectionManager queueStorageConnectionManager, TableStorageConnectionManager tableStorageConnectionManager) {
        this.mongoDBConnectionManager = mongoDBConnectionManager;
        this.serviceBusConnectionManager = serviceBusConnectionManager;
        this.sftpConnectionManager = sftpConnectionManager;
        this.blobStorageConnectionManager = blobStorageConnectionManager;
        this.queueStorageConnectionManager = queueStorageConnectionManager;
        this.tableStorageConnectionManager = tableStorageConnectionManager;
    }

    public ServiceBusConnectionManager getServiceBusConnectionManager() { return serviceBusConnectionManager; }

    public MongoDBConnectionManager getMongoDBConnectionManager() { return mongoDBConnectionManager; }

    public BlobStorageConnectionManager getBlobStorageConnectionManager() { return blobStorageConnectionManager; }

    public TableStorageConnectionManager getTableStorageConnectionManager() { return tableStorageConnectionManager; }

    public SFTPConnectionManager getSftpConnectionManager() { return sftpConnectionManager; }

    public QueueStorageConnectionManager getQueueStorageConnectionManager() { return queueStorageConnectionManager; }
}

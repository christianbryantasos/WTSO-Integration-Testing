package com.asos.ip.helper.JMS;

import javax.jms.JMSException;
import javax.jms.TopicConnectionFactory;
import javax.sql.DataSource;

/**
 * Factory class that creates a TopicConnectionFactory allowing message uploads to AQJMS Topics
 */
public class AQJMSFactory {

    public AQJMSFactory() {}

    /**
     * @param dataSource The Datasource to create a connection to
     * @return
     * @throws JMSException
     */
    public TopicConnectionFactory getTopicConnectionFactory(DataSource dataSource) throws JMSException {
        return oracle.jms.AQjmsFactory.getTopicConnectionFactory(dataSource);
    }

}

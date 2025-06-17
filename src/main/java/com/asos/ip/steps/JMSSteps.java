package com.asos.ip.steps;

import com.asos.ip.helper.JMS.AQJMSFactory;
import com.asos.ip.helper.JMS.JMSHelper;
import com.asos.ip.helper.JMS.OracleDataSource;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.io.File;
import javax.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;

public class JMSSteps {

    private JMSHelper jmsHelper;
    private AQJMSFactory aqjmsFactory;
    private OracleDataSource dataSource;
    private String filePath;

    @Autowired
    public JMSSteps(JMSHelper jmsHelper, AQJMSFactory aqjmsFactory, OracleDataSource dataSource) {
        this.jmsHelper = jmsHelper;
        this.aqjmsFactory = aqjmsFactory;
        this.dataSource = dataSource;
    }

    @Given("I have a file at {string} to publish to JMS")
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @When("I publish a file from path {string} to JMS Publisher target topic")
    public void publishMessageToJms(String filePath) throws JMSException {
        File xmlFile = new File(filePath);
        jmsHelper.putMessageFromXmlFile(xmlFile);

    }

}

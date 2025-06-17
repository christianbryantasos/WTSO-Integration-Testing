package com.asos.ip.config;


import com.asos.ip.helper.mongoDB.MongoDBConnectionManager;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;

@CucumberContextConfiguration
@ContextHierarchy({
        @ContextConfiguration(classes = SpringConfig.class),
        @ContextConfiguration(classes = MongoDBConnectionManager.class)
})
public class CucumberSpringConfiguration {
}

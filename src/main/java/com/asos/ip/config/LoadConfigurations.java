package com.asos.ip.config;

import com.asos.ip.helper.configLoader.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

public class LoadConfigurations {

    private static final Logger logger = LoggerFactory.getLogger(LoadConfigurations.class);

    public Properties getConfigProperties() {
        return configProperties;
    }

    // Properties object to hold configuration settings
    public Properties configProperties;


   public void initialize() {
       String configPath = "environments/";
       String environment = System.getProperty("ENVIRONMENT");
       logger.info("ENVIRONMENT system property: " + environment);
       // Check if the environment is set; if not, default to "local"
       if (environment == null) {
           logger.error("ENVIRONMENT system property is not set. Defaulting to local environment.");
           environment = "local"; // Set a default to avoid null
       }

       logger.info("Loading configuration files...");
       loadConfigurations(configPath, environment);
       logger.info("Configurations loaded...");
   }

    /**
     * Loads the configuration properties from the specified path and environment.
     *
     * @param configPath The path to the configuration files.
     * @param environment The environment name to load specific configurations.
     */
    private void loadConfigurations(String configPath, String environment) {
        ConfigLoader configLoader = new ConfigLoader();
        Map<String, Object> configMap = configLoader.loadAnyConfigFile(configPath, environment);
        configProperties = configLoader.convertMapToProperties(configMap);
    }

}
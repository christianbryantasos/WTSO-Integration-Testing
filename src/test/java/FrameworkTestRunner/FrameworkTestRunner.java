package FrameworkTestRunner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.asos.ip")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:") // Extent Reports plugin
public class FrameworkTestRunner {
}
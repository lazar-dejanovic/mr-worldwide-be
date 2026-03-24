package com.raf.mrworldwide;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Runs only the user-management feature scenarios.
 * Use this class in the IDE to execute user tests in isolation,
 * or from the CLI:
 *   ./gradlew integrationTest -Dcucumber.filter.tags="@user"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/user.feature")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.raf.mrworldwide")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:build/reports/cucumber/user.html")
public class UserIT {
}


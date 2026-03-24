package com.raf.mrworldwide;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Runs only the trip-plan feature scenarios.
 * Use this class in the IDE to execute trip-plan tests in isolation,
 * or from the CLI:
 *   ./gradlew integrationTest -Dcucumber.filter.tags="@trip-plan"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/trip_plan.feature")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.raf.mrworldwide")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:build/reports/cucumber/trip-plan.html")
public class TripPlanIT {
}


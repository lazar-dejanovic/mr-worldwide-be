package com.raf.mrworldwide.context;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Holds per-scenario state shared across step definition classes.
 * A new instance is created for every Cucumber scenario thanks to {@code @ScenarioScope}.
 */
@Component
@ScenarioScope
public class ScenarioContext {

    private String accessToken;
    private UUID lastTripPlanId;
    private int lastStatusCode;
    private String lastResponseBody;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public UUID getLastTripPlanId() {
        return lastTripPlanId;
    }

    public void setLastTripPlanId(UUID lastTripPlanId) {
        this.lastTripPlanId = lastTripPlanId;
    }

    public int getLastStatusCode() {
        return lastStatusCode;
    }

    public void setLastStatusCode(int lastStatusCode) {
        this.lastStatusCode = lastStatusCode;
    }

    public String getLastResponseBody() {
        return lastResponseBody;
    }

    public void setLastResponseBody(String lastResponseBody) {
        this.lastResponseBody = lastResponseBody;
    }
}


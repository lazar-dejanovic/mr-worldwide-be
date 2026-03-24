package com.raf.mrworldwide.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raf.mrworldwide.context.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TripPlanSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioContext scenarioContext;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(scenarioContext.getAccessToken());
        return headers;
    }

    private ResponseEntity<String> doRegisterAndLogin(String email, String password) {
        Map<String, String> regBody = Map.of(
                "firstName", "Test", "lastName", "User", "email", email, "password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange("/api/users/register", HttpMethod.POST,
                new HttpEntity<>(regBody, headers), String.class);

        ResponseEntity<String> loginResponse = restTemplate.exchange(
                "/api/users/login", HttpMethod.POST,
                new HttpEntity<>(Map.of("email", email, "password", password), headers),
                String.class);
        return loginResponse;
    }

    private UUID doCreateTripPlan(String name, String startDate, String endDate) throws Exception {
        Map<String, Object> body = Map.of(
                "name", name,
                "startDate", startDate,
                "endDate", endDate,
                "interests", java.util.List.of()
        );
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/trips", HttpMethod.POST,
                new HttpEntity<>(body, authHeaders()),
                String.class);
        JsonNode json = objectMapper.readTree(response.getBody());
        return UUID.fromString(json.get("base").get("id").asText());
    }

    // -------------------------------------------------------------------------
    // Background step
    // -------------------------------------------------------------------------

    @Given("I am registered and logged in as {string} with password {string}")
    public void iAmRegisteredAndLoggedIn(String email, String password) throws Exception {
        ResponseEntity<String> loginResponse = doRegisterAndLogin(email, password);
        JsonNode json = objectMapper.readTree(loginResponse.getBody());
        scenarioContext.setAccessToken(json.get("accessToken").asText());
    }

    // -------------------------------------------------------------------------
    // Trip plan creation
    // -------------------------------------------------------------------------

    @When("I create a trip plan with name {string}, startDate {string}, endDate {string}")
    public void iCreateATripPlan(String name, String startDate, String endDate) {
        Map<String, Object> body = Map.of(
                "name", name,
                "startDate", startDate,
                "endDate", endDate,
                "interests", java.util.List.of()
        );
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/trips", HttpMethod.POST,
                new HttpEntity<>(body, authHeaders()),
                String.class);
        scenarioContext.setLastStatusCode(response.getStatusCode().value());
        scenarioContext.setLastResponseBody(response.getBody());

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                JsonNode json = objectMapper.readTree(response.getBody());
                UUID id = UUID.fromString(json.get("base").get("id").asText());
                scenarioContext.setLastTripPlanId(id);
            } catch (Exception ignored) {
            }
        }
    }

    @And("the trip plan is persisted with name {string}")
    public void theTripPlanIsPersistedWithName(String expectedName) throws Exception {
        UUID id = scenarioContext.getLastTripPlanId();
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/trips/" + id, HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        JsonNode json = objectMapper.readTree(response.getBody());
        assertThat(json.get("name").asText()).isEqualTo(expectedName);
    }

    // -------------------------------------------------------------------------
    // Pre-condition: I have a trip plan
    // -------------------------------------------------------------------------

    @Given("I have a trip plan with name {string}")
    public void iHaveATripPlanWithName(String name) throws Exception {
        UUID id = doCreateTripPlan(name, "2026-07-01", "2026-07-14");
        scenarioContext.setLastTripPlanId(id);
    }

    // -------------------------------------------------------------------------
    // Update name
    // -------------------------------------------------------------------------

    @When("I update the trip plan name to {string}")
    public void iUpdateTheTripPlanNameTo(String newName) {
        Map<String, Object> body = Map.of(
                "name", newName,
                "startDate", "2026-07-01",
                "endDate", "2026-07-14",
                "interests", java.util.List.of()
        );
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/trips/" + scenarioContext.getLastTripPlanId(),
                HttpMethod.PUT,
                new HttpEntity<>(body, authHeaders()),
                String.class);
        scenarioContext.setLastStatusCode(response.getStatusCode().value());
        scenarioContext.setLastResponseBody(response.getBody());
    }

    @And("the response body contains name {string}")
    public void theResponseBodyContainsName(String expectedName) throws Exception {
        JsonNode json = objectMapper.readTree(scenarioContext.getLastResponseBody());
        assertThat(json.get("name").asText()).isEqualTo(expectedName);
    }

    // -------------------------------------------------------------------------
    // Non-owner 403
    // -------------------------------------------------------------------------

    @Given("another user {string} with password {string} owns a trip plan")
    public void anotherUserOwnsATripPlan(String email, String password) throws Exception {
        // Save current user's token, switch to the other user, create a trip, restore
        String originalToken = scenarioContext.getAccessToken();

        ResponseEntity<String> loginResponse = doRegisterAndLogin(email, password);
        JsonNode json = objectMapper.readTree(loginResponse.getBody());
        scenarioContext.setAccessToken(json.get("accessToken").asText());

        UUID id = doCreateTripPlan("Other User's Trip", "2026-08-01", "2026-08-10");
        scenarioContext.setLastTripPlanId(id);

        // Restore original user's token
        scenarioContext.setAccessToken(originalToken);
    }

    @When("I try to get that trip plan")
    public void iTryToGetThatTripPlan() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/trips/" + scenarioContext.getLastTripPlanId(),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);
        scenarioContext.setLastStatusCode(response.getStatusCode().value());
        scenarioContext.setLastResponseBody(response.getBody());
    }

    // -------------------------------------------------------------------------
    // Status transitions
    // -------------------------------------------------------------------------

    @When("I update the trip plan status to {string}")
    public void iUpdateTheTripPlanStatusTo(String status) {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/trips/" + scenarioContext.getLastTripPlanId() + "/status?status=" + status,
                HttpMethod.PATCH,
                new HttpEntity<>(authHeaders()),
                String.class);
        scenarioContext.setLastStatusCode(response.getStatusCode().value());
        scenarioContext.setLastResponseBody(response.getBody());
    }

    @And("the trip plan status has been advanced to {string}")
    public void theTripPlanStatusHasBeenAdvancedTo(String status) {
        iUpdateTheTripPlanStatusTo(status);
        assertThat(scenarioContext.getLastStatusCode())
                .as("Pre-condition: status advance to " + status)
                .isEqualTo(200);
    }

    @And("the response body contains status {string}")
    public void theResponseBodyContainsStatus(String expectedStatus) throws Exception {
        JsonNode json = objectMapper.readTree(scenarioContext.getLastResponseBody());
        assertThat(json.get("status").asText()).isEqualTo(expectedStatus);
    }

}




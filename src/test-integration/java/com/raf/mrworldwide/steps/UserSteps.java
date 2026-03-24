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

import static org.assertj.core.api.Assertions.assertThat;

public class UserSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioContext scenarioContext;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Reusable helper: register a user and return the response body as JsonNode
    // -------------------------------------------------------------------------

    private ResponseEntity<String> doRegister(String firstName, String lastName,
                                               String email, String password) {
        Map<String, String> body = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "email", email,
                "password", password
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(
                "/api/users/register",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );
    }

    private ResponseEntity<String> doLogin(String email, String password) {
        Map<String, String> body = Map.of("email", email, "password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(
                "/api/users/login",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );
    }

    // -------------------------------------------------------------------------
    // Step definitions
    // -------------------------------------------------------------------------

    @When("I register with firstName {string}, lastName {string}, email {string}, password {string}")
    public void iRegister(String firstName, String lastName, String email, String password) {
        ResponseEntity<String> response = doRegister(firstName, lastName, email, password);
        scenarioContext.setLastStatusCode(response.getStatusCode().value());
        scenarioContext.setLastResponseBody(response.getBody());
    }


    @When("I login with email {string} and password {string}")
    public void iLogin(String email, String password) {
        ResponseEntity<String> response = doLogin(email, password);
        scenarioContext.setLastStatusCode(response.getStatusCode().value());
        scenarioContext.setLastResponseBody(response.getBody());

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode token = json.get("accessToken");
                if (token != null && !token.isNull()) {
                    scenarioContext.setAccessToken(token.asText());
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(scenarioContext.getLastStatusCode())
                .as("HTTP status code")
                .isEqualTo(expectedStatus);
    }

    @And("the response body contains email {string}")
    public void theResponseBodyContainsEmail(String expectedEmail) throws Exception {
        JsonNode json = objectMapper.readTree(scenarioContext.getLastResponseBody());
        assertThat(json.get("email").asText()).isEqualTo(expectedEmail);
    }

    @And("the response body contains a non-empty access token")
    public void theResponseBodyContainsANonEmptyAccessToken() throws Exception {
        JsonNode json = objectMapper.readTree(scenarioContext.getLastResponseBody());
        String token = json.get("accessToken").asText();
        assertThat(token).isNotBlank();
    }
}



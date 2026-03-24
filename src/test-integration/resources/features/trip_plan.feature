@trip-plan
Feature: Trip plan management

  Background:
    Given I am registered and logged in as "tripper@test.com" with password "password123"

  Scenario: Authenticated user creates a trip plan and it is persisted
    When I create a trip plan with name "European Adventure", startDate "2026-06-01", endDate "2026-06-30"
    Then the response status should be 201
    And the trip plan is persisted with name "European Adventure"

  Scenario: Owner can update their trip plan
    Given I have a trip plan with name "Asia Tour"
    When I update the trip plan name to "Asia Grand Tour"
    Then the response status should be 200
    And the response body contains name "Asia Grand Tour"

  Scenario: Non-owner receives 403 when accessing another user's trip plan
    Given another user "other@test.com" with password "password123" owns a trip plan
    When I try to get that trip plan
    Then the response status should be 403

  Scenario: Valid status transition from DRAFT to PLANNED succeeds
    Given I have a trip plan with name "Paris Trip"
    When I update the trip plan status to "PLANNED"
    Then the response status should be 200
    And the response body contains status "PLANNED"

  Scenario: Invalid status transition from PLANNED to COMPLETED returns 400
    Given I have a trip plan with name "Rome Trip"
    And the trip plan status has been advanced to "PLANNED"
    When I update the trip plan status to "COMPLETED"
    Then the response status should be 400


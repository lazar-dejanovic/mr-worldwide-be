Feature: User management

  Scenario: Register a new user
    When I register with firstName "Alice", lastName "Smith", email "alice@test.com", password "password123"
    Then the response status should be 200
    And the response body contains email "alice@test.com"

  Scenario: Login with valid credentials returns a JWT token
    Given I register with firstName "Bob", lastName "Jones", email "bob@test.com", password "password123"
    When I login with email "bob@test.com" and password "password123"
    Then the response status should be 200
    And the response body contains a non-empty access token

  Scenario: Login with wrong password returns 400
    Given I register with firstName "Carol", lastName "Brown", email "carol@test.com", password "password123"
    When I login with email "carol@test.com" and password "wrongpassword"
    Then the response status should be 400


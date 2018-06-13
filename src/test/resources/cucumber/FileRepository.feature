Feature: File Repository

  As a user,
  I want to be able to read or write files in spite of concurrency
  In order to ensure my file is valid

  @NoConcurrency
  Scenario: single user requests to read a file
    Given user "user1" granted to READ
    And file "test.txt" containing "test"
    When user "user1" requests to read the file "test.txt"
    Then user "user1" received value "test"

  @NoConcurrency
  Scenario: single user requests to write a file
    Given user "user1" granted to WRITE
    And file "test.txt" containing "test"
    When user "user1" requests to write "test user1" to the file "test.txt"
    Then file "test.txt" has been saved 1 times
    And file "test.txt" contains "test user1"

  @NoConcurrency
  Scenario: two users request to read a different file at the same time
    Given user "user1" granted to READ
    And user "user2" granted to READ
    And file "test1.txt" containing "test1"
    And file "test2.txt" containing "test2"
    When following requests are executed at the same time in the following order:
      | Order | User  | Action | File      | Duration |
      | 1     | user1 | READ   | test1.txt | 10       |
      | 2     | user2 | READ   | test2.txt | 5        |
    Then user "user1" received value "test1"
    And user "user2" received value "test2"

  @NoConcurrency
  Scenario: two users request to write a different file
    Given user "user1" granted to WRITE
    And user "user2" granted to WRITE
    And file "test1.txt" containing "test1"
    And file "test2.txt" containing "test2"
    When following requests are executed at the same time in the following order:
      | Order | User  | Action            | File      | Content     | Duration |
      | 1     | user1 | CREATE_OR_REPLACE | test1.txt | test1 user1 | 10       |
      | 2     | user2 | CREATE_OR_REPLACE | test2.txt | test2 user2 | 5        |
    Then file "test1.txt" has been saved 1 times
    And file "test2.txt" has been saved 1 times
    And file "test1.txt" contains "test1 user1"
    And file "test2.txt" contains "test2 user2"

  @Concurrency
  Scenario: two users request to read the same file at the same time
    Given user "user1" granted to READ
    And user "user2" granted to READ
    And file "test.txt" containing "test"
    When following requests are executed at the same time in the following order:
      | Order | User  | Action | File     | Duration |
      | 1     | user1 | READ   | test.txt | 10       |
      | 2     | user2 | READ   | test.txt | 5        |
    Then user "user1" received value "test"
    And user "user2" received value "test"

  @Concurrency
  Scenario: two users request to write the same file at the same time
    Given user "user1" granted to WRITE
    And user "user2" granted to WRITE
    And file "test.txt" containing "test"
    When following requests are executed at the same time in the following order:
      | Order | User  | Action            | File     | Content    | Duration |
      | 1     | user1 | CREATE_OR_REPLACE | test.txt | test user1 | 10       |
      | 2     | user2 | CREATE_OR_REPLACE | test.txt | test user2 | 5        |
    Then user "user2" received exception "LockException"
    And file "test.txt" has been saved 1 times
    And file "test.txt" contains "test user1"

  @Concurrency
  Scenario: a first user requests to write a file then a second user requests to read the same file at the same time
    Given user "user1" granted to WRITE
    And user "user2" granted to READ
    And file "test.txt" containing "test"
    When following requests are executed at the same time in the following order:
      | Order | User  | Action            | File     | Content    | Duration |
      | 1     | user1 | CREATE_OR_REPLACE | test.txt | test user1 | 10       |
      | 2     | user2 | READ              | test.txt |            | 5        |
    Then user "user2" received exception "LockException"
    And file "test.txt" has been saved 1 times
    And file "test.txt" contains "test user1"

  @Concurrency
  Scenario: a first user requests to read a file then a second user requests to write the same file at the same time
    Given user "user1" granted to READ
    And user "user2" granted to WRITE
    And file "test.txt" containing "test"
    When following requests are executed at the same time in the following order:
      | Order | User  | Action            | File     | Content    | Duration |
      | 1     | user1 | READ              | test.txt |            | 10       |
      | 2     | user2 | CREATE_OR_REPLACE | test.txt | test user2 | 5        |
    Then user "user2" received exception "LockException"
    And file "test.txt" has not been saved
    And file "test.txt" contains "test"
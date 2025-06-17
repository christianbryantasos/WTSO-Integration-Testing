@framework
Feature: Invoking the WTSO Logic app to Enrich data

  Background:
    Given I have the following Service Bus Subscription test data to clear down:
      | ciris | wtsotest | "119934168" |

  Scenario: Invoking logic app to enrich data and send message to enriched service bus topic
    Given I have a file at "optionData.json" to upload
    When I send a message to the "ciris" service bus topic "wtsotest" with the body in file "optionData.json"
    Then Target subscription "wtsoenrichtest" in "ciris" service bus should contain 1 new messages, with "119934168" in the "optionId" field in body



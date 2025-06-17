@framework
  Feature: Service Bus Connection Tests
#
#    Background:
#      Given I have the following Service Bus Subscription test data to clear down:
#      | systemtest | completeAllMessagesInSubscription |
#
#    Scenario: Service Bus Happy Path Test
#      Given I have a file at "servicebusTest.json" to upload
#      When I send a message to the service bus topic "systemtest" with the body in file "servicebusTest.json"
#      Then Target subscription "systemtest" should contain 1 new message matching the JSON body in file "servicebusTest.json" ignoring fields "EventTime, Data.ClientRequestId"

    Background:
    Given I have the following Service Bus Subscription test data to clear down:
      | ris | systemtest | "northlondonderby" |
      | ris | systemtest | "north2londonderby2" |

    Scenario: Service Bus Clear Down Test
      Given I have a file at "testFile.json" to upload
      And I have a file at "testFile2.json" to upload
      When I send a message to the "ris" service bus topic "systemtest" with the body in file "testFile.json"
      And I send a message to the "ris" service bus topic "systemtest" with the body in file "testFile2.json"
      Then Target subscription "systemtest" in "ris" service bus should contain 1 new messages, with "northlondonderby" in the "test" field in body
      Then Target subscription "systemtest" in "ris" service bus should contain 1 new messages, with "north2londonderby2" in the "test" field in body

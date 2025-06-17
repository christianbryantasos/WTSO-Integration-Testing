@framework
  Feature: QueueStorage Connection Tests

    Background:
      Given I have the following Queue Storage test data to clear down:
        | ris | systemtest | test |
        #| sit | systemtest | test |

    Scenario: Testing Queue Storage File Upload
      Given I have a file at "testFile.json" to upload
      When I send a message with the string "test" to the queue "systemtest" in the queue storage account "ris"
      #And I send a message with the string "test" to the queue "systemtest" in the queue storage account "sit"
      Then I wait for flow to process for 10 seconds
      Then A new message containing the string "test" should be found in the queue "systemtest" in the queue storage account "ris"
      #And A new message containing the string "test" should be found in the queue "systemtest" in the queue storage account "sit"
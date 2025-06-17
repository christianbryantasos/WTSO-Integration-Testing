@framework
Feature: MongoDB Connection Tests

  Background:
    Given I have the following MongoDB collection test data to clear down:
      | dev-server | baseproductlocation | exported | mongoDbJsonId |
      | item-ci-server | baseproductlocation | published | mongoDbJsonId |

  Scenario: MongoDB Standard Upload and Assert Test
    Given I have a file at "mongoDBTestInput.json" to upload
    When I upload the file "mongoDBTestInput.json" to the MongoDB "dev-server" server in the "baseproductlocation" database in the "exported" collection
    And the MongoDB "dev-server" server in the "baseproductlocation" database in the "exported" collection does not contain a document with ID "mongoDbJsonId"
    And I upload the file "mongoDBTestInput.json" to the MongoDB "dev-server" server in the "baseproductlocation" database in the "exported" collection
    And I upload the file "mongoDBTestInput.json" to the MongoDB "item-ci-server" server in the "baseproductlocation" database in the "published" collection
    And I wait for flow to process for 10 seconds
    Then Document with ID "mongoDbJsonId" in the MongoDB "dev-server" server in the "baseproductlocation" database in the "exported" collection should match the expected output in the file "mongoDBTestOutput.json" ignoring fields ""
    And the document with ID "mongoDbJsonId" is present in the MongoDB "item-ci-server" server in the "baseproductlocation" database in the "published" collection




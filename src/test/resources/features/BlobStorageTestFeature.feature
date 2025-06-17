@framework
Feature: Testing BlobStorageConnectionManager

#  Background:
#   Given I have the following Blob Storage test data to clear down:
#     | devDatabase | ris | testFile.json |
#     | devDatabase | ris2 | testFile.json |
#
#  Scenario: Uploading a File to two different Blob Containers in the Same Database
#    Given I have a file at "testFile.json" to upload
#    When I upload file "testFile.json" to Blob Storage Account "devDatabase" to Container "ris" at path "testFile.json"
#    And I upload file "testFile.json" to Blob Storage Account "devDatabase" to Container "ris2" at path "testFile.json"
#    Then There should be a file in Blob Storage Account "devDatabase" in Container "ris" with the filename "testFile.json"
#    Then There should be a file in Blob Storage Account "devDatabase" in Container "ris2" with the filename "testFile.json"
#    And There should be a file in Blob Storage Account "devDatabase" in Container "ris" that contains string "northlondonderby"
#    And There should be a file in Blob Storage Account "devDatabase" in Container "ris2" that contains string "northlondonderby"

    ###################################################################################

#  Background:
#   Given I have the following Blob Storage test data to clear down:
#     | devDatabase | ris | test/yyyy/MM/dd/testFile.json |
#
#  Scenario: Testing upload and clear down for dynamic dateTime
#    Given I have a file at "testFile.json" to upload
#    When I upload file "testFile.json" to Blob Storage Account "devDatabase" to Container "ris" at path "test/yyyy/MM/dd/testFile.json"
#    Then There should be a file in Blob Storage Account "devDatabase" in Container "ris" with the filename "test/yyyy/MM/dd/testFile.json"

    ###################################################################################

  Background:
    Given I have the following Blob Storage test data to clear down:
      | ris | bam020m | testFile.json |
      | ris | bam020 | testFile.json |
      | sit | ingestsystem | testFile.json |
      | sit | ingestbam020m | testFile.json |

  Scenario: Uploading a File to two different Blob Containers in the Same Database
    Given I have a file at "testFile.json" to upload
    When I upload file "testFile.json" to Blob Storage Account "ris" to Container "bam020m" at path "testFile.json"
    And I upload file "testFile.json" to Blob Storage Account "ris" to Container "bam020" at path "testFile.json"
    And I upload file "testFile.json" to Blob Storage Account "sit" to Container "ingestsystem" at path "testFile.json"
    And I upload file "testFile.json" to Blob Storage Account "sit" to Container "ingestbam020m" at path "testFile.json"
    And I wait for flow to process for 10 seconds
    Then There should be a file in Blob Storage Account "ris" in Container "bam020m" with the filename "testFile.json"
    And There should be a file in Blob Storage Account "ris" in Container "bam020" with the filename "testFile.json"
    And There should be a file in Blob Storage Account "sit" in Container "ingestsystem" with the filename "testFile.json"
    And There should be a file in Blob Storage Account "sit" in Container "ingestbam020m" with the filename "testFile.json"
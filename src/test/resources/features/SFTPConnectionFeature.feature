@framework
  Feature: SFTP Connection feature tests

    #################### SCENARIO ONE #####################
#   1 FILE / 1 PATH / 1 HOST

#    Background:
#      Given I have the following SFTP hosts, their path tags and respective test data to clear down:
#        | ris | primary_host_first_path | testFile.json |
#
#    Scenario: Connecting to the SFTP Server, Uploading 1 File to 1 Path
#      Given I have a file at "testFile.json" to upload
#      When I upload a file to the SFTP Host tagged as "ris", to the SFTP Path tagged as "primary_host_first_path", the file named "testFile.json"
#      Then I should see in the SFTP Host tagged as "ris", in the SFTP Path tagged as "primary_host_first_path", the file named "testFile.json"

    ##################### SCENARIO TWO #####################
    # 2 FILES / 2 PATHS / 1 HOST

#    Background:
#      Given I have the following SFTP hosts, their path tags and respective test data to clear down:
#        | ris | primary_host_first_path | testFile.json |
#        | ris | primary_host_second_path | testFile2.json |
#
#    Scenario: Connecting to the SFTP Server, Uploading 2 Files to 2 Path, same host
#      Given I have a file at "testFile.json" to upload
#      And I have a file at "testFile2.json" to upload
#      When I upload a file to the SFTP Host tagged as "ris", to the SFTP Path tagged as "primary_host_first_path", the file named "testFile.json"
#      And I upload a file to the SFTP Host tagged as "ris", to the SFTP Path tagged as "primary_host_second_path", the file named "testFile2.json"
#      Then I should see in the SFTP Host tagged as "ris", in the SFTP Path tagged as "primary_host_first_path", the file named "testFile.json"
#      And I should see in the SFTP Host tagged as "ris", in the SFTP Path tagged as "primary_host_second_path", the file named "testFile2.json"

    ##################### SCENARIO THREE #####################
    # 3 FILES / 3 PATHS / 2 HOSTS

#    Background:
#      Given I have the following SFTP hosts, their path tags and respective test data to clear down:
#        | ris | primary_host_first_path | testFile.json |
#        | ris | primary_host_second_path | testFile2.json |
#        | second_host | second_host_first_path | testFile3.json |
#
#    Scenario: Connecting to the SFTP Server, Uploading 3 Files to 3 Paths, two to the primary host, a third file to a different host
#      Given I have a file at "testFile.json" to upload
#      And I have a file at "testFile2.json" to upload
#      And I have a file at "testFile3.json" to upload
#      When I upload a file to the SFTP Host tagged as "ris", to the SFTP Path tagged as "primary_host_first_path", the file named "testFile.json"
#      And I upload a file to the SFTP Host tagged as "ris", to the SFTP Path tagged as "primary_host_second_path", the file named "testFile2.json"
#      And I upload a file to the SFTP Host tagged as "second_host", to the SFTP Path tagged as "second_host_first_path", the file named "testFile3.json"
#      Then I should see in the SFTP Host tagged as "ris", in the SFTP Path tagged as "primary_host_first_path", the file named "testFile.json"
#      And I should see in the SFTP Host tagged as "ris", in the SFTP Path tagged as "primary_host_second_path", the file named "testFile2.json"
#      And I should see in the SFTP Host tagged as "second_host", in the SFTP Path tagged as "second_host_first_path", the file named "testFile3.json"

          ##################### SCENARIO FOUR #####################
    # 4 FILES / 4 PATHS / 2 HOSTS
#
#    Background:
#      Given I have the following SFTP hosts, their path tags and respective test data to clear down:
#        | ris | primary_host_first_path | testFile.json |
#        | ris | primary_host_second_path | testFile2.json |
#        | second_host | second_host_first_path | testFile3.json |
#        | second_host | second_host_second_path | testFile4.json |
#
#    Scenario: Connecting to the SFTP Server, Uploading 4 Files to 4 Paths, two to the primary host, two to the second host
#      Given I have a file at "testFile.json" to upload
#      And I have a file at "testFile2.json" to upload
#      And I have a file at "testFile3.json" to upload
#      And I have a file at "testFile4.json" to upload
#      When I upload a file to the SFTP Host tagged as "ris", to the SFTP Path tagged as "primary_host_first_path", the file named "testFile.json"
#      And I upload a file to the SFTP Host tagged as "ris", to the SFTP Path tagged as "primary_host_second_path", the file named "testFile2.json"
#      And I upload a file to the SFTP Host tagged as "second_host", to the SFTP Path tagged as "second_host_first_path", the file named "testFile3.json"
#      And I upload a file to the SFTP Host tagged as "second_host", to the SFTP Path tagged as "second_host_second_path", the file named "testFile4.json"
#      Then I should see in the SFTP Host tagged as "ris", in the SFTP Path tagged as "primary_host_first_path", the file named "testFile.json"
#      And I should see in the SFTP Host tagged as "ris", in the SFTP Path tagged as "primary_host_second_path", the file named "testFile2.json"
#      And I should see in the SFTP Host tagged as "second_host", in the SFTP Path tagged as "second_host_first_path", the file named "testFile3.json"
#      And I should see in the SFTP Host tagged as "second_host", in the SFTP Path tagged as "second_host_second_path", the file named "testFile4.json"

          ##################### SCENARIO FIVE #####################
  # GENERATE A FILE IN SFTP ENDPOINT

     Background:
       Given I have the following SFTP hosts, their path tags and respective test data to clear down:
          | ciSystemTest | large_file_path | largefile.bin |

      Scenario: Generate an SFTP file in the SFTP endpoint and confirm presence
       When I generate a 1000 megabyte test file in the "ciSystemTest" SFTP server, at the path "large_file_path", with name "largefile.bin"
       Then I should see in the SFTP Host tagged as "ciSystemTest", in the SFTP Path tagged as "large_file_path", the file named "largefile.bin"

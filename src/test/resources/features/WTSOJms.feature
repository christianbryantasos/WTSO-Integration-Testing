@framework
Feature: Invoking the WTSO Logic app to Enrich data via mule flow

  Scenario: Invoking logic app via mule flow to send message to service bus topic

    Given I have a file at 'WTSOItem.xml' to publish to JMS
    When I publish a file from path 'src/test/resources/Input/WTSOItem.xml' to JMS Publisher target topic

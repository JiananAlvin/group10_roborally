@tag
Feature:

  Scenario Outline: Player inputs a name
    Given a player opened the application
    When  player inputs a name "<name>"
    Then this name is assigned to this player
    Examples:
      | name      |
      | test_1    |
      | test_2    |
      | 123333123 |
      | tttttttt  |

#  Scenario Outline: Player chooses a map
#    Given a player has a name "<name>"
#          #when player clicks on get a map
#    When the player chooses a map "<map_num>"
#    Then this map "<map_num_displayed>" is displayed
#    Examples:
#      | name   | map_num | map_num_displayed |
#      | Wenjie | map1    | MAP1_CONTENT      |
#      | Jianan | map2    | MAP2_CONTENT      |
#      | Alice  | map999  | ERROR_MAP_NUM     |

  Scenario Outline: Player chooses a robot character
    Given a player has a name "<name>"
    And having-a-robot status is false
    When choose a robot "<robot-name>"
    Then  "<robot-name>" is assigned to this player
    Examples:
      | name       | robot-name |
      | test1_user | Alice      |


  Scenario Outline: As a player I want to create a room
    Given a player has a name "<name>"
    When player creates a new room with code number <room_number>
    Then there is a new room with code <room_number> in the list of available rooms
    Examples:
      | name | room_number |
      | test1 | 100        |
      | test2 | 234        |

  Scenario Outline: As a player I want to join a room
    Given a player has a name "<name>"
    When player enters a room with code number <room_number>
    Then player is in room <room_number>
    Examples:
      | name | room_number |
      | test1 | 100        |
      | test2 | 212        |

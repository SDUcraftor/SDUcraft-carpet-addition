English | [简体中文](./readme_cn.md)
# Description
This is a [carpet](https://github.com/gnembon/fabric-carpet) addition mod of SDUcraft

----

# Rules

Change the hardness of the deepslate to 1.6
* Type: boolean
* Default value: false
* Valid values: true, false
### armorStandIgnoreShulkerDamage
***  
Prevent shulker bullets from destroying armor stands
* Type: boolean
* Default value: false
* Valid values: true, false
### skipCropLightCheck
***  
Skip light level checks for crops
* Type: boolean
* Default value: false
* Valid values: true, false
### tickRateChangedMessage
***  
Notify all players when the game speed changes. If the game speed is not 20gt/s, notify new players that the game speed has changed.  
Enables /leavemessage command, allowing players to leave messages
* Type: boolean
* Default value: false
* Valid values: true, false
### easyFakePeace
***  
Allow players to quickly control fake peace through the /fakepeace command
* Type: boolean
* Default value: false
* Valid values: true, false
* Category: SDU
    * Use /setfakepeace <dimension> <pos> to set the location of the fake peace switch (the switch must be a lever)
### itemPickUpDelay
***  
Modifie the time it takes for items thrown by players to be picked up again
* Type: int
* Default value: 40
* Suggested value: 40
* Additional notes:
    * Set to 32767 to disable item pickup
    * Cannot be negative
### tntTeleportThroughNetherPortal
***  
Allow TNT to pass teleport through a nether portal
* Type: boolean
* Default value: false
* Valid values: true, false
### disableNetherPortal
***  
Disables Nether portals
* Type: boolean
* Default value: false
* Valid values: true, false
### netherPortalCooldown
***
Modifie the cooldown for entity teleport through a nether portal except player
* Type: int
* Default value: 300
* Suggested value: 300
* Additional notes:
    * Cannot be negative
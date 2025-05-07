[English](./readme.md) | 简体中文
# 简介

这是SDUcraft生存服的 [carpet](https://github.com/gnembon/fabric-carpet) 附属模组
----

# 规则

### brittleDeepSlate
***
将深板岩的硬度变为1.6刚好可以被急迫2效率5下界合金镐秒破
* 类型：boolean
* 默认值：false
* 有效值：true, false
### armorStandIgnoreShulkerDamage
***
阻止潜影贝导弹摧毁盔甲架
* 类型：boolean
* 默认值：false
* 有效值：true, false
### skipCropLightCheck
***
使作物跳过光照检测
* 类型：boolean
* 默认值：false
* 有效值：true, false
### tickRateChangedMessage
***
在游戏速度改变时提示所有玩家，当游戏速度不为20gt/s时提示新玩家当前游戏速度发生改变。
启用/leavemessage 指令，允许玩家进行留言
* 类型：boolean
* 默认值：false
* 有效值：true, false
### easyFakePeace
***
允许玩家通过/fakepeace指令快速操控伪和平
* 类型：boolean
* 默认值：false
* 有效值：true, false
* 分类：SDU
    * 使用/setfakepeace <dimension> <pos>设置伪和平开关的位置(开关必须为拉杆)
### itemPickUpDelay
***
修改被玩家丢出的物品再次被拾取的时间
* 类型：int
* 默认值：40
* 建议值：40
* 补充说明：
    * 设置为32767以禁用物品被拾取
    * 不能为负值
### tntTeleportThroughNetherPortal
***
允许被点燃的TNT穿过地狱门
* 类型：boolean
* 默认值：false
* 有效值：true, false
### disableNetherPortal
***
禁用地狱门
* 类型：boolean
* 默认值：false
* 有效值：true, false
### netherPortalCooldown
***
更改玩家以外实体穿过地狱门的冷却时间
* 类型：int
* 默认值：300
* 建议值：300
* 补充说明：
  * 不能为负值
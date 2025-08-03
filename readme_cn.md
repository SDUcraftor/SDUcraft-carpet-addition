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

----
#easycommand系统
游戏内的快捷命令系统,使用/easycommand即可打开 
![img.png](img.png)
直接点击即可进入二级菜单,菜单内包含[]的按钮均可通过点击执行对应操作(包括简介部分也可以点击转跳到详细介绍)
![img_1.png](img_1.png)

也可以直接使用 /easycommand <子命令名> 来访问特定的子命令界面
所有子命令简要介绍(能直接在游戏内打开就没必要在这里看)
EasyCommand 系统目前包含 11 个子命令：

* 机器状态查询 (machinestatus)
   功能: 查看和管理服务器中各种机器的运行状态
   特点: 支持临时和永久机器，可以监控拉杆和红石灯的开关状态
* 服务器路标 (locationmarker)
   功能: 基于 MCDR 的服务器路标插件界面
   使用方法: 通过 !!loc 命令获取详细信息
* 永昼机 (perpetualday)
   功能: 利用假人模拟玩家睡觉来跳过夜晚
   注意事项: 开关前需告知其他玩家，非必要不要关闭
* 伪和平 (fakepeace)
   注意事项: 开关前需与其他玩家沟通
* 镜像服管理 (mirrormanager)
   功能: 基于 MCDR 的镜像服务器管理插件
   注意事项: 执行操作前确认镜像服中没有人在测试机器
* 旁观者模式 (spectator)
   功能: 基于 MCDR 的游戏模式切换插件
   注意事项: tp 指令只能在旁观者模式下使用
* 游戏速度控制 (tickratemanager)
   功能: 控制游戏刻速度（tick rate）
   注意事项: 更改前需与其他玩家沟通，可使用 /leavemessage 留言
* 警告系统 (warning)
   功能: 管理服务器警告点，当玩家接近时显示警告信息
* 全物品助手 (allitem)
   功能: 自动分类储存 MC 中所有物品的装置管理
   特点: 支持中英文搜索，列表中的元素可以直接点击获取详细信息
* 备份系统 (primebackup)
    功能: 基于 MCDR 的强大备份插件 PrimeBackup 的界面
    使用方法: 通过 !!pb 命令获取详细信息
* 材料列表助手 (syncmatica)
    功能: Syncmatica 模组的材料列表管理，在全物品中高亮对应材质
    特点: 支持与其他玩家共享投影，列表元素可点击获取详细信息 
# AmbServerSpigotPlugin
思服器私人插件

##1.添加的物品

    ·传送书：可以使用"书+末影珍珠"合成，可以标记传送点，实现快速传送；
  
    ·万能雷达：可以使用"绿宝石+罗盘+红石+铁块"合成，放入目标后，可以扫描附近是否有目标对象；
    
    ·蓝图木斧：没有新增物品，复用原版木斧，作为建造时的区域选择工具；
    
##2.增加的命令
    建造蓝图：
        选择区域：
        填充区域：
        挖掘区域：

##3.权限系统
    
    相关指令：/ amb permission <-u|-p> <add|del|get> [player] [permission]
        -u：代表通过uuid直接添加权限，不需要被添加权限的玩家在线，此时[player]为相应uuid；
        -p：代表通过玩家名称添加权限，需要玩家在线，此时[player]为玩家名称；
        [permission]是权限节点，可以随意添加任意字符串；
    
    系统中使用到的权限节点有：
        "amb"：用于控制是否可以使用 /amb 命令，此权限只能通过op添加；
        "amb.tpb"：用于控制是否可以使用传送书；
        "amb.radar"：用于控制是否可以使用雷达；
        "amb.blueprint"：用于控制是否可以使用木斧用于建筑的选择；

##4.游戏规则配置
    
    GameRule.MOB_GRIEFING = false，禁止怪物破坏物品；
        可以通过配置文件节点：server.manage.gamerule.mobGriefing 修改；
    GameRule.DO_FIRE_TICK = false，禁止火蔓延；
        可以通过配置文件节点：server.manage.gamerule.doFireTick 修改；        

##5.配置文件
    
    插件配置文件：/** 传送书相关配置 **/
               tpBookTitle;
               tpBookMenuTitle;
               tpBookPageMax;// 默认可以消耗的点数，没有通货时将消耗此点数
               tpBookCurrencyItem;// 传送消耗的通货
               tpBookCurrencyItemName;// 传送消耗的通货
               deadTpMax;// 死亡传送点最多个数
               privateTpMax;// 私人传送点最多个数
               publicTpMax;// 公共传送点最多个数
               tpBookTpPrice;// 传送消耗通货的数量
               tpBookAddTpPrice;// 新增传送点消耗通货的基础数量，指数增长
           
               /** 雷达相关配置 **/
               "radar.item";// 雷达物品
               "radar.name";// 雷达名称
               "radar.rulu.max.usecount";// 雷达最多使用次数
               "radar.rulu.userprice";// 雷达每次消耗数量 
               "radar.rulu.max.found";// 雷达最多搜索半径
    
    传送书数据文件：tpbSaveData.yml，用于记录玩家的传送点信息；
    
    权限配置文件：permission.yml，用于记录玩家的权限节点；

    
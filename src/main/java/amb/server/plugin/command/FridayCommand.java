package amb.server.plugin.command;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.aip.entity.Friday;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class FridayCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player){
            Player player = (Player)commandSender;
            if (!player.getName().equals("LadyJv")){
                return false;
            }
            if ("friday".equals(s)){
                if (strings.length == 0){
                    if (PluginCore.getFridayInventory() == null){
                        player.sendMessage("Friday 背包里什么也没有!");
                        return true;
                    }
                    Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST);
                    inventory.setContents(PluginCore.getFridayInventory());
                    player.openInventory(inventory);
                    PluginCore.setFridayInventory(null);
                    player.sendMessage("Friday 已清空");
                    return true;
                } else if (strings[0].equals("free")){
                    PluginCore.getFriday().setPlayMode(Friday.PlayMode.FREE);
                    player.sendMessage("Friday 以自由活动");
                    return true;
                } else if (strings[0].equals("follow")){
                    player.sendMessage(player.getName()+":"+PluginConfig.AmbName+" 跟我走");
                    PluginCore.getFriday().setPlayMode(Friday.PlayMode.FOLLOW);
                    PluginCore.getFriday().setFollowTarget(player);
                    player.sendMessage(PluginConfig.AmbName+":马上到！");
                } else if (strings[0].equals("guards")){
                    player.sendMessage(player.getName()+":"+PluginConfig.AmbName+" 守在这");
                    PluginCore.getFriday().setPlayMode(Friday.PlayMode.GUARDS);
                    PluginCore.getFriday().setGuardsLocation(player.getLocation());
                    player.sendMessage(PluginConfig.AmbName+":好的！");
                }
            }
        }
        return false;
    }
}

package amb.server.plugin.core;

import amb.server.plugin.command.FridayCommand;
import amb.server.plugin.command.TpBookCommand;
import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.listener.ManageListener;
import amb.server.plugin.listener.PlayerListener;
import amb.server.plugin.service.aip.AutoPlayService;
import amb.server.plugin.service.aip.entity.Friday;
import amb.server.plugin.service.tpb.TpBookItem;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class PluginCore extends JavaPlugin {
    Logger logger = PluginLogger.getLogger("AmbSP");
    private static PluginCore instance;
    private static Friday friday;
    private static ItemStack[] fridayInventory;

    @Override
    public void onEnable() {
        logger.info("[AmbSP]-启动中……");
        instance = this;
        PluginConfig.init(this);
        TpBookItem.addRecipe(this);
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new ManageListener(), this);
        this.getCommand("tpb").setExecutor(new TpBookCommand());
        this.getCommand("tpbRequest").setExecutor(new TpBookCommand());
        this.getCommand("friday").setExecutor(new FridayCommand());
        World world = this.getServer().getWorld("world");
        PluginConfig.gameRuleConfig.init(world);
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onDisable() {
        logger.info("[AmbSP]-关闭中……");
        if (friday != null){
            fridayInventory = AutoPlayService.getInventory(friday.getBukkitEntity());
            if (fridayInventory != null){
                Block block = friday.getBukkitEntity().getLocation().getBlock();
                block.setType(Material.CHEST);
                ((Chest)block.getState()).getInventory().setContents(fridayInventory);
            }
            AutoPlayService.remove(friday);
        }
    }

    public static PluginCore getInstance() {
        return instance;
    }

    public static Friday getFriday() {
        return friday;
    }

    public static void setFriday(Friday friday) {
        PluginCore.friday = friday;
    }

    public static ItemStack[] getFridayInventory() {
        return fridayInventory;
    }

    public static void setFridayInventory(ItemStack[] fridayInventory) {
        PluginCore.fridayInventory = fridayInventory;
    }
}

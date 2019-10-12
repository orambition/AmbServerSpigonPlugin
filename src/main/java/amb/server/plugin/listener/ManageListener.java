package amb.server.plugin.listener;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.aip.AutoPlayService;
import amb.server.plugin.service.aip.entity.Friday;
import amb.server.plugin.tools.NMSUtil;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ManageListener implements Listener {

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        player.sendTitle(ChatColor.GOLD + "欢迎回到思服器", player.getDisplayName(), 10, 100, 20);
        // 解锁传送书合成表
        player.discoverRecipe(new NamespacedKey(PluginCore.getInstance(), "amb_plugin_tpbook"));
        // 向新玩家添加ai player
        if (PluginCore.getFriday() != null){
            NMSUtil.sendTabListAdd(player, PluginCore.getFriday().getBukkitEntity());
        }
    }

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent event){
        if (event.getPlayer().getName().equals(PluginConfig.AmbName)){
            if (PluginCore.getFriday() != null){
                PluginCore.setFridayInventory(AutoPlayService.getInventory(PluginCore.getFriday().getBukkitEntity()));
                AutoPlayService.remove(PluginCore.getFriday());
            }
        }
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event){
        if (event.getPlayer().getName().equals(PluginConfig.AmbName)){
            Player player = event.getPlayer();
            if (PluginCore.getFriday() == null){
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Friday friday = AutoPlayService.addAutoPlayer(player.getDisplayName(), player.getUniqueId(), player.getLocation());
                        AutoPlayService.initAutoPlayer(friday, player);
                        PluginCore.setFriday(friday);
                    }
                }.runTaskLater(PluginCore.getInstance(), 40);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event){
        // 防止friday射伤玩家
        if (event.getDamager() instanceof Arrow){
            Arrow arrow = (Arrow) event.getDamager();
            if (event.getEntity() instanceof Player) {
                if (arrow.getShooter() instanceof Player) {
                    if (((Player) arrow.getShooter()).hasMetadata(Friday.NPC_FLAG)){
                        Player player = (Player) event.getEntity();
                        player.sendMessage(PluginConfig.AmbName+":你挡住我射箭了");
                        arrow.remove();
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}

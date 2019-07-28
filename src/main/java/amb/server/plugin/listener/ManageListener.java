package amb.server.plugin.listener;

import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.ap.AutoPlayService;
import amb.server.plugin.tools.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ManageListener implements Listener {

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        player.sendTitle(ChatColor.GOLD + "��ӭ�ص�˼����", player.getDisplayName(), 10, 100, 20);
        // �첽����������ϳɱ�
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
                PluginCore.getInstance(),
                new Runnable() {
                    public void run() {
                        player.discoverRecipe(new NamespacedKey(PluginCore.getInstance(), "amb_plugin_tpbook"));
                        Player friday = (Player) AutoPlayService.addAutoPlayer(player.getLocation());
                    }
                },
                500L
        );

    }
}

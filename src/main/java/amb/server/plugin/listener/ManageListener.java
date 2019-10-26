package amb.server.plugin.listener;

import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.permission.PermissionService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import static amb.server.plugin.config.ConstantConfig.AP_RADAR_RECIPE;
import static amb.server.plugin.config.ConstantConfig.TP_BOOK_RECIPE;

public class ManageListener implements Listener {

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        // ����Ȩ��
        PermissionService.setupPermission(player);

        player.sendTitle(ChatColor.GOLD + "��ӭ�ص�˼����", player.getDisplayName(), 10, 100, 20);
        // �첽����������ϳɱ�
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
                PluginCore.getInstance(),
                () -> {
                    player.discoverRecipe(new NamespacedKey(PluginCore.getInstance(), TP_BOOK_RECIPE));
                    player.discoverRecipe(new NamespacedKey(PluginCore.getInstance(), AP_RADAR_RECIPE));
                },
                500L
        );
    }
    @EventHandler
    public void onPlayerQuitServer(PlayerQuitEvent event) {
        // ����Ȩ��
        PermissionService.clearPermission(event.getPlayer());
    }

    /*@EventHandler
    public void mapInit(MapInitializeEvent event){
        MapView mapView = event.getMap();
        System.out.println("inti render = " + mapView.getRenderers().toString());
        System.out.println("init = " + mapView.getScale() + "x=" + mapView.getCenterX() + "y=" + mapView.getCenterZ());
    }*/
}

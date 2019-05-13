package amb.server.plugin.listener;

import amb.server.plugin.init.PluginConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class TpBookListener implements Listener {

    @EventHandler
    public void onPlayerOpenBook(PlayerInteractEvent event){
        event.getPlayer().sendMessage(event.getItem().getItemMeta().getDisplayName());
        event.getPlayer().sendMessage(PluginConfig.configuration.getString("tpb.title"));

    }
}

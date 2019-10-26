package amb.server.plugin.service.utils.map;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.util.List;

public class MapUtil {
    public static final int MAXSIZE = 128;

    public static ItemStack buildMap(Location location, List<Double> tX, List<Double> tY, int range){

        MapView mapView = Bukkit.createMap(location.getWorld());
        mapView.setCenterX((int) location.getX());
        mapView.setCenterZ((int) location.getZ());
        mapView.setScale(MapView.Scale.CLOSEST);
        mapView.setTrackingPosition(true);
        mapView.setUnlimitedTracking(true);

        // 添加自定义渲染器
        mapView.addRenderer(new AmbMapRenderer(location, tX, tY, range));

        //player.sendMap(mapView);
        ItemStack map = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) map.getItemMeta();
        mapMeta.setMapView(mapView);
        map.setItemMeta(mapMeta);
        return map;
    }
}
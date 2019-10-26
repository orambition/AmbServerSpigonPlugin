package amb.server.plugin.service.utils.map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.*;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class AmbMapRenderer extends MapRenderer {
    private boolean onlyOne = true;
    private Location location;
    private List<Double> x,y;
    private int foundRange;
    //private static BufferedImage image = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
    //private Graphics graphics = image.getGraphics();

    /**
     * 注意坐标类型，实际绘制时转为byte
     * @param location
     * @param x
     * @param y
     * @param foundRange
     */
    public AmbMapRenderer(Location location, List<Double> x, List<Double> y, int foundRange) {
        this.location = location.clone();
        this.x = x;
        this.y = y;
        this.foundRange = foundRange;
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if (onlyOne){
            onlyOne = false;
            //MapCursorCollection cursorCollection = canvas.getCursors();
            for (int i = 0; i < x.size(); i++){
                //MapCursor cursor = new MapCursor((byte) (2*x.get(i)), (byte) (2*y.get(i)), (byte) 0,MapCursor.Type.RED_X, true);
                //cursorCollection.addCursor(cursor);
                //canvas.setPixel((int)(64+x.get(i)),(int)(64+y.get(i)), (byte) 16);
                canvas.drawText((int)(58+x.get(i)),(int)(61+y.get(i)), MinecraftFont.Font,  "§"+i*4%53+";[+]");
            }
            //(16 * 8- foundRange)/2 - location.getX()%16
            byte minX = (byte) (64-foundRange/2 - location.getX()%16);
            byte minY = (byte) (64-foundRange/2 - location.getZ()%16);
            byte maxX = (byte) (foundRange + minX);
            byte maxY = (byte) (foundRange + minY);

            for (int i = 0; i < foundRange; i+=4){
                canvas.setPixel(minX,minY+i, (byte) 16);
                canvas.setPixel(minX+i,maxY, (byte) 16);
                canvas.setPixel(maxX,maxY-i, (byte) 16);
                canvas.setPixel(maxX-i,minY, (byte) 16);
            }
        }
    }
}

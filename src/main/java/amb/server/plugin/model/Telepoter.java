package amb.server.plugin.model;

import amb.server.plugin.config.PluginConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static amb.server.plugin.config.PluginConfig.tpBookAddTpPrice;
import static amb.server.plugin.config.PluginConfig.tpBookCurrencyItemName;

public class Telepoter {
    private String num;
    private String name;
    private Location location;
    private String author;
    private int price;
    private int type;// 1公共、2私人、3死亡、4上一地点
    private long ctime;

    public Telepoter(String num, String name, Location location, int type) {
        this.num = num;
        this.name = name;
        this.location = location.clone();
        this.type = type;
    }

    public Telepoter() {

    }

    public Material getItemType(){
        switch (type){
            case 1:
                return PluginConfig.publicTpItem;
            case 2:
                return PluginConfig.privateTpItem;
            case 3:
                return PluginConfig.deadTpItem;
            case 4:
                return PluginConfig.beforeTpItem;
                default:
                    return PluginConfig.privateTpItem;
        }

    }

    public List<String> getItemLore(){
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GOLD + "点击传送到此地点");
        lore.add(ChatColor.RESET +""+ ChatColor.RED +"此操作将花费:");
        lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "- ["+tpBookCurrencyItemName + "x" +tpBookAddTpPrice+"] 或:");
        lore.add(ChatColor.GREEN + "- ["+tpBookAddTpPrice+"页]传送书");
        switch (type){
            case 1:
                lore.add(ChatColor.RESET + "创建者:"+this.getAuthor());
                lore.add(ChatColor.GREEN + "Shift+鼠标左键 设置为快速传送点");
                break;
            case 2:
                lore.add(ChatColor.RESET + this.getWorld());
                lore.add(ChatColor.RESET + this.getXYZ());
                lore.add(ChatColor.RED + "Shift+鼠标右键 可删除地点");
                lore.add(ChatColor.GREEN + "Shift+鼠标左键 设置为快速传送点");
                break;
            case 3:
                lore.add(ChatColor.RESET + "时间:"+this.getCtimeShow());
                break;
            case 4:
                lore.add(ChatColor.RESET + this.getWorld());
                lore.add(ChatColor.RESET + this.getXYZ());
                break;
        }
        return lore;
    }

    public long getCtime() {
        return ctime;
    }
    public String getCtimeShow() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ctime);
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getNum() {
        return num;
    }

    public String getName() {
        return name;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }
    public String getWorld(){
        return "世界:"+location.getWorld().getName();
    }
    public String getXYZ(){
        return "坐标:x="+location.getBlockX()+"y="+location.getBlockY()+"z="+location.getBlockZ();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}

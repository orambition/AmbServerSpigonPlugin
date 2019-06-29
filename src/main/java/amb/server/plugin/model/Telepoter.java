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
    private int type;// 1������2˽�ˡ�3������4��һ�ص�
    private long ctime;

    public Telepoter(String num, String name, Location location, int type) {
        this.num = num;
        this.name = name;
        this.location = location;
        this.type = type;
    }

    public Material getItemType(){
        switch (type){
            case 1:
                return PluginConfig.publicTpItem;
            case 2:
                return PluginConfig.privateTpItem;
            case 3:
                return PluginConfig.deadTpItem;
                default:
                    return PluginConfig.privateTpItem;
        }

    }

    public List<String> getItemLore(){
        List<String> lore = new ArrayList<String>();

        switch (type){
            case 1:
                lore.add(ChatColor.RESET + "������:"+this.getAuthor());
                lore.add(ChatColor.GREEN + "Shift+������ ����Ϊ���ٴ��͵�");
                break;
            case 2:
                lore.add(ChatColor.RESET + this.getWorld());
                lore.add(ChatColor.RESET + this.getXYZ());
                lore.add(ChatColor.RED + "Shift+����Ҽ� ��ɾ���ص�");
                lore.add(ChatColor.GREEN + "Shift+������ ����Ϊ���ٴ��͵�");
                break;
            case 3:
                lore.add(ChatColor.RESET + "ʱ��:"+this.getCtimeShow());
                break;
        }
        lore.add(ChatColor.GOLD + "������͵��˵ص�");
        lore.add(ChatColor.RESET +""+ ChatColor.RED +"�˲���������:");
        lore.add(ChatColor.RESET + "" + ChatColor.GOLD + "- ["+tpBookCurrencyItemName + "x" +tpBookAddTpPrice+"] ��:");
        lore.add(ChatColor.GREEN + "- ["+tpBookAddTpPrice+"ҳ]������");
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

    public Location getLocation() {
        return location;
    }
    public String getWorld(){
        return "����:"+location.getWorld().getName();
    }
    public String getXYZ(){
        return "����:x="+location.getBlockX()+"y="+location.getBlockY()+"z="+location.getBlockZ();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}

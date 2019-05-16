package amb.server.plugin.model;

import org.bukkit.Location;

public class Telepoter {
    private String num;
    private String name;
    private Location location;
    private String author;

    public Telepoter(String num, String name, Location location, String author) {
        this.num = num;
        this.name = name;
        this.location = location;
        this.author = author;
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
        return "ÊÀ½ç:"+location.getWorld().getName();
    }
    public String getXYZ(){
        return "×ø±ê:x="+location.getBlockX()+"y="+location.getBlockY()+"z="+location.getBlockZ();
    }

    public String getAuthor() {
        return author;
    }
}

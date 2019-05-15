package amb.server.plugin.service.tpb;

import amb.server.plugin.model.Telepoter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static amb.server.plugin.config.PluginConfig.saveTpbSaveData;
import static amb.server.plugin.config.PluginConfig.tpbSaveData;

public class TpBookService {
    /**
     * ��ӹ����ص�
     * @param player
     */
    public static void addPublicTeleporter(Player player, String name){
        if (player.hasPermission("op")){
            player.sendMessage("op");
            int pNum = tpbSaveData.getInt("public.num",0);
            tpbSaveData.set("public.tp."+pNum+".name",name);
            tpbSaveData.set("public.tp."+pNum+".location",player.getLocation());
            tpbSaveData.set("public.tp."+pNum+".author",player.getDisplayName());
            tpbSaveData.set("public.num",pNum+1);
            saveTpbSaveData();
        }
    }
    /**
     * ��ȡ���й����ص�
     */
    public static List<Telepoter> getAllPublicTeleporter(){
        List<Telepoter> telepoters = new ArrayList<Telepoter>();
        if(tpbSaveData.contains("public.tp")){
            for (String num : tpbSaveData.getConfigurationSection("public.tp").getKeys(false)){
                Telepoter telepoter = new Telepoter(num,
                        tpbSaveData.getString("public.tp."+num+".name","�����ص�"),
                        (Location) tpbSaveData.get("public.tp."+num+".location"),
                        tpbSaveData.getString("public.tp."+num+".author","admin"));
                telepoters.add(telepoter);
            }
        }
        return telepoters;
    }
    /**
     * ɾ�������ص�
     * @param player
     */
    public static void delPublicTeleporter(Player player){
        if (player.hasPermission("op")){
            player.sendMessage("opdel");
        }
    }


    /**
     * ���˽�˵ص�
     * @param player
     */
    public static void addPrivateTeleporter(Player player, String name){
        player.sendMessage("add");
        int pNum = tpbSaveData.getInt("player."+player.getUniqueId().toString()+".num",0);
        tpbSaveData.set("player.tp."+player.getUniqueId().toString()+"."+pNum+".name",name);
        tpbSaveData.set("player.tp."+player.getUniqueId().toString()+"."+pNum+".location",player.getLocation());
        tpbSaveData.set("player."+player.getUniqueId().toString()+".num",pNum++);
    }
    /**
     * ɾ��˽�˵ص�
     * @param player
     */
    public static void delPrivateTeleporter(Player player){
        player.sendMessage("del");
    }

    /**
     * ��������ص�
     * @param player
     */
    public static void addPlayerDeadTeleporter(Player player){
        player.sendMessage("dead");
    }

    /**
     * ���ô��Ϳ���
     */
    public static void setTeleporterSwitch(Player player){

    }
}

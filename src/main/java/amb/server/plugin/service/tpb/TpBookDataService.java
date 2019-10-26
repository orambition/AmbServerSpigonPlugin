package amb.server.plugin.service.tpb;

import amb.server.plugin.model.Telepoter;
import amb.server.plugin.service.utils.GUIUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static amb.server.plugin.config.PluginConfig.*;

public class TpBookDataService {
    /**
     * 添加公共地点
     *
     * @param player
     */
    public static void addPublicTeleporter(Player player, String name) {
        if (player.hasPermission("op")) {
            int pCount = tpbSaveData.getInt("public.count", 0);
            if (pCount == publicTpMax) {
                player.sendMessage("公共地点数量已达到上线，请删除后再添加");
                return;
            }
            int pNum = getPNum("public");
            tpbSaveData.set("public.tp." + pNum + ".name", name);
            tpbSaveData.set("public.tp." + pNum + ".location", player.getLocation());
            tpbSaveData.set("public.tp." + pNum + ".author", player.getDisplayName());
            tpbSaveData.set("public.num", pNum + 1);
            tpbSaveData.set("public.count", pCount + 1);
            saveTpbSaveData();
            player.sendMessage("新增公共传送点成功");
        }
    }

    /**
     * 添加私人地点
     *
     * @param player
     */
    public static void addPrivateTeleporter(Player player, String name, int price) {
        String path = "player." + player.getUniqueId().toString();
        int pCount = tpbSaveData.getInt(path + ".count", 0);
        if (pCount == privateTpMax) {
            player.sendMessage("传送点数量已达到上限\n请删除后再添加");
            return;
        }
        if (player.getInventory().contains(tpBookCurrencyItem, price)){
            player.getInventory().removeItem(new ItemStack(tpBookCurrencyItem, price));
            player.sendMessage(ChatColor.GOLD + "消耗["+tpBookCurrencyItemName+"x"+price+"]");
        }else {
            player.sendMessage("背包中["+tpBookCurrencyItemName+"]不足"+price+"颗!");
            int xp = player.getTotalExperience();
            if (price < 10 && xp >= price * 100){
                player.setTotalExperience(xp-price*100);
                player.sendMessage(ChatColor.GREEN+"已消耗"+price*100+"点[经验值],剩余:"+(xp-price*100));
            }else {
                player.sendMessage(ChatColor.RED+"无法添加新的传送点");
                return;
            }
        }


        int pNum = getPNum(path);
        if (null == name){
            name = "私人传送点"+(pNum+1);
        }
        tpbSaveData.set(path + ".tp." + pNum + ".name", name);
        tpbSaveData.set(path + ".tp." + pNum + ".location", player.getLocation());
        tpbSaveData.set(path + ".num", pNum + 1);
        tpbSaveData.set(path + ".count", pCount + 1);
        saveTpbSaveData();
        //player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.BOLD + ""+ ChatColor.GREEN + "增加私人传送点成功"));
        GUIUtils.sendMsg(player,"增加私人传送点成功");
    }

    /**
     * 添加死亡地点
     *
     * @param player
     */
    public static void addPlayerDeadTeleporter(Player player) {
        String path = "player." + player.getUniqueId().toString() + ".dead";

        int pCount = tpbSaveData.getInt(path + ".count", 0);
        while (pCount >= deadTpMax) {
            String num = tpbSaveData.getConfigurationSection(path + ".tp").getKeys(false).iterator().next();
            tpbSaveData.set(path + ".tp." + num, null);
            pCount--;
        }
        tpbSaveData.set(path + ".count", pCount + 1);

        int pNum = getPNum(path);
        tpbSaveData.set(path + ".tp." + pNum + ".name", "死亡地点" + (pNum+1));
        tpbSaveData.set(path + ".tp." + pNum + ".location", player.getLocation());
        tpbSaveData.set(path + ".tp." + pNum + ".ctime", System.currentTimeMillis());
        tpbSaveData.set(path + ".num", pNum + 1);

        saveTpbSaveData();
        player.sendMessage(ChatColor.GREEN+"死亡地点已记录");
    }

    /**
     * 存储快速传送点
     * @param player
     * @param telepoter
     */
    public static void setFastTeleporter(Player player,Telepoter telepoter){
        if (null != telepoter && StringUtils.isNotBlank(telepoter.getName()) && null != telepoter.getLocation()){
            String path = "player." + player.getUniqueId().toString() + ".fast.tp";
            tpbSaveData.set(path + ".name", telepoter.getName());
            tpbSaveData.set(path + ".location", telepoter.getLocation());
            saveTpbSaveData();
            player.sendMessage(ChatColor.GREEN+"快速传送点已更新");
        }else {
            player.sendMessage(ChatColor.RED+"快速传送点设置失败");
        }
    }

    /**
     * 存储前一地点
     * @param player
     * @param telepoter
     */
    public static void setBegoreTeleporter(Player player,Telepoter telepoter){
        if (null != telepoter && StringUtils.isNotBlank(telepoter.getName()) && null != telepoter.getLocation()){
            String path = "player." + player.getUniqueId().toString() + ".before.tp";
            tpbSaveData.set(path + ".name", telepoter.getName());
            tpbSaveData.set(path + ".location", telepoter.getLocation());
            saveTpbSaveData();
        }else {
            player.sendMessage(ChatColor.RED+"回退地点记录失败");
        }
    }

    /**
     * 获取所有公共地点
     */
    public static List<Telepoter> getAllPublicTeleporter() {
        List<Telepoter> telepoters = new ArrayList<Telepoter>();
        if (tpbSaveData.contains("public.tp")) {
            for (String num : tpbSaveData.getConfigurationSection("public.tp").getKeys(false)) {
                Telepoter telepoter = new Telepoter(num,
                        tpbSaveData.getString("public.tp." + num + ".name", "公共地点"),
                        (Location) tpbSaveData.get("public.tp." + num + ".location"), 1);
                telepoter.setAuthor(tpbSaveData.getString("public.tp." + num + ".author", "admin"));
                telepoters.add(telepoter);
            }
        }
        return telepoters;
    }

    /**
     * 获取所有私人地点
     */
    public static List<Telepoter> getPlayerPrivateTeleporter(String uuid) {
        List<Telepoter> telepoters = new ArrayList<Telepoter>();
        String path = "player." + uuid + ".tp";
        if (tpbSaveData.contains(path)) {
            for (String num : tpbSaveData.getConfigurationSection(path).getKeys(false)) {
                Telepoter telepoter = new Telepoter(num,
                        tpbSaveData.getString(path + "." + num + ".name", "私人地点"),
                        (Location) tpbSaveData.get(path + "." + num + ".location"), 2);
                telepoters.add(telepoter);
            }
        }
        return telepoters;
    }

    /**
     * 获取所有死亡地点
     */
    public static List<Telepoter> getPlayerDeadTeleporter(String uuid) {
        List<Telepoter> telepoters = new ArrayList<Telepoter>();
        String path = "player." + uuid + ".dead.tp";
        if (tpbSaveData.contains(path)) {
            for (String num : tpbSaveData.getConfigurationSection(path).getKeys(false)) {
                Telepoter telepoter = new Telepoter(num,
                        tpbSaveData.getString(path + "." + num + ".name", "死亡地点"),
                        (Location) tpbSaveData.get(path + "." + num + ".location"), 3);
                telepoter.setCtime(tpbSaveData.getLong(path + "." + num + ".ctime"));
                telepoters.add(telepoter);
            }
        }
        return telepoters;
    }

    /**
     * 获取公共地点
     * @param num
     * @return
     */
    public static Telepoter getPublicTeleporterByNum(int num) {
        String path = "public.tp." + num;
        if (tpbSaveData.contains(path)) {
            Telepoter telepoter = new Telepoter(String.valueOf(num),
                    tpbSaveData.getString(path + ".name",null),
                    (Location)tpbSaveData.get(path + ".location",null),
                    1);
            return telepoter;
        }
        return null;
    }

    /**
     * 获取私人地点
     * @param uuid
     * @param num
     * @return
     */
    public static Telepoter getPrivateTeleporterByNum(String uuid, int num) {
        String path = "player." + uuid + ".tp." + num;
        if (tpbSaveData.contains(path)) {
            Telepoter telepoter = new Telepoter(String.valueOf(num),
                    tpbSaveData.getString(path + ".name",null),
                    (Location)tpbSaveData.get(path + ".location",null),
                    1);
            return telepoter;
        }
        return null;
    }

    /**
     * 获取玩家快速传送点
     * @param uuid
     * @return
     */
    public static Telepoter getPrivateFastTeleporter(String uuid) {
        String path = "player." + uuid + ".fast.tp";
        if (tpbSaveData.contains(path)) {
            Telepoter telepoter = new Telepoter("",
                    tpbSaveData.getString(path + ".name",null),
                    (Location)tpbSaveData.get(path + ".location",null),
                    1);
            return telepoter;
        }
        return null;
    }

    /**
     * 获取玩家的回退地点
     * @param uuid
     * @return
     */
    public static Telepoter getBeforeTeleporter(String uuid) {
        Telepoter telepoter = new Telepoter();
        telepoter.setType(4);
        String path = "player." + uuid + ".before.tp";
        if (tpbSaveData.contains(path)) {
            telepoter.setName(tpbSaveData.getString(path + ".name",null));
            telepoter.setLocation((Location)tpbSaveData.get(path + ".location",null));
            return telepoter;
        }
        return null;
    }

    /**
     * 获取死亡地点
     * @param uuid
     * @param num
     * @return
     */
    public static Telepoter getDeadTeleporterByNum(String uuid, int num) {
        String path = "player." + uuid + ".dead.tp." + num;
        if (tpbSaveData.contains(path)) {
            Telepoter telepoter = new Telepoter(String.valueOf(num),
                    tpbSaveData.getString(path + ".name",null),
                    (Location)tpbSaveData.get(path + ".location",null),
                    1);
            return telepoter;
        }
        return null;
    }

    /**
     * 删除公共地点
     *
     * @param player
     */
    public static void delPublicTeleporter(Player player, int num) {
        if (player.hasPermission("op")) {
            String path = "public.tp." + num;
            if (tpbSaveData.contains(path)) {
                tpbSaveData.set(path, null);
                int pCount = tpbSaveData.getInt("public.count", 0);
                tpbSaveData.set("public.count", pCount-1);
                saveTpbSaveData();
            }
            player.sendMessage("公共地点已删除");
        }
    }

    /**
     * 删除私人地点
     *
     * @param player
     */
    public static void delPrivateTeleporter(Player player, int num) {
        String path = "player." + player.getUniqueId().toString();
        if (tpbSaveData.contains(path + ".tp." + num)) {
            tpbSaveData.set(path+ ".tp." + num, null);
            int pCount = tpbSaveData.getInt(path + ".count", 0);
            tpbSaveData.set(path + ".count", pCount-1);
            saveTpbSaveData();
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.BOLD +""+ChatColor.RED + "传送点已删除"));
    }

    /**
     * 获取可用的位置序号,已int最大值为范围，循环使用
     * @param path
     * @return
     */
    private static int getPNum(String path) {
        int pNum = tpbSaveData.getInt(path + ".num", 0);
        pNum = pNum == Integer.MAX_VALUE ? 0 : pNum;
        while (tpbSaveData.contains(path + ".tp." + pNum)) {
            // 正常使用不会有死循环
            pNum++;
        }
        return pNum;
    }
}

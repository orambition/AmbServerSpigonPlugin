package amb.server.plugin.service.radar;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.service.utils.GUIUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static amb.server.plugin.config.ConstantConfig.AP_RADAR_RECIPE;

public class RadarItem {
    /**
     *
     * @param material 搜索目标
     * @param better 能量
     * @return
     */
    public static ItemStack buildRadar(Material material, int better){
        ItemStack itemStack = new ItemStack(PluginConfig.radarItem);
        buildRadar(itemStack, material, better);
        return itemStack;
    }

    public static ItemStack buildRadar(ItemStack itemStack, Material material, int better){
        better = better<0 ? 0 : better;
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(PluginConfig.radarName);

        List<String> lore = new ArrayList<>();
        // 雷达类型
        lore.add(ChatColor.GREEN + "目标:" + ((material == null || material.isAir()) ? "空" : material.name()));
        lore.add(ChatColor.RESET + "能量:"+ GUIUtils.buildBatter(better, PluginConfig.raderBatteryMax));
        itemMeta.setLore(lore);
        // 能量
        itemMeta.addEnchant(Enchantment.DAMAGE_ALL, better,true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * 获取当前目标，为空说明不是radar
     * @param itemStack
     * @return
     */
    public static Material getRadarTarget(ItemStack itemStack){
        if (itemStack.getType().equals(PluginConfig.radarItem)
                && itemStack.getItemMeta().getDisplayName().equals(PluginConfig.radarName)){
            String targetName = itemStack.getItemMeta().getLore().get(0).substring(5);
            return (StringUtils.isBlank(targetName) || targetName.equals("空")) ? Material.AIR : Material.getMaterial(targetName);
        }
        return null;
    }
    /**
     * 添加合成表
     * @param plugin
     */
    public static void addRecipe(JavaPlugin plugin){
        ItemStack itemStack = buildRadar(Material.AIR, 6);

        NamespacedKey key = new NamespacedKey(plugin, AP_RADAR_RECIPE);
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.shape("EEE","ICR","EEE");
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('C', Material.COMPASS);
        recipe.setIngredient('R', Material.REDSTONE);
        Bukkit.addRecipe(recipe);

        NamespacedKey key2 = new NamespacedKey(plugin, AP_RADAR_RECIPE+"_2");
        ShapedRecipe recipe2 = new ShapedRecipe(key2, itemStack);
        recipe2.shape("EEE","RCI","EEE");
        recipe2.setIngredient('E', Material.EMERALD);
        recipe2.setIngredient('I', Material.IRON_INGOT);
        recipe2.setIngredient('C', Material.COMPASS);
        recipe2.setIngredient('R', Material.REDSTONE);
        Bukkit.addRecipe(recipe2);
    }
}

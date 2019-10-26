package amb.server.plugin.service.tpb;

import amb.server.plugin.config.PluginConfig;
import amb.server.plugin.service.utils.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static amb.server.plugin.config.ConstantConfig.TP_BOOK_RECIPE;

public class TpBookItem {

    public static ItemStack getItem(){
        return getItem(PluginConfig.tpBookPageMax);
    }
    public static ItemStack getItem(int pageCount){
        ItemStack item = new ItemStack(PluginConfig.tpBookItem,1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(PluginConfig.tpBookTitle);
        itemMeta.setLore(getBookItemLore(pageCount));
        itemMeta.addEnchant(Enchantment.DAMAGE_ALL,pageCount,true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(itemMeta);
        return item;
    }

    public static List<String> getBookItemLore(int pageCount){
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RESET + "页数:"+ GUIUtils.buildBatter(pageCount, PluginConfig.tpBookPageMax));
        lore.add(ChatColor.GOLD + "[潜行状态]点击可快速传送");
        return lore;
    }

    /**
     * 添加合成表
     * @param plugin
     */
    public static void addRecipe(JavaPlugin plugin){
        NamespacedKey key = new NamespacedKey(plugin, TP_BOOK_RECIPE);
        ShapelessRecipe recipe = new ShapelessRecipe(key, getItem());
        recipe.addIngredient(Material.BOOK);
        recipe.addIngredient(Material.ENDER_PEARL);
        Bukkit.addRecipe(recipe);
    }
}

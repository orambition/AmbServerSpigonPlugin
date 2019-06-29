package amb.server.plugin.service.tpb;

import amb.server.plugin.config.PluginConfig;
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

import java.util.Collections;

public class TpBookItem {

    public static ItemStack getItem(){
        return getItem(PluginConfig.tpBookPageMax);
    }
    public static ItemStack getItem(int pageCount){
        ItemStack item = new ItemStack(PluginConfig.tpBookItem,1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(PluginConfig.tpBookTitle);
        itemMeta.setLore(Collections.singletonList(ChatColor.RESET + "页数:"+pageCount));
        itemMeta.setLore(Collections.singletonList(ChatColor.GOLD + "点击可打开传送菜单"));
        itemMeta.setLore(Collections.singletonList(ChatColor.GOLD + "[潜行状态]点击可快速传送"));
        itemMeta.addEnchant(Enchantment.DAMAGE_ALL,pageCount,true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(itemMeta);
        return item;
    }

    public static void addRecipe(JavaPlugin plugin){
        NamespacedKey key = new NamespacedKey(plugin, "amb_plugin_tpbook");
        ShapelessRecipe recipe = new ShapelessRecipe(key, getItem());
        recipe.addIngredient(Material.BOOK);
        recipe.addIngredient(Material.ENDER_PEARL);
        Bukkit.addRecipe(recipe);
    }
}

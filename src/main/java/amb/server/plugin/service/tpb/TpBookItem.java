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
    private static ItemStack item;

    public static ItemStack getItem(){
        if (null == item){
            item = new ItemStack(PluginConfig.tpBookItem);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(PluginConfig.tpBookTitle);
            itemMeta.setLore(Collections.singletonList(ChatColor.RESET + "Ò³Êý:"+PluginConfig.tpBookPageMax));
            itemMeta.addEnchant(Enchantment.DAMAGE_ALL,PluginConfig.tpBookPageMax,true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(itemMeta);
        }
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

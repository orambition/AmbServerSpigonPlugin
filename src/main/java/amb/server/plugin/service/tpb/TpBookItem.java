package amb.server.plugin.service.tpb;

import amb.server.plugin.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class TpBookItem {
    private static ItemStack item;

    public static ItemStack getItem(){
        if (null == item){
            item = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta itemMeta = (BookMeta) item.getItemMeta();
            itemMeta.setTitle(PluginConfig.pluginConfig.getString("tpb.book.title"));
            itemMeta.setAuthor("Mr_Amb");
            itemMeta.setGeneration(BookMeta.Generation.TATTERED);
            itemMeta.setDisplayName(PluginConfig.pluginConfig.getString("tpb.book.title"));
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    public static void addRecipe(JavaPlugin plugin){
        NamespacedKey key = new NamespacedKey(plugin, "amb_tpbook");
        ShapelessRecipe recipe = new ShapelessRecipe(key, getItem());
        recipe.addIngredient(Material.BOOK);
        recipe.addIngredient(Material.ENDER_PEARL);
        Bukkit.addRecipe(recipe);
    }
}

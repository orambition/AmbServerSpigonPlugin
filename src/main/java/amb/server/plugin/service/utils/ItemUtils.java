package amb.server.plugin.service.utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhangrenjing
 * created on 2021/3/8
 */
public class ItemUtils {

    public static boolean hasDurability(ItemStack itemStack, int count) {
        if (itemStack == null) return false;
        // 物品耐久 与 损伤是相反的，损伤越大耐久越小
        Damageable damageable = (Damageable) itemStack.getItemMeta();
        return damageable != null && (itemStack.getType().getMaxDurability() - damageable.getDamage()) >= count;
    }

    public static void useDurability(ItemStack itemStack, int count) {
        if (itemStack == null) return;
        Damageable damageable = (Damageable) itemStack.getItemMeta();
        if (damageable == null) return;
        // 物品耐久 与 损伤是相反的，损伤越大耐久越小
        damageable.setDamage(damageable.getDamage() + count);
        itemStack.setItemMeta((ItemMeta) damageable);
    }

    public static ItemStack buildMenuItem(Material material, String name, String ... lore) {
        return buildMenuItem(material, name, Arrays.asList(lore));
    }

    public static ItemStack buildMenuItem(Material material, String name, List<String> loreList) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(loreList);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}

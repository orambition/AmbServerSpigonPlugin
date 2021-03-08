package amb.server.plugin.service.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author zhangrenjing
 * created on 2021/3/8
 */
public class ItemUtils {

    public static boolean hasDurability(ItemStack itemStack, int count) {
        if (itemStack == null) return false;
        // ��Ʒ�;� �� �������෴�ģ�����Խ���;�ԽС
        Damageable damageable = (Damageable) itemStack.getItemMeta();
        return damageable != null && (itemStack.getType().getMaxDurability() - damageable.getDamage()) >= count;
    }

    public static void useDurability(ItemStack itemStack, int count) {
        if (itemStack == null) return;
        Damageable damageable = (Damageable) itemStack.getItemMeta();
        if (damageable == null) return;
        // ��Ʒ�;� �� �������෴�ģ�����Խ���;�ԽС
        damageable.setDamage(damageable.getDamage() + count);
        itemStack.setItemMeta((ItemMeta) damageable);
    }
}

package amb.server.plugin.service.aip.task;

import amb.server.plugin.service.aip.entity.Friday;
import amb.server.plugin.tools.PlayerAnimation;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

public class DiggingTask extends AiTaskAbstract {
    private List<Location> locations;
    private int currentTick;
    private int startDigTick;

    public DiggingTask(Friday friday, List<Location> locations) {
        super(friday);
        this.locations = locations;
    }

    @Override
    public void init() {
        this.startDigTick = (int) (System.currentTimeMillis() / 50);
        this.status = TaskStatus.RUNNING;
    }

    @Override
    public boolean notOver() {
        return this.locations != null && !this.locations.isEmpty();
    }

    @Override
    public TaskStatus run() {
        if (locations == null || locations.isEmpty()){
            return status =TaskStatus.SUCCESS;
        }
        if (friday.dead) {
            return status = TaskStatus.FAILURE;
        }
        if (status.equals(TaskStatus.INITIAL)) {
            init();
        }
        Location location = locations.get(0);
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        currentTick = (int) (System.currentTimeMillis() / 50); // CraftBukkit
        if (playerFriday.getEyeLocation().distance(location) > 2) {
            if (friday.getNavigation().n()) {
                friday.getNavigation().a(location, 1D);
            }
            startDigTick = currentTick;
            return status = TaskStatus.RUNNING;
        }
        Block block = friday.world.getWorld().getBlockAt(location);
        IBlockData blockData = friday.world.getType(blockPosition);
        if (blockData == null || blockData.isAir()) {
            next();
            return status = TaskStatus.SUCCESS;
        } else {
            friday.setTargetLook(location);
            PlayerAnimation.ARM_SWING.play(playerFriday, 64);
            int tickDifference = currentTick - startDigTick;
            float damage = getStrength(blockData) * (tickDifference + 1);
            if (damage >= 1F) {
                block.breakNaturally();
                next();
                return status = TaskStatus.SUCCESS;
            }
            int modifiedDamage = (int) (damage * 10.0F);
            setBlockDamage(blockPosition, modifiedDamage);
        }
        return status = TaskStatus.RUNNING;
    }

    @Override
    public boolean pause() {
        status = TaskStatus.INITIAL;
        return true;
    }

    @Override
    public boolean cancel() {
        locations = null;
        status = TaskStatus.INITIAL;
        return true;
    }
    private void next(){
        locations.remove(0);
        startDigTick = (int) (System.currentTimeMillis() / 50);
    }
    private void setBlockDamage(BlockPosition blockPosition, int modifiedDamage) {
        friday.world.a(friday.getId(), blockPosition, modifiedDamage);
    }

    private float getStrength(IBlockData block) {
        float base = block.getBlock().strength;
        return base < 0.0F ? 0.0F : (!isDestroyable(block) ? 1.0F / base / 100.0F : strengthMod(block) / base / 30.0F);
    }

    private boolean isDestroyable(IBlockData block) {
        if (block.getMaterial().isAlwaysDestroyable()) {
            return true;
        } else {
            ItemStack current = friday.getEquipment(EnumItemSlot.MAINHAND);
            return current != null ? current.b(block) : false;
        }
    }
    private float strengthMod(IBlockData blockData) {
        ItemStack itemStack = friday.getEquipment(EnumItemSlot.MAINHAND);
        float f = itemStack.a(blockData);
        if (f > 1.0F) {
            int i = EnchantmentManager.getDigSpeedEnchantmentLevel(friday);
            if (i > 0) {
                f += i * i + 1;
            }
        }
        if (friday.hasEffect(MobEffects.FASTER_DIG)) {
            f *= (1.0F + (friday.getEffect(MobEffects.FASTER_DIG).getAmplifier() + 1) * 0.2F);
        }
        if (friday.hasEffect(MobEffects.SLOWER_DIG)) {
            float f1 = 1.0F;
            switch (friday.getEffect(MobEffects.SLOWER_DIG).getAmplifier()) {
                case 0:
                    f1 = 0.3F;
                    break;
                case 1:
                    f1 = 0.09F;
                    break;
                case 2:
                    f1 = 0.0027F;
                    break;
                case 3:
                default:
                    f1 = 8.1E-4F;
            }
            f *= f1;
        }
        /*if ((friday.a(Material.WATER)) && (!EnchantmentManager.i(friday))) {
            f /= 5.0F;
        }*/
        if (!friday.onGround) {
            f /= 5.0F;
        }
        System.out.println("f = "+ f);
        return f;
    }
}

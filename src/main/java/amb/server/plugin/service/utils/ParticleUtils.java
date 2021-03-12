package amb.server.plugin.service.utils;

import amb.server.plugin.core.PluginCore;
import amb.server.plugin.service.blueprint.BlueprintUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author zhangrenjing
 * created on 2021/3/7
 * �������ɹ���
 */
public class ParticleUtils {
    /**
     * ��λ��һ��λ�ö� ��������
     *
     * @param pos1
     * @param pos2
     */
    public static void drawLineTimer(Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null) {
            return;
        }
        BukkitTask task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(PluginCore.getInstance(),
                () -> drawLine(pos1, pos2), 0, 40);
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(PluginCore.getInstance(), () -> {
            if (!task.isCancelled()) task.cancel();
        }, 200);
    }

    public static void drawLine(Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null) {
            return;
        }
        // ��Ҫ���Ӷ����˶�ʱ����������Ϊ0��ƫ���������˶������ʸ��
        // pos1.getWorld().spawnParticle(Particle.CRIT, pos1x, pos1y, pos1z, 0, 0, pos2y - pos1y, 0, 1.0);
        int[] xyzRange = BlueprintUtil.getRange(pos1, pos2);

        for (double x = xyzRange[0]; x <= xyzRange[1] + 1; x += 0.25) {
            pos1.getWorld().spawnParticle(Particle.COMPOSTER, x, xyzRange[2], xyzRange[4], 1);
            pos1.getWorld().spawnParticle(Particle.COMPOSTER, x, xyzRange[3] + 1, xyzRange[4], 1);
            pos1.getWorld().spawnParticle(Particle.COMPOSTER, x, xyzRange[2], xyzRange[5] + 1, 1);
            pos1.getWorld().spawnParticle(Particle.COMPOSTER, x, xyzRange[3] + 1, xyzRange[5] + 1, 1);
        }
        for (double y = xyzRange[2]; y <= xyzRange[3] + 1; y += 0.3) {
            pos1.getWorld().spawnParticle(Particle.COMPOSTER, xyzRange[0], y, xyzRange[4], 1);
            pos1.getWorld().spawnParticle(Particle.COMPOSTER, xyzRange[1] + 1, y, xyzRange[4], 1);
            pos1.getWorld().spawnParticle(Particle.COMPOSTER, xyzRange[0], y, xyzRange[5] + 1, 1);
            pos1.getWorld().spawnParticle(Particle.COMPOSTER, xyzRange[1] + 1, y, xyzRange[5] + 1, 1);
        }
        for (double z = xyzRange[4]; z <= xyzRange[5] + 1; z += 0.3) {
            pos1.getWorld().spawnParticle(Particle.COMPOSTER, xyzRange[0], xyzRange[2], z, 1);
            pos1.getWorld().spawnParticle(Particle.COMPOSTER, xyzRange[1] + 1, xyzRange[2], z, 1);
            pos1.getWorld().spawnParticle(Particle.COMPOSTER, xyzRange[0], xyzRange[3] + 1, z, 1);
            pos1.getWorld().spawnParticle(Particle.COMPOSTER, xyzRange[1] + 1, xyzRange[3] + 1, z, 1);
        }
    }
}

package amb.server.plugin.service.aip.navigation;

import amb.server.plugin.service.aip.entity.Friday;
import amb.server.plugin.service.aip.task.FollowTask;
import amb.server.plugin.service.aip.task.FoundTask;
import amb.server.plugin.service.aip.task.GuardsTask;
import amb.server.plugin.service.aip.task.SleepTask;
import amb.server.plugin.tools.NMSUtil;
import amb.server.plugin.tools.PlayerAnimation;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class AutoPlayNavigator extends NavigationAbstract {
    private static final Logger logger = PluginLogger.getLogger(AutoPlayNavigator.class.getName());

    private final boolean haveBow;
    private final double attickRange = 3D; // ��ս����
    private boolean attickAfter = false;

    // ��Χ��ʵ��
    private List<Entity> nearbyPlayerEntity = new ArrayList<>();
    // ��Χ�й����Ե�ʵ��
    private List<Entity> nearbyHostileEntity = new ArrayList<>();

    public AutoPlayNavigator(Friday friday) {
        super(friday);
        if (this.playerFriday.getInventory().getItemInMainHand().getType().equals(Material.BOW)
                || this.playerFriday.getInventory().getItemInOffHand().getType().equals(Material.BOW)) {
            haveBow = true;
        } else {
            haveBow = false;
        }
    }

    /**
     * 1 �����Ϊ�ݵ�����͵�������
     * 2 �������񣺼���ǰ����û�����Ҳ����������ͬ����
     * 3 �ݵ����񣬼�ͬһ���͵�����ֻ�ܴ���һ��
     * 4 ����������������������ǵ�������Ϊ�ݵ�����
     * **/
    @Override
    protected void createTask() {
        switch (friday.getPlayMode()){
            case FREE:
                if (tasks.isEmpty()){
                    logger.info("add FoundTask");
                    tasks.add(new FoundTask(friday, Material.DIAMOND_ORE, 10));
                }
                break;
            case GUARDS:
                if (friday.getGuardsLocation() == null){
                    friday.setPlayMode(Friday.PlayMode.FREE);
                } else {
                    if (tasks.stream().noneMatch(e->e instanceof GuardsTask)){
                        logger.info("add GuardsTask");
                        tasks.add(new GuardsTask(friday));
                    }
                }
                break;
            case FOLLOW:
                if (friday.getFollowTarget() == null || !friday.getFollowTarget().isValid()){
                    friday.setPlayMode(Friday.PlayMode.FREE);
                } else {
                    if (tasks.stream().noneMatch(e->e instanceof FollowTask)){
                        logger.info("add FollowTask");
                        tasks.add(new FollowTask(friday));
                    }
                }
                break;
        }
        if (playerFriday.getWorld().getTime() > 12300){
            if (nearbyHostileEntity.isEmpty() && !playerFriday.isSleeping()){
                if (!friday.getPlayMode().equals(Friday.PlayMode.FOLLOW)
                        || friday.getFollowTarget().isSleeping()){
                    if (!(nowTask instanceof SleepTask)){
                        logger.info("add SleepTask");
                        tasks.add(new SleepTask(friday));
                    }
                }
            }
        }
        //logger.info("Task="+ tasks.toString());
    }

    /**
     * ����״̬����
     */
    @Override
    protected void passive() {
        if (playerFriday.getHealth() < 20){
            playerFriday.setHealth(20);
        }
        if (!playerFriday.isSleeping()){
            // ������� ȷ�ϰ�ȫ
            Entity target = foundTarget(attickRange);
            if (target != null) {
                // ������������
                NMSUtil.attickEntity(playerFriday, target, haveBow, attickRange);
                attickAfter = true;
            } else {
                if (attickAfter){
                    attickAfter = false;
                    PlayerAnimation.STOP_USE_ITEM.play(playerFriday, 64);
                }
                if (!nearbyPlayerEntity.isEmpty()) {
                    NMSUtil.look(playerFriday, nearbyPlayerEntity.get(0));
                }
            }
        }
    }

    private Entity foundTarget(double radius){
        foundNearbyTarget(radius);
        if (!nearbyHostileEntity.isEmpty()) {
            return nearbyHostileEntity.get(0);
        }
        if (friday.getDamageTarget() != null){
            return friday.setDamageTarget(null);
        }
        return null;
    }
    /**
     * ������Χʵ��
     * @param radius
     * @return
     */
    private void foundNearbyTarget(double radius) {
        nearbyHostileEntity.clear();
        nearbyPlayerEntity.clear();
        // ���˹���ʵ��
        Predicate<Entity> attackEntity = e -> e.hasMetadata(Friday.NPC_FLAG) ? false : playerFriday.hasLineOfSight(e);
        Collection<Entity> nearbyEntity = playerFriday.getWorld().getNearbyEntities(playerFriday.getLocation(), radius, radius, radius, attackEntity);
        nearbyEntity.stream().sorted(Comparator.comparing(e -> playerFriday.getLocation().distance(e.getLocation()))).forEach(e -> {
            if ((e instanceof Monster && e.getType() != EntityType.PIG_ZOMBIE)
                    || e.getType() == EntityType.SLIME || e.getType() == EntityType.MAGMA_CUBE
                    || e.getType() == EntityType.PHANTOM) {
                nearbyHostileEntity.add(e);
            } else if (e instanceof Player) {
                nearbyPlayerEntity.add(e);
            }
        });
    }

}

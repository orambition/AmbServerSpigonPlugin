package amb.server.plugin.service.aip.task;

import amb.server.plugin.service.aip.entity.Friday;
import amb.server.plugin.tools.NMSUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.List;

public class FollowTask extends AiTaskAbstract{
    private final Entity entity;
    private Location location;

    public FollowTask(Friday friday) {
        super(friday);
        this.entity = friday.getFollowTarget();
    }

    @Override
    public void init() {

    }

    @Override
    public boolean notOver() {
        return friday.getPlayMode().equals(Friday.PlayMode.FOLLOW) && friday.getFollowTarget() != null;
    }

    @Override
    public TaskStatus run() {
        if (!playerFriday.getWorld().equals(entity.getWorld())){
            playerFriday.teleport(entity.getLocation());
        }
        if (playerFriday.getLocation().distanceSquared(entity.getLocation())>9){
            location = entity.getLocation();
        } else {
            List<Entity> nearbyHostile = NMSUtil.foundNearbyHostile(entity, 2D);
            if (nearbyHostile != null && !nearbyHostile.isEmpty()){
                location = nearbyHostile.get(0).getLocation();
            }
        }
        if (location != null && friday.getNavigation().n()) {
            friday.getNavigation().setNavigationTarget(location,1.5, true);
            location = null;
        }
        return TaskStatus.RUNNING;
    }

    @Override
    public boolean pause() {
        // 取消之前 的 导航
        friday.getNavigation().o();
        status = TaskStatus.PAUSE;
        return true;
    }

    @Override
    public boolean cancel() {
        // 取消之前 的 导航
        friday.getNavigation().o();
        return true;
    }
}

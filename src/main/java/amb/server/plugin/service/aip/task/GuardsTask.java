package amb.server.plugin.service.aip.task;

import amb.server.plugin.service.aip.entity.Friday;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuardsTask extends AiTaskAbstract{

    public GuardsTask(Friday friday) {
        super(friday);
    }

    @Override
    public void init() {

    }

    @Override
    public boolean notOver() {
        return friday.getPlayMode().equals(Friday.PlayMode.GUARDS);
    }

    @Override
    public TaskStatus run() {
        if (!playerFriday.getWorld().equals(friday.getGuardsLocation().getWorld())){
            playerFriday.teleport(friday.getGuardsLocation());
        }
        if (friday.getGuardsLocation().distanceSquared(playerFriday.getLocation())>9){
            friday.getNavigation().setNavigationTarget(friday.getGuardsLocation(), 1, true);
        }
        return status = TaskStatus.RUNNING;
    }

    @Override
    public boolean pause() {
        // 取消之前 的 导航
        friday.getNavigation().o();
        status = TaskStatus.PAUSE;
        return false;
    }

    @Override
    public boolean cancel() {
        // 取消之前 的 导航
        friday.getNavigation().o();
        return true;
    }
}

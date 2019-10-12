package amb.server.plugin.service.aip.navigation;

import amb.server.plugin.service.aip.entity.Friday;
import amb.server.plugin.service.aip.task.AiTaskAbstract;
import amb.server.plugin.service.aip.task.SleepTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class NavigationAbstract extends BukkitRunnable {
    private static final Logger logger = PluginLogger.getLogger(NavigationAbstract.class.getName());

    protected final Friday friday;
    protected final Player playerFriday;

    protected final List<AiTaskAbstract> tasks;
    protected AiTaskAbstract nowTask;

    public NavigationAbstract(Friday friday) {
        this.friday = friday;
        this.playerFriday = friday.getBukkitEntity();
        this.tasks = new ArrayList<>();
    }

    @Override
    public void run() {
        // ÿ 10 tickִ��һ��
        if (friday == null || playerFriday.isDead()) {
            this.cancel();
            logger.info("[Friday] AutoPlay is cancel!");
            return;
        }

        // ����Ai����
        createTask();

        // ִ������ ͬʱֻ��ִ��һ�����񣬲��ܲ���ִ��
        if (!tasks.isEmpty()) {
            AiTaskAbstract task = tasks.get(tasks.size() - 1);
            if (!task.equals(nowTask)) {
                if (nowTask != null) {
                    nowTask.pause();
                }
                nowTask = task;
            }
            nowTask.run();
            if (!nowTask.notOver()) {
                nowTask.cancel();
                tasks.remove(nowTask);
            }
        }

        // ������
        passive();
    }

    /**
     * �������񴴽�
     */
    protected abstract void createTask();

    /**
     * ����״̬����
     */
    protected abstract void passive();

}

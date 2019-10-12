package amb.server.plugin.service.aip.task;

import amb.server.plugin.service.aip.entity.Friday;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class AiTaskAbstract {
    protected Friday friday;
    protected Player playerFriday;
    protected TaskStatus status;

    public AiTaskAbstract(Friday friday) {
        this.friday = friday;
        this.playerFriday = friday.getBukkitEntity();
        this.status = TaskStatus.INITIAL;
    }

    public abstract void init();
    public abstract boolean notOver();
    public abstract TaskStatus run();
    public abstract boolean pause();
    public abstract boolean cancel();

    enum TaskStatus {
        INITIAL,
        RUNNING,
        PAUSE,
        SUCCESS,
        FAILURE,
    }
}

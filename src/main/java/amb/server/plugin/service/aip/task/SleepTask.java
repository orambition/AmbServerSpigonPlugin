package amb.server.plugin.service.aip.task;

import amb.server.plugin.service.aip.entity.Friday;
import amb.server.plugin.tools.NMSUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;

public class SleepTask extends AiTaskAbstract{

    private Location bedLocation;
    private BlockFace bedBlockFace;

    public SleepTask(Friday friday) {
        super(friday);
        bedLocation = playerFriday.getLocation().clone();
        bedBlockFace = playerFriday.getFacing();
    }

    @Override
    public void init() {
        if (bedLocation.getBlock().equals(Material.BLUE_BED)){
            return;
        }
        bedLocation.getBlock().setType(Material.BLUE_BED);
        BlockState blockState = bedLocation.getBlock().getState();
        blockState.setType(Material.BLUE_BED);
        Bed bed = (Bed)blockState.getBlockData().clone();
        bed.setPart(Bed.Part.HEAD);
        bed.setFacing(bedBlockFace);
        blockState.setBlockData(bed);
        blockState.update();

        /*两格的床有破坏地形的风险
        bedLocation.add(bedBlockFace.getDirection());

        blockState = bedLocation.getBlock().getState();
        blockState.setType(Material.BLUE_BED);

        bed = (Bed)blockState.getBlockData().clone();
        bed.setPart(Bed.Part.FOOT);
        bed.setFacing(bedBlockFace);
        blockState.setBlockData(bed);*/
    }

    @Override
    public boolean notOver() {
        long time = playerFriday.getWorld().getTime();
        return time < 0 || time > 12300;
    }

    @Override
    public TaskStatus run() {
        if (!playerFriday.isSleeping() && friday.getDamageTarget() == null){
            playerFriday.teleport(bedLocation);
            init();
            playerFriday.sleep(playerFriday.getLocation(), true);
        }

        return status = TaskStatus.RUNNING;
    }

    @Override
    public boolean pause() {
        if (playerFriday.isSleeping()){
            playerFriday.wakeup(false);
        }
        diggingBed();
        status = TaskStatus.PAUSE;
        return true;
    }

    @Override
    public boolean cancel() {
        if (playerFriday.isSleeping()) {
            playerFriday.wakeup(false);
        }
        diggingBed();
        return true;
    }
    private void diggingBed(){
        bedLocation.getBlock().setType(Material.AIR);
    }
}

package amb.server.plugin.service.aip.task;

import amb.server.plugin.service.aip.entity.Friday;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.List;

public class FoundTask extends AiTaskAbstract {
    private final World world;
    private List<Location> locationList = new ArrayList<>();
    private int needFoundNum;
    private final Material needFoundMaterial;
    private Location orgLocation;
    private Location oldLocation;
    private ChunkSnapshot chunkSnapshot;

    public FounopdTask(Friday friday, Material material, int needFoundNum) {
        super(friday);
        world = playerFriday.getWorld();
        needFoundMaterial = material;
        this.needFoundNum = needFoundNum;
    }

    @Override
    public void init() {
        if (!world.getName().equals("world")){
            needFoundNum = 0;
            return;
        }
        orgLocation = playerFriday.getLocation().clone();
        // 取消之前 的 导航
        friday.getNavigation().o();
    }

    @Override
    public boolean notOver() {
        return !(needFoundNum == 0 && locationList.isEmpty() && friday.getNavigation().n());
    }

    @Override
    public TaskStatus run() {
        if (status.equals(TaskStatus.INITIAL)) {
            init();
        }

        if (friday.getNavigation().n()){
            if (needFoundNum > 0) {
                // 目标没完成需要扫描
                ChunkSnapshot chunkTemp = world.getChunkAt(playerFriday.getLocation()).getChunkSnapshot();
                if (chunkSnapshot == null
                        || !chunkTemp.getWorldName().equals(chunkSnapshot.getWorldName())
                        || chunkTemp.getX() != chunkSnapshot.getX()
                        || chunkTemp.getZ() != chunkSnapshot.getZ()){
                    // 到达新区块，开始扫描
                    chunkSnapshot = chunkTemp;
                    for (int i = 0; i < 16; i++) {
                        for (int j = 0; j < 16; j++) {
                            for (int k = 0; k < chunkSnapshot.getHighestBlockYAt(i,j); k++) {
                                Material blockData = chunkSnapshot.getBlockType(i, k, j);
                                if (blockData.equals(needFoundMaterial)) {
                                    Location temp = new Location(playerFriday.getWorld(),chunkSnapshot.getX()*16+i,k,chunkSnapshot.getZ()*16+j);
                                    locationList.add(temp);
                                    needFoundNum--;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (oldLocation != null){
                oldLocation.getBlock().setType(Material.TORCH);
                oldLocation = null;
            }
            if (!locationList.isEmpty()) {
                System.out.println(locationList.toString());
                oldLocation = world.getHighestBlockAt(locationList.get(0)).getLocation();
                locationList.remove(0);
                friday.getNavigation().setNavigationTarget(oldLocation, 1D, false);
            } else if (needFoundNum > 0){
                // 扫描结果为空并且目标没有完成，需要前往新的区块
                Location newLoc = playerFriday.getLocation().clone();
                newLoc.add(16,0,0);
                newLoc = world.getHighestBlockAt(newLoc).getLocation();
                friday.getNavigation().setNavigationTarget(newLoc, 1D, false);
            } else {
                // 目标完成
                friday.getNavigation().setNavigationTarget(orgLocation, 2D,true);
            }
        }

        return status = TaskStatus.RUNNING;
    }

    @Override
    public boolean pause() {
        // 取消之前 的 导航
        friday.getNavigation().o();
        friday.getNavigation().setNavigationTarget(orgLocation, 2D, true);
        status = TaskStatus.PAUSE;
        return true;
    }

    @Override
    public boolean cancel() {
        locationList.clear();
        // 取消之前 的 导航
        friday.getNavigation().o();
        return true;
    }

}

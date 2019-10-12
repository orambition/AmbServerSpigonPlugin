package amb.server.plugin.service.aip.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_14_R1.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 寻路器
 * 用于生成路径点，并创建路径实体
 */
public class FridayPathfinder extends Pathfinder {
    private final Path a = new Path();
    private final Set<PathPoint> b = Sets.newHashSet();
    private final PathPoint[] c = new PathPoint[32];
    private final int d;
    private FridayPathfinderNormal e;

    public FridayPathfinder(FridayPathfinderNormal var0, int var1) {
        super(var0, var1);
        this.d = var1;
        this.e = var0;
    }

    /**
     * 在指定区块内 获取目的地的路径实体
     * @param entityCC 区块
     * @param friday 实体
     * @param targetSet 目的地位置集合
     * @param defaultFoundRange 实体可见范围
     * @param targetArriveDistance 定义距离多少算到达
     * @return
     */
    public PathEntity a(IWorldReader entityCC, Friday friday, Set<BlockPosition> targetSet, float defaultFoundRange, int targetArriveDistance){
        // 初始化路径
        this.a.a();
        // 初始化探路者指针
        this.e.a(entityCC, friday);
        // 通过探路者指针 获取实体Y轴 目的地 的 路径点
        PathPoint pathPointY = this.e.b();
        // 通过探路者指针 将目的地位置 转换为 “路径目的地”对象和位置的MAP
        Map var6 = targetSet.stream().collect(Collectors.toMap(
                (var0x) -> this.e.a((double) var0x.getX(), (double) var0x.getY(), (double) var0x.getZ()),
                Function.identity()));
        // 获取路径对象 Y轴目的地、路径目的地、实体可见范围、距离多少算到达
        PathEntity var7 = this.a(pathPointY, var6, defaultFoundRange, targetArriveDistance);
        // 释放探路器指针
        this.e.a();
        return var7;
    }

    /**
     * 重要！！！返回目的地的路径实体
     * 已Y轴 目的地 为中心，向外搜索可视距离的范围，检查目的地是否可达
     * @param var0 Y轴 目的地 搜索的中心 也是第一个起点
     * @param var1 路径目的地
     * @param var2 可见范围
     * @param var3 定义 距离目的地多远 可被视为到达
     * @return
     */
    private PathEntity a(PathPoint var0, Map<PathDestination, BlockPosition> var1, float var2, int var3) {
        // 路径目的地集合
        Set<PathDestination> var4 = var1.keySet();
        var0.e = 0.0F;
        // 计算离Y轴目的地最近的 路径目的地 距离
        // 并将 Y轴目的地 作为 要到达 路径目的地 的第一个必经之路
        var0.f = this.a(var0, var4);
        var0.g = var0.f;
        // 路径初始化，将路径点数组数量置为0
        this.a.a();
        this.b.clear();
        // 将Y轴目的地 加入路径对象的路径点数组中
        this.a.a(var0);
        int var5 = 0;
        // 路径对象的b !=0
        while(!this.a.e()) {
            ++var5;
            // d 在初始化时设置为了768，意味着最多尝试768次
            if (var5 >= this.d) {
                break;
            }

            // 1 返回路径对象 中路径点数组的第一个，并向前移动数组
            // 路径对象 b--
            PathPoint var6 = this.a.c();
            var6.i = true;

            // 2 第一个路径点到 路径目的地 的距离 < 1 则标记为可达？
            var4.stream().filter((var2x) -> {
                return var6.c(var2x) <= (float)var3;
            }).forEach(PathDestination::e);
            // 如果有任意一个 路径目的地 可达，则返回
            if (var4.stream().anyMatch(PathDestination::f)) {
                break;
            }

            // 3 第一个路径点到 Y轴目的地 的距离< 可见距离
            // 从Y轴目的地向外搜索，直到不可见边界
            if (var6.a(var0) < var2) {
                // 将第一个路径点 周围的有效路径点 添加到 this.C,并返回有效路径点个数
                int var7 = this.e.a(this.c, var6);

                // 遍历 周围有效路径点，处理这些路径点的必要数据？
                for(int var8 = 0; var8 < var7; ++var8) {
                    PathPoint var9 = this.c[var8];
                    // 第一个路径点到 周围路径点的距离
                    float var10 = var6.a(var9);
                    var9.j = var6.j + var10;
                    float var11 = var6.e + var10 + var9.k;
                    if (var9.j < var2 && (!var9.c() || var11 < var9.e)) {
                        // 重要！！！ 设置 周围路径点的 上一个路径 为当前路径点
                        var9.h = var6;
                        var9.e = var11;
                        // 重要！！！ 更新 到达 路径目的地 的必经之路 为周围路径点！
                        var9.f = this.a(var9, var4) * 1.5F;
                        if (var9.c()) {
                            this.a.a(var9, var9.e + var9.f);
                        } else {
                            var9.g = var9.e + var9.f;
                            // 4 将周围路径点 添加到 路径数组
                            this.a.a(var9);
                        }
                    }
                }
            }
        }

        Stream var6;
        // 搜索之后，如果有任意一个 路径目的地 可达，
        if (var4.stream().anyMatch(PathDestination::f)) {
            var6 = var4.stream().filter(PathDestination::f).map((var1x) -> {
                // 通过 目的地路径 和目标位置，创建路径实体
                return this.a(var1x.d(), (BlockPosition)var1.get(var1x), true);
            }).sorted(Comparator.comparingInt(PathEntity::e));
        } else {
            var6 = var4.stream().map((var1x) -> {
                // 没有可达路径，也返回路径实体，但标记不可达
                return this.a(var1x.d(), (BlockPosition)var1.get(var1x), false);
            }).sorted(Comparator.comparingDouble(PathEntity::l).thenComparingInt(PathEntity::e));
        }
        // 返回 路径点列表最短的一个
        Optional<PathEntity> var7 = var6.findFirst();
        if (!var7.isPresent()) {
            return null;
        } else {
            PathEntity var8 = (PathEntity)var7.get();
            return var8;
        }
    }

    /**
     * 重要！！！！
     * 将 路径点 设置为 到底 路径目的地 的必经路径点
     * 返回 路径点 最近的 路径目的地 距离
     * @param var0 路径点
     * @param var1 路径目的地
     * @return
     */
    private float a(PathPoint var0, Set var1) {
        float var2 = Float.MAX_VALUE;
        float var5;
        // 获取距离Y轴目的地最近的 路径目的地
        for (Iterator var4 = var1.iterator(); var4.hasNext(); var2 = Math.min(var5, var2)) {
            PathDestination var3 = (PathDestination) var4.next();
            // Y轴目的地 到 路径目的地 的 欧式距离
            var5 = var0.a(var3);
            // 更新 达到 路径目的地 必经路径点为 var0
            // 重要
            var3.a(var5, var0);
        }
        return var2;
    }

    /**
     * 创建路径实体对象
     * @param var0 路径点
     * @param var1 路径点的块位置
     * @param var2
     * @return
     */
    private PathEntity a(PathPoint var0, BlockPosition var1, boolean var2) {
        List<PathPoint> var3 = Lists.newArrayList();
        PathPoint var4 = var0;
        var3.add(0, var0);
        // 遍历下一个 必经之路 路径点
        while(var4.h != null) {
            var4 = var4.h;
            // 必经之路 路径点 是从离 目的地 最近的点开始的，所以此处要向前添加，转为离实体最近
            var3.add(0, var4);
        }
        // 创建路径实体，包含一个路劲点列表和目标位置
        return new PathEntity(var3, var1, var2);
    }
}

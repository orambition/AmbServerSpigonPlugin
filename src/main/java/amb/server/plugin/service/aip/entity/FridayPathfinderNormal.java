package amb.server.plugin.service.aip.entity;

import com.google.common.collect.Sets;
import net.minecraft.server.v1_14_R1.*;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 寻路器 指针
 */
public class FridayPathfinderNormal extends PathfinderAbstract {
    protected float j;
    protected Friday b;

    /**
     * 初始化方法
     * @param var0
     * @param var1
     */
    public void a(IWorldReader var0, Friday var1) {
        this.a = var0;
        this.b = var1;
        this.c.clear();
        this.d = MathHelper.d(var1.getWidth() + 1.0F);
        this.e = MathHelper.d(var1.getHeight() + 1.0F);
        this.f = MathHelper.d(var1.getWidth() + 1.0F);
    }

    @Override
    public void a() {
        this.b.a(PathType.WATER, this.j);
        super.a();
    }

    /**
     * 获取实体在Y轴的目的地, 即水面或地面
     * @return
     */
    @Override
    public PathPoint b() {
        // 实体要到的Y值
        int var0 = 0;
        BlockPosition var1;
        if (this.e() && this.b.isInWater()) {
            /*// 获取实体绑定盒最低点
            var0 = MathHelper.floor(this.b.getBoundingBox().minY);
            // 获取实体脚底方块位置
            var1 = new BlockPosition.MutableBlockPosition(this.b.locX, var0, this.b.locZ);
            // 从脚底方块开始向上遍历，直到不是水
            for (IBlockData var2 = this.a.getType(var1);
                 var2.getBlock() == Blocks.WATER || var2.p() == FluidTypes.WATER.a(false);
                 var2 = this.a.getType(var1)) {
                ++var0;
                ((BlockPosition.MutableBlockPosition) var1).c(this.b.locX, var0, this.b.locZ);
                //var1 = var1.up();
            }*/
            for (var1 = new BlockPosition(this.b);
                 this.a.getType(var1).getBlock() == Blocks.WATER || this.a.getType(var1).p() == FluidTypes.WATER.a(false);
                 var1 = var1.up())
            //--var0;
            var0 = var1.down().getY();
        } else if (this.b.onGround) {
            // 在陆地方块上
            var0 = MathHelper.floor(this.b.getBoundingBox().minY + 0.5D);
        } else {
            // 从玩家位置开始向下遍历，直到不是空气
            for (var1 = new BlockPosition(this.b);
                 (this.a.getType(var1).isAir() || this.a.getType(var1).a(this.a, var1, PathMode.LAND)) && var1.getY() > 0;
                 var1 = var1.down()) {
            }
            var0 = var1.up().getY();
        }

        var1 = new BlockPosition(this.b);
        // 获取指定位置的路径类型
        PathType pathType = this.a(this.a, var1.getX(), var0, var1.getZ(), this.b);
        // 实体对此类型的权重<0,不可达
        if (this.b.a(pathType) < 0.0F) {
            // 获取目的地周围其他4个方块的位置
            Set var3 = Sets.newHashSet();
            var3.add(new BlockPosition(this.b.getBoundingBox().minX, var0, this.b.getBoundingBox().minZ));
            var3.add(new BlockPosition(this.b.getBoundingBox().minX, var0, this.b.getBoundingBox().maxZ));
            var3.add(new BlockPosition(this.b.getBoundingBox().maxX, var0, this.b.getBoundingBox().minZ));
            var3.add(new BlockPosition(this.b.getBoundingBox().maxX, var0, this.b.getBoundingBox().maxZ));
            Iterator var5 = var3.iterator();
            // 遍历周围方块
            while (var5.hasNext()) {
                BlockPosition var4 = (BlockPosition) var5.next();
                // 获取周围方块的路径类型
                PathType var6 = this.a(this.a, var4.getX(), var4.getY(), var4.getZ(), this.b);
                // 若实体对这些方块的权重大于0，则返回相应的路径点
                if (this.b.a(var6) >= 0.0F) {
                    return this.a(var4.getX(), var4.getY(), var4.getZ());
                }
            }
        }
        // 根据位置信息生成路径点(此方法带缓存，重复地点不会重新创建)
        return this.a(var1.getX(), var0, var1.getZ());
    }

    /**
     * 获取指定区块、指定位置、指定实体的路径类型
     * 简单的封装
     */
    private PathType a(IBlockAccess var0, int var1, int var2, int var3, Friday var4) {
        // (区块,目的地X,目的地Y,目的地Z,实体,实体宽,实体高,实体宽,可破坏门,可通过门)
        return this.a(var0, var1, var2, var3, var4, this.d, this.e, this.f, this.d(), this.c());
    }

    @Override
    public PathType a(IBlockAccess iBlockAccess, int i, int i1, int i2, EntityInsentient entityInsentient, int i3, int i4, int i5, boolean b, boolean b1) { return null; }
    /**
     * 获取区块中指定(var1,var2,var3)位置的路径类型
     *  (覆写上面的方法，因为需要使用friday实体)
     * 1.根据目的地块类型获取最基本的路径类型
     * 2.目的地在空中时，路径类型已目的地下一格方块为准
     * 3.目的地周围有仙人掌、火、浆果时，路径为伤害类型
     * 4.目的地为门、铁轨、树叶时，根据实体属性设置是否可到达
     * 5.以相同规则获取目的地周围3*3的路径类型
     * 6.目的地周围有栅栏，则返回栅栏类型
     * 7.最后根据实体对不同路径类型的权重进行返回
     */
    public PathType a(IBlockAccess var0, int var1, int var2, int var3, Friday var4, int var5, int var6, int var7, boolean var8, boolean var9) {
        // 目的地周围3*3路径类型
        EnumSet var10 = EnumSet.noneOf(PathType.class);
        // 目的地路径类型
        PathType var11 = PathType.BLOCKED;
        //double var12 = var4.getWidth() / 2.0D;
        // 实体的块位置
        BlockPosition var14 = new BlockPosition(var4);
        // 获取路径类型，(区块,目的地X,目的地Y,目的地Z,实体宽,实体高,实体宽,,,路径类型枚举,默认类型,实体位置)
        var11 = this.a(var0, var1, var2, var3, var5, var6, var7, var8, var9, var10, var11, var14);

        if (var10.contains(PathType.FENCE)) {
            // 周围有栅栏
            return PathType.FENCE;
        } else {
            // 根据实体对不同路径类型的权重 返回路径类型
            PathType var15 = PathType.BLOCKED;
            Iterator var17 = var10.iterator();

            while (var17.hasNext()) {
                PathType var16 = (PathType) var17.next();
                if (var4.a(var16) < 0.0F) {
                    return var16;
                }
                if (var4.a(var16) >= var4.a(var15)) {
                    var15 = var16;
                }
            }

            if (var11 == PathType.OPEN && var4.a(var15) == 0.0F) {
                return PathType.OPEN;
            } else {
                return var15;
            }
        }
    }

    /**
     * 获取目的地及周围3*3区域的路径类型
     * @param var0 搜索的区块
     * @param var1 目的地X
     * @param var2 目的地Y
     * @param var3 目的地Z
     * @param var4 实体宽
     * @param var5 实体高
     * @param var6 实体宽
     * @param var7 可破坏门
     * @param var8 可通过门
     * @param var9 目的地周围3*3的路径类型集
     * @param var10 目的地的路径类型
     * @param var11 实体位置
     * @return
     */
    public PathType a(IBlockAccess var0, int var1, int var2, int var3, int var4, int var5, int var6, boolean var7,
                      boolean var8, EnumSet var9, PathType var10, BlockPosition var11) {
        // 实体宽+1
        for (int var12 = 0; var12 < var4; ++var12) {
            // 实体高+1
            for (int var13 = 0; var13 < var5; ++var13) {
                // 实体宽+1
                for (int var14 = 0; var14 < var6; ++var14) {
                    // + 实体的目的地
                    int var15 = var12 + var1;
                    int var16 = var13 + var2;
                    int var17 = var14 + var3;
                    // 获取目的的路径类型
                    PathType var18 = this.a(var0, var15, var16, var17);
                    // 处理路径类型有门、铁轨、树叶的情况(区块,可破坏门,可通过门,实体位置，目的路径类型)
                    var18 = this.a(var0, var7, var8, var11, var18);
                    if (var12 == 0 && var13 == 0 && var14 == 0) {
                        // 设置目的地的路径类型
                        var10 = var18;
                    }
                    // 目的地周围3*3的路径类型
                    var9.add(var18);
                }
            }
        }
        return var10;
    }

    /**
     * 获取目的地的路径类型
     * 附加 空中、被包围等情况处理
     * @param var0
     * @param var1
     * @param var2
     * @param var3
     * @return
     */
    @Override
    public PathType a(IBlockAccess var0, int var1, int var2, int var3) {
        // 获取区块指定位置的路径类型
        PathType var4 = this.b(var0, var1, var2, var3);
        if (var4 == PathType.OPEN && var2 >= 1) {
            // 如果路径时空气，则以下一块的路径类型位置
            Block var5 = var0.getType(new BlockPosition(var1, var2 - 1, var3)).getBlock();
            PathType var6 = this.b(var0, var1, var2 - 1, var3);
            var4 = var6 != PathType.WALKABLE && var6 != PathType.OPEN && var6 != PathType.WATER && var6 != PathType.LAVA
                    ? PathType.WALKABLE
                    : PathType.OPEN;
            if (var6 == PathType.DAMAGE_FIRE || var5 == Blocks.MAGMA_BLOCK || var5 == Blocks.CAMPFIRE) {
                var4 = PathType.DAMAGE_FIRE;
            }

            if (var6 == PathType.DAMAGE_CACTUS) {
                var4 = PathType.DAMAGE_CACTUS;
            }

            if (var6 == PathType.DAMAGE_OTHER) {
                var4 = PathType.DAMAGE_OTHER;
            }
        }
        // 被仙人掌、浆果等包围的路径同为伤害路径
        var4 = this.a(var0, var1, var2, var3, var4);
        return var4;
    }

    /**
     * 处理目的地被伤害仙人掌、浆果包围时的情况
     * @param var0
     * @param var1
     * @param var2
     * @param var3
     * @param var4
     * @return
     */
    public PathType a(IBlockAccess var0, int var1, int var2, int var3, PathType var4) {
        if (var4 == PathType.WALKABLE) {
            BlockPosition.PooledBlockPosition var5 = BlockPosition.PooledBlockPosition.r();
            Throwable tt = null;

            try {
                for (int var7 = -1; var7 <= 1; ++var7) {
                    for (int var8 = -1; var8 <= 1; ++var8) {
                        if (var7 != 0 || var8 != 0) {
                            Block var9 = var0.getType(var5.d(var7 + var1, var2, var8 + var3)).getBlock();
                            if (var9 == Blocks.CACTUS) {// 仙人掌
                                var4 = PathType.DANGER_CACTUS;
                            } else if (var9 == Blocks.FIRE) {// 火
                                var4 = PathType.DANGER_FIRE;
                            } else if (var9 == Blocks.SWEET_BERRY_BUSH) {// 浆果
                                var4 = PathType.DANGER_OTHER;
                            }
                        }
                    }
                }
            } catch (Throwable var18) {
                tt = var18;
                throw var18;
            } finally {
                if (var5 != null) {
                    if (tt != null) {
                        try {
                            var5.close();
                        } catch (Throwable var17) {
                            tt.addSuppressed(var17);
                        }
                    } else {
                        var5.close();
                    }
                }

            }
        }

        return var4;
    }

    /**
     * 处理路径中包含门、铁轨、树叶等情况
     * @param var0
     * @param var1 可以破坏门
     * @param var2 可以通过门
     * @param var3
     * @param var4
     * @return
     */
    protected PathType a(IBlockAccess var0, boolean var1, boolean var2, BlockPosition var3, PathType var4) {
        if (var4 == PathType.DOOR_WOOD_CLOSED && var1 && var2) {
            var4 = PathType.WALKABLE;// 可到达
        }

        if (var4 == PathType.DOOR_OPEN && !var2) {// 不可通过门
            var4 = PathType.BLOCKED;
        }

        if (var4 == PathType.RAIL && !(var0.getType(var3).getBlock() instanceof BlockMinecartTrackAbstract)
                && !(var0.getType(var3.down()).getBlock() instanceof BlockMinecartTrackAbstract)) {
            var4 = PathType.FENCE; // 栏杆
        }

        if (var4 == PathType.LEAVES) {// 树叶
            var4 = PathType.BLOCKED;// 此路不通
        }

        return var4;
    }

    /**
     * 获取区块指定位置的 基本路径类型
     * @param var0
     * @param var1
     * @param var2
     * @param var3
     * @return
     */
    protected PathType b(IBlockAccess var0, int var1, int var2, int var3) {
        // 实体的目的地
        BlockPosition var4 = new BlockPosition(var1, var2, var3);
        // 目的地快类型
        IBlockData var5 = var0.getType(var4);
        Block var6 = var5.getBlock();
        Material var7 = var5.getMaterial();
        if (var5.isAir()) {
            return PathType.OPEN;
        } else if (!var6.a(TagsBlock.TRAPDOORS) && var6 != Blocks.LILY_PAD) {
            if (var6 == Blocks.FIRE) {
                return PathType.DAMAGE_FIRE;
            } else if (var6 == Blocks.CACTUS) {
                return PathType.DAMAGE_CACTUS;
            } else if (var6 == Blocks.SWEET_BERRY_BUSH) {
                return PathType.DAMAGE_OTHER;
            } else if (var6 instanceof BlockDoor && var7 == Material.WOOD && !(Boolean) var5.get(BlockDoor.OPEN)) {
                return PathType.DOOR_WOOD_CLOSED;
            } else if (var6 instanceof BlockDoor && var7 == Material.ORE && !(Boolean) var5.get(BlockDoor.OPEN)) {
                return PathType.DOOR_IRON_CLOSED;
            } else if (var6 instanceof BlockDoor && var5.get(BlockDoor.OPEN)) {
                return PathType.DOOR_OPEN;
            } else if (var6 instanceof BlockMinecartTrackAbstract) {
                return PathType.RAIL;
            } else if (var6 instanceof BlockLeaves) {
                return PathType.LEAVES;
            } else if (!var6.a(TagsBlock.FENCES) && !var6.a(TagsBlock.WALLS)
                    && (!(var6 instanceof BlockFenceGate) || var5.get(BlockFenceGate.OPEN))) {
                Fluid var8 = var0.getFluid(var4);
                if (var8.a(TagsFluid.WATER)) {
                    return PathType.WATER;
                } else if (var8.a(TagsFluid.LAVA)) {
                    return PathType.LAVA;
                } else {
                    return var5.a(var0, var4, PathMode.LAND) ? PathType.OPEN : PathType.BLOCKED;
                }
            } else {
                return PathType.FENCE;
            }
        } else {
            return PathType.TRAPDOOR;
        }
    }

    /**
     * 通过位置坐标创建一个 路径目的地 对象
     * @param v
     * @param v1
     * @param v2
     * @return
     */
    @Override
    public PathDestination a(double v, double v1, double v2) {
        // this.a(int,int,int) 通过位置坐标创建一个路径点对象
        // 通过路径点对象创建一个子对象 - 路径目的地
        return new PathDestination(this.a(MathHelper.floor(v), MathHelper.floor(v1), MathHelper.floor(v2)));
    }

    /**
     * 将 指定路径点 周围的有效路径点 加入到 路径数组中
     * @param pathPoints 路径点数组
     * @param pathPoint 指定路径点
     * @return
     */
    @Override
    public int a(PathPoint[] pathPoints, PathPoint pathPoint) {
        int var2 = 0;
        int var3 = 0;
        // 获取路径点 上方位置的 路径类型
        PathType var4 = this.a(this.a, pathPoint.a, pathPoint.b + 1, pathPoint.c, this.b);
        // 实体对此类型的权重
        if (this.b.a(var4) >= 0.0F) {
            var3 = MathHelper.d(Math.max(1.0F, this.b.K));
        }
        // 获取路径点的精确Y值
        double var5 = a(this.a, (new BlockPosition(pathPoint.a, pathPoint.b, pathPoint.c)));
        // 获取路径点 南(Z+1) 位置的路径点，并添加到路径点 数组中
        PathPoint var7 = this.a(pathPoint.a, pathPoint.b, pathPoint.c + 1, var3, var5, EnumDirection.SOUTH);
        if (var7 != null && !var7.i && var7.k >= 0.0F) {
            pathPoints[var2++] = var7;
        }
        // 获取路径点 西(X-1) 位置的路径点，并添加到路径点 数组中
        PathPoint var8 = this.a(pathPoint.a - 1, pathPoint.b, pathPoint.c, var3, var5, EnumDirection.WEST);
        if (var8 != null && !var8.i && var8.k >= 0.0F) {
            pathPoints[var2++] = var8;
        }
        // 获取路径点 东(X+1) 位置的路径点，并添加到路径点 数组中
        PathPoint var9 = this.a(pathPoint.a + 1, pathPoint.b, pathPoint.c, var3, var5, EnumDirection.EAST);
        if (var9 != null && !var9.i && var9.k >= 0.0F) {
            pathPoints[var2++] = var9;
        }
        // 获取路径点 北(Z-1) 位置的路径点，并添加到路径点 数组中
        PathPoint var10 = this.a(pathPoint.a, pathPoint.b, pathPoint.c - 1, var3, var5, EnumDirection.NORTH);
        if (var10 != null && !var10.i && var10.k >= 0.0F) {
            pathPoints[var2++] = var10;
        }
        // 获取路径点 西北(X-1 Z-1) 位置的路径点，并添加到路径点 数组中
        PathPoint var11 = this.a(pathPoint.a - 1, pathPoint.b, pathPoint.c - 1, var3, var5, EnumDirection.NORTH);
        if (this.a(pathPoint, var8, var10, var11)) {
            pathPoints[var2++] = var11;
        }
        // 获取路径点 东北(X-1 Z-1) 位置的路径点，并添加到路径点 数组中
        PathPoint var12 = this.a(pathPoint.a + 1, pathPoint.b, pathPoint.c - 1, var3, var5, EnumDirection.NORTH);
        if (this.a(pathPoint, var9, var10, var12)) {
            pathPoints[var2++] = var12;
        }
        // 获取路径点 西南(X-1 Z+1) 位置的路径点，并添加到路径点 数组中
        PathPoint var13 = this.a(pathPoint.a - 1, pathPoint.b, pathPoint.c + 1, var3, var5, EnumDirection.SOUTH);
        if (this.a(pathPoint, var8, var7, var13)) {
            pathPoints[var2++] = var13;
        }
        // 获取路径点 东南(X+1 Z+1) 位置的路径点，并添加到路径点 数组中
        PathPoint var14 = this.a(pathPoint.a + 1, pathPoint.b, pathPoint.c + 1, var3, var5, EnumDirection.SOUTH);
        if (this.a(pathPoint, var9, var7, var14)) {
            pathPoints[var2++] = var14;
        }

        return var2;
    }

    /**
     * 获取指定区域 指定块的 碰撞高度Y值(可处理半砖等方块)
     * @param var0 指定区域
     * @param var1 指定位置
     * @return
     */
    public static double a(IBlockAccess var0, BlockPosition var1) {
        BlockPosition var2 = var1.down();
        // 获取碰撞形状
        VoxelShape var3 = var0.getType(var2).getCollisionShape(var0, var2);
        return var2.getY() + (var3.isEmpty() ? 0.0D : var3.c(EnumDirection.EnumAxis.Y));
    }

    /**
     * 在指定路径点 向指定方向寻找 合理路径点
     * @param var0 x
     * @param var1 y
     * @param var2 z
     * @param var3 最多寻找次数
     * @param var4 中心路径点 精确Y值
     * @param var6 指定方向
     * @return
     */
    private PathPoint a(int var0, int var1, int var2, int var3, double var4, EnumDirection var6) {
        PathPoint var7 = null;
        BlockPosition var8 = new BlockPosition(var0, var1, var2);
        // 位置的 精确Y值
        double var9 = a(this.a, var8);
        // 此处位置为 原路径点的Z+1偏移， 此位置Y-路径点Y>最大跳跃高度？
        if (var9 - var4 > 1.125D) {
            return null;
        } else {
            // 此位置的路径类型
            PathType var11 = this.a(this.a, var0, var1, var2, this.b);
            float var12 = this.b.a(var11);
            // 实体宽度 / 2
            double var13 = this.b.getWidth() / 2.0D;
            // 实体对此路径类型的 权重
            if (var12 >= 0.0F) {
                // 创建路径点
                var7 = this.a(var0, var1, var2);
                var7.l = var11;
                var7.k = Math.max(var7.k, var12);
            }
            // 根据此位置的 路径点类型，返回此处的合适路径点
            if (var11 == PathType.WALKABLE) {
                // 此位置可达，则返回此路径点
                return var7;
            } else {
                // 此位置不存 则向上递归 查找可用位置
                if ((var7 == null || var7.k < 0.0F) && var3 > 0 && var11 != PathType.FENCE
                        && var11 != PathType.TRAPDOOR) {
                    // 向上递归调用
                    var7 = this.a(var0, var1 + 1, var2, var3 - 1, var4, var6);
                    if (var7 != null && (var7.l == PathType.OPEN || var7.l == PathType.WALKABLE)
                            && this.b.getWidth() < 1.0F) {
                        double var15 = var0 - var6.getAdjacentX() + 0.5D;
                        double var17 = var2 - var6.getAdjacentZ() + 0.5D;
                        AxisAlignedBB var19 = new AxisAlignedBB(var15 - var13,
                                a(this.a, (new BlockPosition(var15, var1 + 1, var17))) + 0.001D, var17 - var13,
                                var15 + var13,
                                this.b.getHeight() + a(this.a, (new BlockPosition(var7.a, var7.b, var7.c))) - 0.002D,
                                var17 + var13);
                        if (!this.a.getCubes(this.b, var19)) {
                            var7 = null;
                        }
                    }
                }
                // 此位置为水 则向下寻找 水低
                if (var11 == PathType.WATER && !this.e()) {
                    if (this.a(this.a, var0, var1 - 1, var2, this.b) != PathType.WATER) {
                        return var7;
                    }
                    while (var1 > 0) {
                        --var1;
                        var11 = this.a(this.a, var0, var1, var2, this.b);
                        if (var11 != PathType.WATER) {
                            return var7;
                        }

                        var7 = this.a(var0, var1, var2);
                        var7.l = var11;
                        var7.k = Math.max(var7.k, this.b.a(var11));
                    }
                }
                //
                if (var11 == PathType.OPEN) {
                    AxisAlignedBB var15 = new AxisAlignedBB(var0 - var13 + 0.5D, var1 + 0.001D, var2 - var13 + 0.5D,
                            var0 + var13 + 0.5D, var1 + this.b.getHeight(), var2 + var13 + 0.5D);
                    if (!this.a.getCubes(this.b, var15)) {
                        return null;
                    }

                    if (this.b.getWidth() >= 1.0F) {
                        PathType var16 = this.a(this.a, var0, var1 - 1, var2, this.b);
                        if (var16 == PathType.BLOCKED) {
                            var7 = this.a(var0, var1, var2);
                            var7.l = PathType.WALKABLE;
                            var7.k = Math.max(var7.k, var12);
                            return var7;
                        }
                    }

                    int var16 = 0;
                    int var17 = var1;

                    while (var11 == PathType.OPEN) {
                        --var1;
                        PathPoint var18;
                        if (var1 < 0) {
                            var18 = this.a(var0, var17, var2);
                            var18.l = PathType.BLOCKED;
                            var18.k = -1.0F;
                            return var18;
                        }

                        var18 = this.a(var0, var1, var2);
                        if (var16++ >= this.b.bv()) {
                            var18.l = PathType.BLOCKED;
                            var18.k = -1.0F;
                            return var18;
                        }

                        var11 = this.a(this.a, var0, var1, var2, this.b);
                        var12 = this.b.a(var11);
                        if (var11 != PathType.OPEN && var12 >= 0.0F) {
                            var7 = var18;
                            var18.l = var11;
                            var18.k = Math.max(var18.k, var12);
                            break;
                        }
                        if (var12 < 0.0F) {
                            var18.l = PathType.BLOCKED;
                            var18.k = -1.0F;
                            return var18;
                        }
                    }
                }
                return var7;
            }
        }
    }

    /**
     * 判断 [西北角] 是否能被添加到路径点数组
     * @param var0
     * @param var1
     * @param var2
     * @param var3
     * @return
     */
    private boolean a(PathPoint var0, PathPoint var1, PathPoint var2, PathPoint var3) {
        if (var3 != null && var2 != null && var1 != null) {
            if (var3.i) {
                return false;
            } else if (var2.b <= var0.b && var1.b <= var0.b) {
                return var3.k >= 0.0F && (var2.b < var0.b || var2.k >= 0.0F) && (var1.b < var0.b || var1.k >= 0.0F);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}

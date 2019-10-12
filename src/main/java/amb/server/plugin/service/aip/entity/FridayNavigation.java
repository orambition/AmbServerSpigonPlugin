package amb.server.plugin.service.aip.entity;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Location;

import java.util.Iterator;
import java.util.Set;

/**
 * 寻路导航
 */
public class FridayNavigation extends NavigationAbstract {
    private static double DEFAULT_PATHFINDING_RANGE = 25.0F;

    protected Friday a;
    protected FridayPathfinderNormal o;
    private AttributeInstance p;
    private FridayPathfinder s;
    /* 目的地块位置 */
    private BlockPosition q;
    private int r;


    public FridayNavigation(Friday var0, World var1) {
        super(new EntityInsentient(EntityTypes.VILLAGER, var1) {}, var1);
        this.a = var0;
        this.setRange(DEFAULT_PATHFINDING_RANGE);
        this.o = new FridayPathfinderNormal();
        this.o.a(true);
        this.s = new FridayPathfinder(this.o, 768);

    }
    public void setRange(double pathfindingRange) {
        AttributeInstance range = this.a.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        if (range == null){
            range = this.a.getAttributeMap().b(GenericAttributes.FOLLOW_RANGE);
        }
        this.p = range;
        this.p.setValue(pathfindingRange);
    }

    /**
     * 设置导航目标
     * @param location
     * @param speed
     * @return
     */
    public boolean setNavigationTarget(Location location, double speed, boolean allowTp){
        if (allowTp && this.a.getBukkitEntity().getLocation().distance(location) > this.p.getValue()){
            this.a.getBukkitEntity().teleport(location);
            return true;
        }
        boolean setSuccess = a(location, speed);
        if (setSuccess && allowTp && !this.c.h()){
            this.a.getBukkitEntity().teleport(location);
            return true;
        }
        return setSuccess;
    }
    /**
     * 设置指定位置为导航目标
     * @param location
     * @param speed
     * @return
     */
    public boolean a(Location location, double speed) {
        return a(location.getX(), location.getY(), location.getZ(), speed);
    }
    /**
     * 将指定位置设置为 导航目标
     * @param x
     * @param y
     * @param z
     * @param speed
     * @return
     */
    public boolean a(double x, double y, double z, double speed) {
        BlockPosition blockPosition = new BlockPosition(x, y, z);
        // 通过指定位置获取路径实体
        PathEntity pathEntity = this.a(ImmutableSet.of(blockPosition), 8, false, 1);

        if (pathEntity == null) {
            this.c = null;
            return false;
        } else {
            // 新路径实体 不等于当前 则更新
            if (!pathEntity.a(this.c)) {
                this.c = pathEntity;
            }
            this.D_();
            if (this.c.e() <= 0) {
                return false;
            } else {
                this.d = speed;
                // 实体 位置(可用的陆地 Y值)
                Vec3D var3 = this.b();
                this.f = this.e;
                this.g = var3;
                return true;
            }
        }
    }

    /**
     * 获取指定范围内的 到达 目的地 的路径实体
     * 通过 探路者指针 获取路径点 类型->判断路径点是否可用->通过 探路者 遍历 到达目的地的路径点
     * @param targetSet 目的地集合
     * @param minFoundRange 默认最小搜索区块范围
     * @param headHigh 一头部位置为0点搜索
     * @param targetArriveDistance 定义距离多少算到达 = 1
     * @return
     */
    @Override
    protected PathEntity a(Set targetSet, int minFoundRange, boolean headHigh, int targetArriveDistance) {
        if (targetSet.isEmpty()) {
            // 目标为空
            return null;
        } else if (this.a.locY < 0.0D) {
            // 实体在虚空中
            return null;
        } else if (!this.a()) {
            // 实体在空中
            return null;
        } else if (this.c != null && !this.c.b() && targetSet.contains(this.q)) {
            // 有前路径 并且 尚未执行完 并且 当前目标包含 前路径目标
            return this.c;
        } else {
            this.b.getMethodProfiler().enter("pathfind");
            // 返回初始化时设置的实体跟随范围：DEFAULT_PATHFINDING_RANGE=25
            float defaultFoundRange = this.i();
            // 设置最小为16*16的范围
            int pfr = (int) (defaultFoundRange + minFoundRange);
            // 获取当前实体位置，参数控制脚下还是头部位置
            BlockPosition entityBP = headHigh ? (new BlockPosition(this.a)).up() : new BlockPosition(this.a);
            // 获取实体所在区块，(世界,左下,右上),BlockPosition.b()为返回当前偏移后的位置
            IWorldReader entiytCC = new ChunkCache(this.b, entityBP.b(-pfr, -pfr, -pfr), entityBP.b(pfr, pfr, pfr));
            // 调用探路者获取路径实体，返回目的地集合中最近的一条
            PathEntity result = this.s.a(entiytCC, this.a, targetSet, defaultFoundRange, targetArriveDistance);
            this.b.getMethodProfiler().exit();
            if (result != null && result.k() != null) {
                // 获取目的地的块位置
                this.q = result.k();
                this.r = targetArriveDistance;
            }

            return result;
        }
    }

    /**
     * 执行导航
     */
    @Override
    public void c() {
        ++this.e;
        if (this.m) {
            this.k();
        }
        // 未完成
        if (!this.n()) {
            Vec3D var0;
            // 实体在陆地上
            if (this.a()) {
                // this.c.e++
                this.m();
            } else if (this.c != null && this.c.f() < this.c.e()) {
                // 获取当前实体的位置(陆地Y值)
                var0 = this.b();
                // 获取第一个路径点位置
                Vec3D var1 = this.c.a(this.a, this.c.f());
                // 实体已到达 路径点位置
                if (var0.y > var1.y && !this.a.onGround && MathHelper.floor(var0.x) == MathHelper.floor(var1.x)
                        && MathHelper.floor(var0.z) == MathHelper.floor(var1.z)) {
                    // this.c.e++
                    this.c.c(this.c.f() + 1);
                }
            }
            // 控制实体 移动到第一个路径点
            if (!this.n()) {
                var0 = this.c.a(this.a);
                BlockPosition var1 = new BlockPosition(var0);
                this.a.getControllerMove().a(
                        var0.x,
                        this.b.getType(var1.down()).isAir() ? var0.y : PathfinderNormal.a(this.b, var1),
                        var0.z,
                        this.d);
            }
        }
    }
    /**
     * 导航完成
     * @return
     */
    @Override
    public boolean n() {
        return this.c == null || this.c.b();
    }
    @Override
    public boolean r() {
        return this.o.e();
    }

    @Override
    protected Pathfinder a(int i) {
        return null;
    }

    /**
     * 实体在陆地上 或 在液体里 或 在载具里
     * 实体不在空中？
     * @return
     */
    @Override
    protected boolean a() {
        return this.a.onGround || this.p() || this.a.isPassenger();
    }

    /**
     * 判断实体是否到达路径点
     */
    @Override
    protected void m() {
        // 获取实体的位置(精确Y值)
        Vec3D var0 = this.b();
        this.l = this.a.getWidth() > 0.75F ? this.a.getWidth() / 2.0F : 0.75F - this.a.getWidth() / 2.0F;
        Vec3D var1 = this.c.g();
        // 实体已到达 路径点
        if (Math.abs(this.a.locX - (var1.x + 0.5D)) < this.l && Math.abs(this.a.locZ - (var1.z + 0.5D)) < this.l
                && Math.abs(this.a.locY - var1.y) < 1.0D) {
            this.c.c(this.c.f() + 1);
        }

        this.a(var0);
    }

    @Override
    protected boolean a(Vec3D var0, Vec3D var1, int var2, int var3, int var4) {
        int var5 = MathHelper.floor(var0.x);
        int var6 = MathHelper.floor(var0.z);
        double var7 = var1.x - var0.x;
        double var9 = var1.z - var0.z;
        double var11 = var7 * var7 + var9 * var9;
        if (var11 < 1.0E-8D) {
            return false;
        } else {
            double var13 = 1.0D / Math.sqrt(var11);
            var7 *= var13;
            var9 *= var13;
            var2 += 2;
            var4 += 2;
            if (!this.a(var5, MathHelper.floor(var0.y), var6, var2, var3, var4, var0, var7, var9)) {
                return false;
            } else {
                var2 -= 2;
                var4 -= 2;
                double var15 = 1.0D / Math.abs(var7);
                double var17 = 1.0D / Math.abs(var9);
                double var19 = var5 - var0.x;
                double var21 = var6 - var0.z;
                if (var7 >= 0.0D) {
                    ++var19;
                }

                if (var9 >= 0.0D) {
                    ++var21;
                }

                var19 /= var7;
                var21 /= var9;
                int var23 = var7 < 0.0D ? -1 : 1;
                int var24 = var9 < 0.0D ? -1 : 1;
                int var25 = MathHelper.floor(var1.x);
                int var26 = MathHelper.floor(var1.z);
                int var27 = var25 - var5;
                int var28 = var26 - var6;

                do {
                    if (var27 * var23 <= 0 && var28 * var24 <= 0) {
                        return true;
                    }

                    if (var19 < var21) {
                        var19 += var15;
                        var5 += var23;
                        var27 = var25 - var5;
                    } else {
                        var21 += var17;
                        var6 += var24;
                        var28 = var26 - var6;
                    }
                } while (this.a(var5, MathHelper.floor(var0.y), var6, var2, var3, var4, var0, var7, var9));

                return false;
            }
        }
    }
    private boolean a(int var0, int var1, int var2, int var3, int var4, int var5, Vec3D var6, double var7,
                      double var9) {
        int var11 = var0 - var3 / 2;
        int var12 = var2 - var5 / 2;
        if (!this.b(var11, var1, var12, var3, var4, var5, var6, var7, var9)) {
            return false;
        } else {
            for (int var13 = var11; var13 < var11 + var3; ++var13) {
                for (int var14 = var12; var14 < var12 + var5; ++var14) {
                    double var15 = var13 + 0.5D - var6.x;
                    double var17 = var14 + 0.5D - var6.z;
                    if (var15 * var7 + var17 * var9 >= 0.0D) {
                        PathType var19 = this.o.a(this.b, var13, var1 - 1, var14, this.a, var3, var4, var5, true, true);
                        if (var19 == PathType.WATER) {
                            return false;
                        }

                        if (var19 == PathType.LAVA) {
                            return false;
                        }

                        if (var19 == PathType.OPEN) {
                            return false;
                        }

                        var19 = this.o.a(this.b, var13, var1, var14, this.a, var3, var4, var5, true, true);
                        float var20 = this.a.a(var19);
                        if (var20 < 0.0F || var20 >= 8.0F) {
                            return false;
                        }

                        if (var19 == PathType.DAMAGE_FIRE || var19 == PathType.DANGER_FIRE
                                || var19 == PathType.DAMAGE_OTHER) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    /**
     * 获取当前实体的可用 陆地Y值
     * @return
     */
    @Override
    protected Vec3D b() {
        return new Vec3D(this.a.locX, this.t(), this.a.locZ);
    }

    /**
     * 获取当前实体的Y轴 陆地高度
     * @return
     */
    private int t() {
        if (this.a.isInWater() && this.r()) {
            int var0 = MathHelper.floor(this.a.getBoundingBox().minY);
            Block var1 = this.b.getType(new BlockPosition(this.a.locX, var0, this.a.locZ)).getBlock();
            int var2 = 0;

            do {
                if (var1 != Blocks.WATER) {
                    return var0;
                }

                ++var0;
                var1 = this.b.getType(new BlockPosition(this.a.locX, var0, this.a.locZ)).getBlock();
                ++var2;
            } while (var2 <= 16);

            return MathHelper.floor(this.a.getBoundingBox().minY);
        } else {
            return MathHelper.floor(this.a.getBoundingBox().minY + 0.5D);
        }
    }
    private boolean b(int var0, int var1, int var2, int var3, int var4, int var5, Vec3D var6, double var7,
                      double var9) {
        Iterator var12 = BlockPosition.a(new BlockPosition(var0, var1, var2),
                new BlockPosition(var0 + var3 - 1, var1 + var4 - 1, var2 + var5 - 1)).iterator();

        BlockPosition var14;
        double var13;
        double var15;
        do {
            if (!var12.hasNext()) {
                return true;
            }

            var14 = (BlockPosition) var12.next();
            var13 = var14.getX() + 0.5D - var6.x;
            var15 = var14.getZ() + 0.5D - var6.z;
        } while (var13 * var7 + var15 * var9 < 0.0D || this.b.getType(var14).a(this.b, var14, PathMode.LAND));

        return false;
    }
}

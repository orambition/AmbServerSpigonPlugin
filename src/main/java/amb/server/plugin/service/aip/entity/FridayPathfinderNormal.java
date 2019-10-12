package amb.server.plugin.service.aip.entity;

import com.google.common.collect.Sets;
import net.minecraft.server.v1_14_R1.*;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Ѱ·�� ָ��
 */
public class FridayPathfinderNormal extends PathfinderAbstract {
    protected float j;
    protected Friday b;

    /**
     * ��ʼ������
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
     * ��ȡʵ����Y���Ŀ�ĵ�, ��ˮ������
     * @return
     */
    @Override
    public PathPoint b() {
        // ʵ��Ҫ����Yֵ
        int var0 = 0;
        BlockPosition var1;
        if (this.e() && this.b.isInWater()) {
            /*// ��ȡʵ��󶨺���͵�
            var0 = MathHelper.floor(this.b.getBoundingBox().minY);
            // ��ȡʵ��ŵ׷���λ��
            var1 = new BlockPosition.MutableBlockPosition(this.b.locX, var0, this.b.locZ);
            // �ӽŵ׷��鿪ʼ���ϱ�����ֱ������ˮ
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
            // ��½�ط�����
            var0 = MathHelper.floor(this.b.getBoundingBox().minY + 0.5D);
        } else {
            // �����λ�ÿ�ʼ���±�����ֱ�����ǿ���
            for (var1 = new BlockPosition(this.b);
                 (this.a.getType(var1).isAir() || this.a.getType(var1).a(this.a, var1, PathMode.LAND)) && var1.getY() > 0;
                 var1 = var1.down()) {
            }
            var0 = var1.up().getY();
        }

        var1 = new BlockPosition(this.b);
        // ��ȡָ��λ�õ�·������
        PathType pathType = this.a(this.a, var1.getX(), var0, var1.getZ(), this.b);
        // ʵ��Դ����͵�Ȩ��<0,���ɴ�
        if (this.b.a(pathType) < 0.0F) {
            // ��ȡĿ�ĵ���Χ����4�������λ��
            Set var3 = Sets.newHashSet();
            var3.add(new BlockPosition(this.b.getBoundingBox().minX, var0, this.b.getBoundingBox().minZ));
            var3.add(new BlockPosition(this.b.getBoundingBox().minX, var0, this.b.getBoundingBox().maxZ));
            var3.add(new BlockPosition(this.b.getBoundingBox().maxX, var0, this.b.getBoundingBox().minZ));
            var3.add(new BlockPosition(this.b.getBoundingBox().maxX, var0, this.b.getBoundingBox().maxZ));
            Iterator var5 = var3.iterator();
            // ������Χ����
            while (var5.hasNext()) {
                BlockPosition var4 = (BlockPosition) var5.next();
                // ��ȡ��Χ�����·������
                PathType var6 = this.a(this.a, var4.getX(), var4.getY(), var4.getZ(), this.b);
                // ��ʵ�����Щ�����Ȩ�ش���0���򷵻���Ӧ��·����
                if (this.b.a(var6) >= 0.0F) {
                    return this.a(var4.getX(), var4.getY(), var4.getZ());
                }
            }
        }
        // ����λ����Ϣ����·����(�˷��������棬�ظ��ص㲻�����´���)
        return this.a(var1.getX(), var0, var1.getZ());
    }

    /**
     * ��ȡָ�����顢ָ��λ�á�ָ��ʵ���·������
     * �򵥵ķ�װ
     */
    private PathType a(IBlockAccess var0, int var1, int var2, int var3, Friday var4) {
        // (����,Ŀ�ĵ�X,Ŀ�ĵ�Y,Ŀ�ĵ�Z,ʵ��,ʵ���,ʵ���,ʵ���,���ƻ���,��ͨ����)
        return this.a(var0, var1, var2, var3, var4, this.d, this.e, this.f, this.d(), this.c());
    }

    @Override
    public PathType a(IBlockAccess iBlockAccess, int i, int i1, int i2, EntityInsentient entityInsentient, int i3, int i4, int i5, boolean b, boolean b1) { return null; }
    /**
     * ��ȡ������ָ��(var1,var2,var3)λ�õ�·������
     *  (��д����ķ�������Ϊ��Ҫʹ��fridayʵ��)
     * 1.����Ŀ�ĵؿ����ͻ�ȡ�������·������
     * 2.Ŀ�ĵ��ڿ���ʱ��·��������Ŀ�ĵ���һ�񷽿�Ϊ׼
     * 3.Ŀ�ĵ���Χ�������ơ��𡢽���ʱ��·��Ϊ�˺�����
     * 4.Ŀ�ĵ�Ϊ�š����졢��Ҷʱ������ʵ�����������Ƿ�ɵ���
     * 5.����ͬ�����ȡĿ�ĵ���Χ3*3��·������
     * 6.Ŀ�ĵ���Χ��դ�����򷵻�դ������
     * 7.������ʵ��Բ�ͬ·�����͵�Ȩ�ؽ��з���
     */
    public PathType a(IBlockAccess var0, int var1, int var2, int var3, Friday var4, int var5, int var6, int var7, boolean var8, boolean var9) {
        // Ŀ�ĵ���Χ3*3·������
        EnumSet var10 = EnumSet.noneOf(PathType.class);
        // Ŀ�ĵ�·������
        PathType var11 = PathType.BLOCKED;
        //double var12 = var4.getWidth() / 2.0D;
        // ʵ��Ŀ�λ��
        BlockPosition var14 = new BlockPosition(var4);
        // ��ȡ·�����ͣ�(����,Ŀ�ĵ�X,Ŀ�ĵ�Y,Ŀ�ĵ�Z,ʵ���,ʵ���,ʵ���,,,·������ö��,Ĭ������,ʵ��λ��)
        var11 = this.a(var0, var1, var2, var3, var5, var6, var7, var8, var9, var10, var11, var14);

        if (var10.contains(PathType.FENCE)) {
            // ��Χ��դ��
            return PathType.FENCE;
        } else {
            // ����ʵ��Բ�ͬ·�����͵�Ȩ�� ����·������
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
     * ��ȡĿ�ĵؼ���Χ3*3�����·������
     * @param var0 ����������
     * @param var1 Ŀ�ĵ�X
     * @param var2 Ŀ�ĵ�Y
     * @param var3 Ŀ�ĵ�Z
     * @param var4 ʵ���
     * @param var5 ʵ���
     * @param var6 ʵ���
     * @param var7 ���ƻ���
     * @param var8 ��ͨ����
     * @param var9 Ŀ�ĵ���Χ3*3��·�����ͼ�
     * @param var10 Ŀ�ĵص�·������
     * @param var11 ʵ��λ��
     * @return
     */
    public PathType a(IBlockAccess var0, int var1, int var2, int var3, int var4, int var5, int var6, boolean var7,
                      boolean var8, EnumSet var9, PathType var10, BlockPosition var11) {
        // ʵ���+1
        for (int var12 = 0; var12 < var4; ++var12) {
            // ʵ���+1
            for (int var13 = 0; var13 < var5; ++var13) {
                // ʵ���+1
                for (int var14 = 0; var14 < var6; ++var14) {
                    // + ʵ���Ŀ�ĵ�
                    int var15 = var12 + var1;
                    int var16 = var13 + var2;
                    int var17 = var14 + var3;
                    // ��ȡĿ�ĵ�·������
                    PathType var18 = this.a(var0, var15, var16, var17);
                    // ����·���������š����졢��Ҷ�����(����,���ƻ���,��ͨ����,ʵ��λ�ã�Ŀ��·������)
                    var18 = this.a(var0, var7, var8, var11, var18);
                    if (var12 == 0 && var13 == 0 && var14 == 0) {
                        // ����Ŀ�ĵص�·������
                        var10 = var18;
                    }
                    // Ŀ�ĵ���Χ3*3��·������
                    var9.add(var18);
                }
            }
        }
        return var10;
    }

    /**
     * ��ȡĿ�ĵص�·������
     * ���� ���С�����Χ���������
     * @param var0
     * @param var1
     * @param var2
     * @param var3
     * @return
     */
    @Override
    public PathType a(IBlockAccess var0, int var1, int var2, int var3) {
        // ��ȡ����ָ��λ�õ�·������
        PathType var4 = this.b(var0, var1, var2, var3);
        if (var4 == PathType.OPEN && var2 >= 1) {
            // ���·��ʱ������������һ���·������λ��
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
        // �������ơ������Ȱ�Χ��·��ͬΪ�˺�·��
        var4 = this.a(var0, var1, var2, var3, var4);
        return var4;
    }

    /**
     * ����Ŀ�ĵر��˺������ơ�������Χʱ�����
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
                            if (var9 == Blocks.CACTUS) {// ������
                                var4 = PathType.DANGER_CACTUS;
                            } else if (var9 == Blocks.FIRE) {// ��
                                var4 = PathType.DANGER_FIRE;
                            } else if (var9 == Blocks.SWEET_BERRY_BUSH) {// ����
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
     * ����·���а����š����졢��Ҷ�����
     * @param var0
     * @param var1 �����ƻ���
     * @param var2 ����ͨ����
     * @param var3
     * @param var4
     * @return
     */
    protected PathType a(IBlockAccess var0, boolean var1, boolean var2, BlockPosition var3, PathType var4) {
        if (var4 == PathType.DOOR_WOOD_CLOSED && var1 && var2) {
            var4 = PathType.WALKABLE;// �ɵ���
        }

        if (var4 == PathType.DOOR_OPEN && !var2) {// ����ͨ����
            var4 = PathType.BLOCKED;
        }

        if (var4 == PathType.RAIL && !(var0.getType(var3).getBlock() instanceof BlockMinecartTrackAbstract)
                && !(var0.getType(var3.down()).getBlock() instanceof BlockMinecartTrackAbstract)) {
            var4 = PathType.FENCE; // ����
        }

        if (var4 == PathType.LEAVES) {// ��Ҷ
            var4 = PathType.BLOCKED;// ��·��ͨ
        }

        return var4;
    }

    /**
     * ��ȡ����ָ��λ�õ� ����·������
     * @param var0
     * @param var1
     * @param var2
     * @param var3
     * @return
     */
    protected PathType b(IBlockAccess var0, int var1, int var2, int var3) {
        // ʵ���Ŀ�ĵ�
        BlockPosition var4 = new BlockPosition(var1, var2, var3);
        // Ŀ�ĵؿ�����
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
     * ͨ��λ�����괴��һ�� ·��Ŀ�ĵ� ����
     * @param v
     * @param v1
     * @param v2
     * @return
     */
    @Override
    public PathDestination a(double v, double v1, double v2) {
        // this.a(int,int,int) ͨ��λ�����괴��һ��·�������
        // ͨ��·������󴴽�һ���Ӷ��� - ·��Ŀ�ĵ�
        return new PathDestination(this.a(MathHelper.floor(v), MathHelper.floor(v1), MathHelper.floor(v2)));
    }

    /**
     * �� ָ��·���� ��Χ����Ч·���� ���뵽 ·��������
     * @param pathPoints ·��������
     * @param pathPoint ָ��·����
     * @return
     */
    @Override
    public int a(PathPoint[] pathPoints, PathPoint pathPoint) {
        int var2 = 0;
        int var3 = 0;
        // ��ȡ·���� �Ϸ�λ�õ� ·������
        PathType var4 = this.a(this.a, pathPoint.a, pathPoint.b + 1, pathPoint.c, this.b);
        // ʵ��Դ����͵�Ȩ��
        if (this.b.a(var4) >= 0.0F) {
            var3 = MathHelper.d(Math.max(1.0F, this.b.K));
        }
        // ��ȡ·����ľ�ȷYֵ
        double var5 = a(this.a, (new BlockPosition(pathPoint.a, pathPoint.b, pathPoint.c)));
        // ��ȡ·���� ��(Z+1) λ�õ�·���㣬����ӵ�·���� ������
        PathPoint var7 = this.a(pathPoint.a, pathPoint.b, pathPoint.c + 1, var3, var5, EnumDirection.SOUTH);
        if (var7 != null && !var7.i && var7.k >= 0.0F) {
            pathPoints[var2++] = var7;
        }
        // ��ȡ·���� ��(X-1) λ�õ�·���㣬����ӵ�·���� ������
        PathPoint var8 = this.a(pathPoint.a - 1, pathPoint.b, pathPoint.c, var3, var5, EnumDirection.WEST);
        if (var8 != null && !var8.i && var8.k >= 0.0F) {
            pathPoints[var2++] = var8;
        }
        // ��ȡ·���� ��(X+1) λ�õ�·���㣬����ӵ�·���� ������
        PathPoint var9 = this.a(pathPoint.a + 1, pathPoint.b, pathPoint.c, var3, var5, EnumDirection.EAST);
        if (var9 != null && !var9.i && var9.k >= 0.0F) {
            pathPoints[var2++] = var9;
        }
        // ��ȡ·���� ��(Z-1) λ�õ�·���㣬����ӵ�·���� ������
        PathPoint var10 = this.a(pathPoint.a, pathPoint.b, pathPoint.c - 1, var3, var5, EnumDirection.NORTH);
        if (var10 != null && !var10.i && var10.k >= 0.0F) {
            pathPoints[var2++] = var10;
        }
        // ��ȡ·���� ����(X-1 Z-1) λ�õ�·���㣬����ӵ�·���� ������
        PathPoint var11 = this.a(pathPoint.a - 1, pathPoint.b, pathPoint.c - 1, var3, var5, EnumDirection.NORTH);
        if (this.a(pathPoint, var8, var10, var11)) {
            pathPoints[var2++] = var11;
        }
        // ��ȡ·���� ����(X-1 Z-1) λ�õ�·���㣬����ӵ�·���� ������
        PathPoint var12 = this.a(pathPoint.a + 1, pathPoint.b, pathPoint.c - 1, var3, var5, EnumDirection.NORTH);
        if (this.a(pathPoint, var9, var10, var12)) {
            pathPoints[var2++] = var12;
        }
        // ��ȡ·���� ����(X-1 Z+1) λ�õ�·���㣬����ӵ�·���� ������
        PathPoint var13 = this.a(pathPoint.a - 1, pathPoint.b, pathPoint.c + 1, var3, var5, EnumDirection.SOUTH);
        if (this.a(pathPoint, var8, var7, var13)) {
            pathPoints[var2++] = var13;
        }
        // ��ȡ·���� ����(X+1 Z+1) λ�õ�·���㣬����ӵ�·���� ������
        PathPoint var14 = this.a(pathPoint.a + 1, pathPoint.b, pathPoint.c + 1, var3, var5, EnumDirection.SOUTH);
        if (this.a(pathPoint, var9, var7, var14)) {
            pathPoints[var2++] = var14;
        }

        return var2;
    }

    /**
     * ��ȡָ������ ָ����� ��ײ�߶�Yֵ(�ɴ����ש�ȷ���)
     * @param var0 ָ������
     * @param var1 ָ��λ��
     * @return
     */
    public static double a(IBlockAccess var0, BlockPosition var1) {
        BlockPosition var2 = var1.down();
        // ��ȡ��ײ��״
        VoxelShape var3 = var0.getType(var2).getCollisionShape(var0, var2);
        return var2.getY() + (var3.isEmpty() ? 0.0D : var3.c(EnumDirection.EnumAxis.Y));
    }

    /**
     * ��ָ��·���� ��ָ������Ѱ�� ����·����
     * @param var0 x
     * @param var1 y
     * @param var2 z
     * @param var3 ���Ѱ�Ҵ���
     * @param var4 ����·���� ��ȷYֵ
     * @param var6 ָ������
     * @return
     */
    private PathPoint a(int var0, int var1, int var2, int var3, double var4, EnumDirection var6) {
        PathPoint var7 = null;
        BlockPosition var8 = new BlockPosition(var0, var1, var2);
        // λ�õ� ��ȷYֵ
        double var9 = a(this.a, var8);
        // �˴�λ��Ϊ ԭ·�����Z+1ƫ�ƣ� ��λ��Y-·����Y>�����Ծ�߶ȣ�
        if (var9 - var4 > 1.125D) {
            return null;
        } else {
            // ��λ�õ�·������
            PathType var11 = this.a(this.a, var0, var1, var2, this.b);
            float var12 = this.b.a(var11);
            // ʵ���� / 2
            double var13 = this.b.getWidth() / 2.0D;
            // ʵ��Դ�·�����͵� Ȩ��
            if (var12 >= 0.0F) {
                // ����·����
                var7 = this.a(var0, var1, var2);
                var7.l = var11;
                var7.k = Math.max(var7.k, var12);
            }
            // ���ݴ�λ�õ� ·�������ͣ����ش˴��ĺ���·����
            if (var11 == PathType.WALKABLE) {
                // ��λ�ÿɴ�򷵻ش�·����
                return var7;
            } else {
                // ��λ�ò��� �����ϵݹ� ���ҿ���λ��
                if ((var7 == null || var7.k < 0.0F) && var3 > 0 && var11 != PathType.FENCE
                        && var11 != PathType.TRAPDOOR) {
                    // ���ϵݹ����
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
                // ��λ��Ϊˮ ������Ѱ�� ˮ��
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
     * �ж� [������] �Ƿ��ܱ���ӵ�·��������
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

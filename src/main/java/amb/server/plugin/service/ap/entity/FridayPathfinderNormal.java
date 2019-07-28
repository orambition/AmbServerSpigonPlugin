package amb.server.plugin.service.ap.entity;

import net.minecraft.server.v1_14_R1.*;

/**
 * Ñ°Â·Æ÷ Ö¸Õë
 */
public class FridayPathfinderNormal extends PathfinderAbstract {
    protected Friday b;
    @Override
    public PathPoint b() {
        return null;
    }

    public void a(IWorldReader var0, Friday var1) {
        this.a = var0;
        this.b = var1;
        this.c.clear();
        this.d = MathHelper.d(var1.getWidth() + 1.0F);
        this.e = MathHelper.d(var1.getHeight() + 1.0F);
        this.f = MathHelper.d(var1.getWidth() + 1.0F);
    }

    @Override
    public PathDestination a(double v, double v1, double v2) {
        return new PathDestination(this.a(MathHelper.floor(v), MathHelper.floor(v1), MathHelper.floor(v2)));
    }

    @Override
    public int a(PathPoint[] pathPoints, PathPoint pathPoint) {
        return 0;
    }

    @Override
    public PathType a(IBlockAccess iBlockAccess, int i, int i1, int i2, EntityInsentient entityInsentient, int i3, int i4, int i5, boolean b, boolean b1) {
        return null;
    }

    @Override
    public PathType a(IBlockAccess iBlockAccess, int i, int i1, int i2) {
        return null;
    }
}

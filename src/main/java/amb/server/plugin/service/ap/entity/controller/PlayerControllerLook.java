package amb.server.plugin.service.ap.entity.controller;

import amb.server.plugin.service.ap.entity.Friday;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.Vec3D;

public class PlayerControllerLook {
    private final Friday a;
    protected float b;
    protected float c;
    protected boolean d;
    protected double e;
    protected double f;
    protected double g;

    public PlayerControllerLook(Friday var0) {
        this.a = var0;
    }

    public void a() {
        if (!this.a.getNavigation().n())
            return;
        if (this.b()) {
            // this.a.pitch = 0.0F;
        }
        if (this.d) {
            this.d = false;
            this.a.aM = this.a(this.a.aM, this.h(), this.b);
            this.a.yaw = this.a.aM;
            this.a.pitch = this.a(this.a.pitch, this.g(), this.c);
        } else {
            // this.a.aM = this.a(this.a.aM, this.a.aK, 10.0F);
        }

        if (!this.a.getNavigation().n()) {
            this.a.aM = MathHelper.b(this.a.aM, this.a.aK, 75);
        }
    }

    public void a(double var0, double var2, double var4) {
        this.a(var0, var2, var4, 10, 40);
    }

    public void a(double var0, double var2, double var4, float var6, float var7) {
        this.e = var0;
        this.f = var2;
        this.g = var4;
        this.b = var6;
        this.c = var7;
        this.d = true;
    }

    public void a(Entity var0, float var1, float var2) {
        this.a(var0.locX, b(var0), var0.locZ, var1, var2);
    }

    protected float a(float var0, float var1, float var2) {
        float var3 = MathHelper.c(var0, var1);
        float var4 = MathHelper.a(var3, -var2, var2);
        return var0 + var4;
    }

    public void a(Vec3D var0) {
        this.a(var0.x, var0.y, var0.z);
    }

    protected boolean b() {
        return true;
    }

    public boolean c() {
        return this.d;
    }

    public double d() {
        return this.e;
    }

    public double e() {
        return this.f;
    }

    public double f() {
        return this.g;
    }

    protected float g() {
        double var0 = this.e - this.a.locX;
        double var2 = this.f - (this.a.locY + this.a.getHeadHeight());
        double var4 = this.g - this.a.locZ;
        double var6 = MathHelper.sqrt(var0 * var0 + var4 * var4);
        return (float) (-(MathHelper.d(var2, var6) * 57.2957763671875D));
    }

    protected float h() {
        double var0 = this.e - this.a.locX;
        double var2 = this.g - this.a.locZ;
        return (float) (MathHelper.d(var2, var0) * 57.2957763671875D) - 90.0F;
    }

    private static double b(Entity var0) {
        return var0 instanceof EntityLiving ? var0.locY + var0.getHeadHeight()
                : (var0.getBoundingBox().minY + var0.getBoundingBox().maxY) / 2.0D;
    }
}

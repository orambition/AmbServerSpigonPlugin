package amb.server.plugin.service.aip.entity.controller;

import amb.server.plugin.service.aip.entity.Friday;
import amb.server.plugin.tools.NMSUtil;
import net.minecraft.server.v1_14_R1.*;

import java.util.Random;

public class PlayerControllerMove extends ControllerMove {
    protected EntityLiving a;
    protected boolean f;

    public PlayerControllerMove(EntityLiving var0) {
        super(var0 instanceof EntityInsentient ? (EntityInsentient)var0 :
                new EntitySlime(EntityTypes.SLIME, var0.getWorld()));
        this.a = var0;
        this.b = var0.locX;
        this.c = var0.locY;
        this.d = var0.locZ;
    }

    @Override
    public void a() {
        this.a.bd = 0F;
        if (this.f) {
            this.f = false;
            int i = MathHelper.floor(this.a.getBoundingBox().minY + 0.5D);
            // 目的地到当前位置距离
            double d0 = this.b - this.a.locX;
            double d1 = this.d - this.a.locZ;
            double d2 = this.c - i;
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;
            //
            if (d3 < 2.500000277905201E-007D) {
                this.a.bd = 0.0F;
                return;
            }
            float f = (float) Math.toDegrees(Math.atan2(d1, d0)) - 90.0F;
            this.a.yaw = a(this.a.yaw, f, 90.0F);
            NMSUtil.setHeadYaw(a.getBukkitEntity(), this.a.yaw);
            AttributeInstance speed = this.a.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
            if (this.a instanceof EntitySlime) {
                speed.setValue(0.3D * this.e);
            } else {
                speed.setValue(0.2D * this.e);
            }
            float movement = (float) (this.e * speed.getValue());
            this.a.o(movement);
            this.a.bd = movement;// 设置向前的移动速度
            if (((d2 > 0.0D) && (d0 * d0 + d1 * d1 < 1.0D))) {
                if (this.a instanceof Friday) {
                    ((Friday) this.a).getControllerJump().jump();
                } else {
                    ((EntityInsentient) this.a).getControllerJump().jump();
                }
            }
        }
    }

    protected int cg() {
        return new Random().nextInt(20) + 10;
    }
    @Override
    public void a(double x, double y, double z, double speed) {
        this.b = x;
        this.c = y;
        this.d = z;
        this.e = speed;
        this.f = true;
    }

    @Override
    protected float a(float var0, float var1, float var2) {
        float var3 = MathHelper.g(var1 - var0);
        if (var3 > var2) {
            var3 = var2;
        }

        if (var3 < -var2) {
            var3 = -var2;
        }

        float var4 = var0 + var3;
        if (var4 < 0.0F) {
            var4 += 360.0F;
        } else if (var4 > 360.0F) {
            var4 -= 360.0F;
        }

        return var4;
    }

    @Override
    public boolean b() {
        return this.f;
    }

}

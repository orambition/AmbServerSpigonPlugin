package amb.server.plugin.service.ap.entity;

import net.minecraft.server.v1_14_R1.*;

/**
 * 寻路导航
 */
public class FridayNavigation extends NavigationAbstract {
    private static double DEFAULT_PATHFINDING_RANGE = 25.0F;

    protected Friday a;
    protected FridayPathfinderNormal o;
    private final AttributeInstance p;
    private Pathfinder s;


    public FridayNavigation(Friday var0, World var1) {
        super(new EntityInsentient(EntityTypes.VILLAGER, var1) {}, var1);
        this.a = var0;
        AttributeInstance range = var0.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        if (range == null){
            range = var0.getAttributeMap().b(GenericAttributes.FOLLOW_RANGE);
        }
        this.p = range;
        this.o = new FridayPathfinderNormal();
        this.o.a(true);
        this.s = new FridayPathfinder(this.o, 768);
        this.setRange(DEFAULT_PATHFINDING_RANGE);
    }

    public void setRange(double pathfindingRange) {
        this.p.setValue(pathfindingRange);
    }

    /**
     * 这些方法在父类中
     * 导航完成
     * public boolean n() {……}
     *
     * 执行导航
     * public boolean c() {……}
     */

    @Override
    protected Pathfinder a(int i) {
        return null;
    }

    @Override
    protected Vec3D b() {
        return null;
    }

    @Override
    protected boolean a() {
        return false;
    }

    @Override
    protected boolean a(Vec3D vec3D, Vec3D vec3D1, int i, int i1, int i2) {
        return false;
    }
}

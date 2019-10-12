package amb.server.plugin.service.aip.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_14_R1.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Ѱ·��
 * ��������·���㣬������·��ʵ��
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
     * ��ָ�������� ��ȡĿ�ĵص�·��ʵ��
     * @param entityCC ����
     * @param friday ʵ��
     * @param targetSet Ŀ�ĵ�λ�ü���
     * @param defaultFoundRange ʵ��ɼ���Χ
     * @param targetArriveDistance �����������㵽��
     * @return
     */
    public PathEntity a(IWorldReader entityCC, Friday friday, Set<BlockPosition> targetSet, float defaultFoundRange, int targetArriveDistance){
        // ��ʼ��·��
        this.a.a();
        // ��ʼ��̽·��ָ��
        this.e.a(entityCC, friday);
        // ͨ��̽·��ָ�� ��ȡʵ��Y�� Ŀ�ĵ� �� ·����
        PathPoint pathPointY = this.e.b();
        // ͨ��̽·��ָ�� ��Ŀ�ĵ�λ�� ת��Ϊ ��·��Ŀ�ĵء������λ�õ�MAP
        Map var6 = targetSet.stream().collect(Collectors.toMap(
                (var0x) -> this.e.a((double) var0x.getX(), (double) var0x.getY(), (double) var0x.getZ()),
                Function.identity()));
        // ��ȡ·������ Y��Ŀ�ĵء�·��Ŀ�ĵء�ʵ��ɼ���Χ����������㵽��
        PathEntity var7 = this.a(pathPointY, var6, defaultFoundRange, targetArriveDistance);
        // �ͷ�̽·��ָ��
        this.e.a();
        return var7;
    }

    /**
     * ��Ҫ����������Ŀ�ĵص�·��ʵ��
     * ��Y�� Ŀ�ĵ� Ϊ���ģ������������Ӿ���ķ�Χ�����Ŀ�ĵ��Ƿ�ɴ�
     * @param var0 Y�� Ŀ�ĵ� ���������� Ҳ�ǵ�һ�����
     * @param var1 ·��Ŀ�ĵ�
     * @param var2 �ɼ���Χ
     * @param var3 ���� ����Ŀ�ĵض�Զ �ɱ���Ϊ����
     * @return
     */
    private PathEntity a(PathPoint var0, Map<PathDestination, BlockPosition> var1, float var2, int var3) {
        // ·��Ŀ�ĵؼ���
        Set<PathDestination> var4 = var1.keySet();
        var0.e = 0.0F;
        // ������Y��Ŀ�ĵ������ ·��Ŀ�ĵ� ����
        // ���� Y��Ŀ�ĵ� ��Ϊ Ҫ���� ·��Ŀ�ĵ� �ĵ�һ���ؾ�֮·
        var0.f = this.a(var0, var4);
        var0.g = var0.f;
        // ·����ʼ������·��������������Ϊ0
        this.a.a();
        this.b.clear();
        // ��Y��Ŀ�ĵ� ����·�������·����������
        this.a.a(var0);
        int var5 = 0;
        // ·�������b !=0
        while(!this.a.e()) {
            ++var5;
            // d �ڳ�ʼ��ʱ����Ϊ��768����ζ����ೢ��768��
            if (var5 >= this.d) {
                break;
            }

            // 1 ����·������ ��·��������ĵ�һ��������ǰ�ƶ�����
            // ·������ b--
            PathPoint var6 = this.a.c();
            var6.i = true;

            // 2 ��һ��·���㵽 ·��Ŀ�ĵ� �ľ��� < 1 ����Ϊ�ɴ
            var4.stream().filter((var2x) -> {
                return var6.c(var2x) <= (float)var3;
            }).forEach(PathDestination::e);
            // ���������һ�� ·��Ŀ�ĵ� �ɴ�򷵻�
            if (var4.stream().anyMatch(PathDestination::f)) {
                break;
            }

            // 3 ��һ��·���㵽 Y��Ŀ�ĵ� �ľ���< �ɼ�����
            // ��Y��Ŀ�ĵ�����������ֱ�����ɼ��߽�
            if (var6.a(var0) < var2) {
                // ����һ��·���� ��Χ����Ч·���� ��ӵ� this.C,��������Ч·�������
                int var7 = this.e.a(this.c, var6);

                // ���� ��Χ��Ч·���㣬������Щ·����ı�Ҫ���ݣ�
                for(int var8 = 0; var8 < var7; ++var8) {
                    PathPoint var9 = this.c[var8];
                    // ��һ��·���㵽 ��Χ·����ľ���
                    float var10 = var6.a(var9);
                    var9.j = var6.j + var10;
                    float var11 = var6.e + var10 + var9.k;
                    if (var9.j < var2 && (!var9.c() || var11 < var9.e)) {
                        // ��Ҫ������ ���� ��Χ·����� ��һ��·�� Ϊ��ǰ·����
                        var9.h = var6;
                        var9.e = var11;
                        // ��Ҫ������ ���� ���� ·��Ŀ�ĵ� �ıؾ�֮· Ϊ��Χ·���㣡
                        var9.f = this.a(var9, var4) * 1.5F;
                        if (var9.c()) {
                            this.a.a(var9, var9.e + var9.f);
                        } else {
                            var9.g = var9.e + var9.f;
                            // 4 ����Χ·���� ��ӵ� ·������
                            this.a.a(var9);
                        }
                    }
                }
            }
        }

        Stream var6;
        // ����֮�����������һ�� ·��Ŀ�ĵ� �ɴ
        if (var4.stream().anyMatch(PathDestination::f)) {
            var6 = var4.stream().filter(PathDestination::f).map((var1x) -> {
                // ͨ�� Ŀ�ĵ�·�� ��Ŀ��λ�ã�����·��ʵ��
                return this.a(var1x.d(), (BlockPosition)var1.get(var1x), true);
            }).sorted(Comparator.comparingInt(PathEntity::e));
        } else {
            var6 = var4.stream().map((var1x) -> {
                // û�пɴ�·����Ҳ����·��ʵ�壬����ǲ��ɴ�
                return this.a(var1x.d(), (BlockPosition)var1.get(var1x), false);
            }).sorted(Comparator.comparingDouble(PathEntity::l).thenComparingInt(PathEntity::e));
        }
        // ���� ·�����б���̵�һ��
        Optional<PathEntity> var7 = var6.findFirst();
        if (!var7.isPresent()) {
            return null;
        } else {
            PathEntity var8 = (PathEntity)var7.get();
            return var8;
        }
    }

    /**
     * ��Ҫ��������
     * �� ·���� ����Ϊ ���� ·��Ŀ�ĵ� �ıؾ�·����
     * ���� ·���� ����� ·��Ŀ�ĵ� ����
     * @param var0 ·����
     * @param var1 ·��Ŀ�ĵ�
     * @return
     */
    private float a(PathPoint var0, Set var1) {
        float var2 = Float.MAX_VALUE;
        float var5;
        // ��ȡ����Y��Ŀ�ĵ������ ·��Ŀ�ĵ�
        for (Iterator var4 = var1.iterator(); var4.hasNext(); var2 = Math.min(var5, var2)) {
            PathDestination var3 = (PathDestination) var4.next();
            // Y��Ŀ�ĵ� �� ·��Ŀ�ĵ� �� ŷʽ����
            var5 = var0.a(var3);
            // ���� �ﵽ ·��Ŀ�ĵ� �ؾ�·����Ϊ var0
            // ��Ҫ
            var3.a(var5, var0);
        }
        return var2;
    }

    /**
     * ����·��ʵ�����
     * @param var0 ·����
     * @param var1 ·����Ŀ�λ��
     * @param var2
     * @return
     */
    private PathEntity a(PathPoint var0, BlockPosition var1, boolean var2) {
        List<PathPoint> var3 = Lists.newArrayList();
        PathPoint var4 = var0;
        var3.add(0, var0);
        // ������һ�� �ؾ�֮· ·����
        while(var4.h != null) {
            var4 = var4.h;
            // �ؾ�֮· ·���� �Ǵ��� Ŀ�ĵ� ����ĵ㿪ʼ�ģ����Դ˴�Ҫ��ǰ��ӣ�תΪ��ʵ�����
            var3.add(0, var4);
        }
        // ����·��ʵ�壬����һ��·�����б��Ŀ��λ��
        return new PathEntity(var3, var1, var2);
    }
}

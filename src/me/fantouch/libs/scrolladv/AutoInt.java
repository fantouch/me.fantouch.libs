package me.fantouch.libs.scrolladv;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 来回整数获取工具,当取值到达设定的极值时,继续获取下一个整数时,会往反方向获取
 * 
 * @author Fantouch
 */
class AutoInt {
    private boolean willAdd = true;
    private int min = 0, max = 0;
    private AtomicInteger auto;

    /**
     * 构造方法
     * 
     * @param min 最小值
     * @param max 最大值
     */
    public AutoInt(int min, int max) {
        this.min = min;
        this.max = max;
        this.auto = new AtomicInteger(min);
    }

    /**
     * 获取下一个整数
     * 
     * @return
     */
    public int get() {
        if (willAdd) {// 递增
            if (auto.get() == max) {// 到最大值了,下一次get将会递减
                willAdd = false;
                return auto.getAndDecrement();
            } else {// 没到最大值,照常递增
                return auto.getAndIncrement();
            }
        } else {// 递减
            if (auto.get() == min) {// 到最小值了,下一次get将会递增
                willAdd = true;
                return auto.getAndIncrement();
            } else {
                return auto.getAndDecrement();
            }
        }
    }

    /**
     * 重设当前值,并重新设定步进方向
     * 
     * @param current
     * @param willAdd
     */
    public void set(int current, boolean willAdd) {
        if (current < min) {
            auto.set(min);
            this.willAdd = true;
        } else if (current > max) {
            auto.set(max);
            this.willAdd = false;
        } else {
            auto.set(current);
            this.willAdd = willAdd;
        }

    }

    /**
     * 单元测试方法
     */
    public static void unitTest() {
        AutoInt auto = new AutoInt(0, 4);
        for (int i = 0; i < 20; i++) {
            System.out.println(auto.get());
        }

        System.out.println("\nset(-10,false):");
        auto.set(-10, false);
        for (int i = 0; i < 20; i++) {
            System.out.println(auto.get());
        }
        System.out.println("\nset(-10,true):");
        auto.set(-10, true);
        for (int i = 0; i < 20; i++) {
            System.out.println(auto.get());
        }

        System.out.println("\nset(10,false):");
        auto.set(10, false);
        for (int i = 0; i < 20; i++) {
            System.out.println(auto.get());
        }
        System.out.println("\nset(10,true):");
        auto.set(10, true);
        for (int i = 0; i < 20; i++) {
            System.out.println(auto.get());
        }

        System.out.println("\nset(0,false):");
        auto.set(0, false);
        for (int i = 0; i < 20; i++) {
            System.out.println(auto.get());
        }
        System.out.println("\nset(0,true):");
        auto.set(0, true);
        for (int i = 0; i < 20; i++) {
            System.out.println(auto.get());
        }

        System.out.println("\nset(4,false):");
        auto.set(4, false);
        for (int i = 0; i < 20; i++) {
            System.out.println(auto.get());
        }
        System.out.println("\nset(4,true):");
        auto.set(4, true);
        for (int i = 0; i < 20; i++) {
            System.out.println(auto.get());
        }

        System.out.println("\nset(2,false):");
        auto.set(2, false);
        for (int i = 0; i < 20; i++) {
            System.out.println(auto.get());
        }
        System.out.println("\nset(2,true):");
        auto.set(2, true);
        for (int i = 0; i < 20; i++) {
            System.out.println(auto.get());
        }
    }
}

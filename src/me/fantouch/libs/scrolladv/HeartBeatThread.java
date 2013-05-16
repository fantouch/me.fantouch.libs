package me.fantouch.libs.scrolladv;

import android.os.Handler;
import android.util.Log;

public class HeartBeatThread extends Thread {
    private boolean die = false;
    private int sleepDru;
    private Handler heartBeatHandler;

    public HeartBeatThread(int sleepDru, Handler heartBeatHandler) {
        this.sleepDru = sleepDru;
        this.heartBeatHandler = heartBeatHandler;
    }

    public void kill() {
        die = true;
    }

    @Override
    public void run() {
        while (!die) {
            // 用户手指离开时这里会被执行,先睡眠,可以避免一松手就切换页面的现象
            try {
                Thread.sleep(sleepDru);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!die) {// 如果睡眠过程没死,才发布心跳
                Log.w(HeartBeatThread.class.getSimpleName(), "beat~");
                heartBeatHandler.sendEmptyMessage(0);
            }
        }
        Log.w(HeartBeatThread.class.getSimpleName(), "Has dead !!");
    };

}

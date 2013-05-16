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
            Log.w(HeartBeatThread.class.getSimpleName(), "beat~");
            heartBeatHandler.sendEmptyMessage(0);
            try {
                Thread.sleep(sleepDru);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.w(HeartBeatThread.class.getSimpleName(), "Has dead !!");
    };

}

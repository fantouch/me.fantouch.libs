
package me.fantouch.libs.crash;

import android.app.Application;

public class MyApplication extends Application {
    private final String TAG = MyApplication.class.getSimpleName();
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        // CrashHandler crashHandler = CrashHandler.getInstance();
        // // 注册crashHandler
        // crashHandler.init(getApplicationContext());
        // // 发送以前没发送的报告(可选)
        // crashHandler.sendPreviousReportsToServer();
    }
}


package me.fantouch.libs.crash;

import android.app.Application;

import me.fantouch.libs.log.ELog;
import me.fantouch.libs.test.SendService;

public class MyApplication extends Application {
    private final static String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        ELog.setEnableLogCat(true);

        CrashHandler crashHandler = CrashHandler.getInstance();
        // 注册crashHandler
        crashHandler.init(getApplicationContext(), SendService.class);
        // 发送以前没发送的报告(可选)
        // crashHandler.sendPreviousReportsToServer();


    }

}

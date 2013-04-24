
package me.fantouch.libs.crash;

import android.app.Application;
import android.util.Log;

import me.fantouch.libs.log.Logg;

public class MyApplication extends Application {
    private final static String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();

        // 注册crashHandler
        // CrashHandler.getInstance().init(getApplicationContext(), SendService.class);

        Logg.setEnableLogcat(true);
        Logg.setEnableLogToFile(true, getApplicationContext());
    }
}

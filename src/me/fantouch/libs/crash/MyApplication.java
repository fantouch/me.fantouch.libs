
package me.fantouch.libs.crash;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import me.fantouch.libs.reporter.AbsSendReportsService;

import java.io.File;
import java.util.Map;

public class MyApplication extends Application {
    private final static String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        // 注册crashHandler
        crashHandler.init(this, SendService.class);
        // 发送以前没发送的报告(可选)
        // crashHandler.sendPreviousReportsToServer();
    }

    public static class SendService extends AbsSendReportsService {
        @Override
        public void asyncSendReportsToServer(Map<String, File> crFiles) {
            Log.i(TAG, "sending");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "send finished");
                    SendService.this.stopSelf();
                }
            }, 5 * 1000);
        }
    }
}

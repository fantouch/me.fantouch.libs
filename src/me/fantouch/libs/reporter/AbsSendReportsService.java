
package me.fantouch.libs.reporter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * 后台发送崩溃报告服务,请不要忘记在AndroidManifest.xml里面注册
 * 
 * @author Fantouch
 */
public abstract class AbsSendReportsService extends Service {
    private static final String TAG = AbsSendReportsService.class.getSimpleName();
    public static final String CRASH_REPORT_FILE_NAMES = "CRASH_REPORT_FILE_NAMES";
    public static final String LOG_FILE_NAMES = "LOG_FILE_NAMES";
    private NotificationHelper notificationHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");

        if (notificationHelper == null) {
            notificationHelper = new NotificationHelper(this);
        }

        String[] crFileNames = intent.getStringArrayExtra(CRASH_REPORT_FILE_NAMES);

        if (crFileNames != null && crFileNames.length > 0) {
            TreeSet<String> sortedFiles = new TreeSet<String>();
            sortedFiles.addAll(Arrays.asList(crFileNames));
            Map<String, File> reportFiles = new HashMap<String, File>();
            for (String fileName : sortedFiles) {
                File crFile = new File(getFilesDir(), fileName);
                if (crFile.exists()) {
                    reportFiles.put(fileName, crFile);
                }
            }
            if (reportFiles.size() != 0) {
                asyncSendReportsToServer(reportFiles, notificationHelper);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 请自行实现异步发送文件到服务器<br>
     * 更新通知栏的发送进度{@link NotificationHelper#refreshProgress(float)}<br>
     * 发送完毕{@link NotificationHelper#onSendFinish(AbsSendReportsService)}
     * 
     * @param reportFiles
     * @param notificationHelper
     */
    public abstract void asyncSendReportsToServer(Map<String, File> reportFiles,
            NotificationHelper notificationHelper);
}

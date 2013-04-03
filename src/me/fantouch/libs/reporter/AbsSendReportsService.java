
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

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");

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
                asyncSendReportsToServer(reportFiles);
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
     * 请自行实现异步发送文件到服务器,发送完毕应调用{@link Service#stopSelf()}以停止服务
     * 
     * @param reportFiles 崩溃文件集合,Key:文件名,Value:文件对象
     */
    public abstract void asyncSendReportsToServer(Map<String, File> reportFiles);

    // ProgressBar pb = new ProgressBar(mContext, null,
    // android.R.attr.progressBarStyleInverse);
    // pb.setMax(100);
}

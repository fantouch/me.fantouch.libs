
package me.fantouch.libs.reporter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 后台发送崩溃报告服务,请不要忘记在AndroidManifest.xml里面注册
 * 
 * @author Fantouch
 */
public abstract class AbsSendReportsService extends Service {

    public static final String INTENT_BROADCAST = "me.fantouch.libs.reporter.INTENT_BROADCAST";
    public static final String INTENT_DIR = "me.fantouch.libs.reporter.INTENT_DIR";
    public static final String INTENT_EXTENSION = "me.fantouch.libs.reporter.INTENT_EXTENSION";

    private static final String NETWORK_TEST_HOST = "http://www.baidu.com";
    private static final String NETWORK_TEST_HOST_KEYWORD = "baidu.com";

    private static final String TAG = AbsSendReportsService.class.getSimpleName();
    private NotificationHelper notificationHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");

        final String dir = intent.getStringExtra(INTENT_DIR);
        final String extension = intent.getStringExtra(INTENT_EXTENSION);
        if (TextUtils.isEmpty(dir) || TextUtils.isEmpty(extension)) {
            Log.e(TAG, "INTENT_DIR or INTENT_EXTENSION can not be null");
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        isWifiAvailable(new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String t) {
                if (t != null && t.indexOf(NETWORK_TEST_HOST_KEYWORD) > -1) {
                    sendCrashReportsToServer(dir, extension);
                } else {
                    onFailure(null, null);
                }
            }

            @Override
            public void onFailure(Throwable t, String strMsg) {
                stopSelf();
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendCrashReportsToServer(String dir, final String extension) {
        String[] crFileNames = getCrashReportFiles(dir, extension);
        if (crFileNames == null || crFileNames.length == 0) {// 停止处理
            Log.w(TAG, "No file found to be send");
            stopSelf();
            return;
        }

        String[] crFilePaths = new String[crFileNames.length];
        for (int i = 0; i < crFileNames.length; i++) {
            crFilePaths[i] = dir + File.separator + crFileNames[i];
        }

        File zipFile = zipReports(extension, crFilePaths);
        if (zipFile != null && zipFile.exists() && zipFile.length() != 0) {
            sendZipReportsToServer(zipFile, new NotificationHelper(this));
        } else {
            Log.w(TAG, "zip fail");
        }
    }

    private File zipReports(String reportExtension, String[] crFileNames) {
        String deviceId = new DeviceUuidFactory(this).getDeviceUuid().toString();
        final String ZIP_PATH = getFilesDir().getAbsolutePath()
                + File.separator + deviceId + reportExtension + ".zip";
        ZipCompressor zipCompressor = new ZipCompressor(ZIP_PATH);
        zipCompressor.compress(crFileNames);
        return new File(ZIP_PATH);
    }

    private String[] getCrashReportFiles(String dir, final String extension) {
        File filesDir = new File(dir);
        // 实现FilenameFilter接口的类实例可用于过滤器文件名
        FilenameFilter filter = new FilenameFilter() {
            // 测试指定文件是否应该包含在某一文件列表中。
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(extension);
            }
        };
        // 返回一个字符串数组，这些字符串指定此抽象路径名表示的目录中满足指定过滤器的文件和目录
        return filesDir.list(filter);
    }

    private void isWifiAvailable(AjaxCallBack<String> wifiCheckResultCallBack) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            wifiCheckResultCallBack.onFailure(null, null);
        }
        FinalHttp fh = new FinalHttp();
        fh.get(NETWORK_TEST_HOST, wifiCheckResultCallBack);
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
     * 请自行实现异步发送文件到服务器
     * <p>
     * <li>操作通知栏(可选):<br>
     * 更新进度{@link NotificationHelper#refreshProgress(float)}<br>
     * 发送完毕{@link NotificationHelper#onSendFinish(AbsSendReportsService)}
     * 
     * @param reportFiles
     * @param notificationHelper
     */
    public abstract void sendZipReportsToServer(File reportsZip,
            NotificationHelper notificationHelper);
}

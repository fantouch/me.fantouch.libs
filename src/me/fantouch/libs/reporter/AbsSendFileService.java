
package me.fantouch.libs.reporter;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

/**
 * 后台发送文件服务,记得在AndroidManifest.xml里面注册
 * 
 * @author Fantouch
 */
public abstract class AbsSendFileService extends Service {
    public static final String INTENT_BROADCAST = "me.fantouch.libs.reporter.INTENT_BROADCAST";
    public static final String INTENT_DIR = "me.fantouch.libs.reporter.INTENT_DIR";
    public static final String INTENT_EXTENSION = "me.fantouch.libs.reporter.INTENT_EXTENSION";

    private static final String NETWORK_TEST_HOST = "http://www.baidu.com";
    private static final String NETWORK_TEST_HOST_KEYWORD = "baidu.com";

    private static final String TAG = AbsSendFileService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");
        int onStartCommandResult = super.onStartCommand(intent, flags, startId);

        final String reportsDir = intent.getStringExtra(INTENT_DIR);
        final String reportExtension = intent.getStringExtra(INTENT_EXTENSION);
        if (TextUtils.isEmpty(reportsDir) || TextUtils.isEmpty(reportExtension)) {
            Log.e(TAG, "INTENT_DIR or INTENT_EXTENSION can not be null");
            stopSelf();
            return onStartCommandResult;
        }

        AjaxCallBack<String> httpGetCallBack = new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String t) {// wifi可用
                if (t != null && t.indexOf(NETWORK_TEST_HOST_KEYWORD) > -1) {
                    File zipFile = zipReports(reportsDir, reportExtension);
                    if (zipFile != null) {
                        // 发送报告
                        sendToServer(zipFile, new NotificationHelper(
                                AbsSendFileService.this));
                    } else {
                        stopSelf();
                    }
                } else {
                    onFailure(null, 0, null);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorNum, String strMsg) {
                stopSelf();
            }
        };

        isWifiAvailable(httpGetCallBack);
        return onStartCommandResult;
    }

    private File zipReports(String reportsDir, final String reportExtension) {
        // 获得文件路径列表
        String[] crFilePaths = getReportFilePaths(reportsDir, reportExtension);
        if (crFilePaths == null || crFilePaths.length == 0) {
            stopSelf();
            return null;
        }

        // 压缩文件
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.MM.dd#hh.mm.ss");
        String time = timeFormat.format(new Date(System.currentTimeMillis()));

        String deviceId = new DeviceUuidFactory(this).getDeviceUuid().toString();
        final String ZIP_PATH = getFilesDir().getAbsolutePath() + File.separator
                + Build.BRAND + "."
                + Build.MODEL + "."
                + deviceId
                + reportExtension + "@" + time + ".zip";
        ZipCompressor zipCompressor = new ZipCompressor(ZIP_PATH);
        zipCompressor.compress(crFilePaths);
        File zipFile = new File(ZIP_PATH);

        if (zipFile == null || !zipFile.exists() || zipFile.length() == 0) {
            Log.w(TAG, "zip fail");
            stopSelf();
            return null;
        }

        return zipFile;
    }

    private String[] getReportFilePaths(String reportsDir, final String extension) {
        // 根据扩展名过滤得到目标文件名列表
        File filesDir = new File(reportsDir);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(extension);
            }
        };
        String[] fileNames = filesDir.list(filter);
        if (fileNames == null || fileNames.length == 0) {
            Log.w(TAG, "No file found to be send");
            stopSelf();
            return null;
        }

        // 生成目标文件路径
        String[] filePaths = new String[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            filePaths[i] = reportsDir + File.separator + fileNames[i];
        }
        return filePaths;
    }

    /**
     * @param httpGetCallBack wifi检查结果回调函数
     */
    private void isWifiAvailable(AjaxCallBack<String> httpGetCallBack) {
        // FIXME 忽略wifi验证
        // ConnectivityManager cm = (ConnectivityManager)
        // getSystemService(Context.CONNECTIVITY_SERVICE);
        // NetworkInfo info = cm.getActiveNetworkInfo();
        // if (info == null || !info.isConnected()) {
        // wifiCheckResultCallBack.onFailure(null, null);
        // }
        FinalHttp fh = new FinalHttp();
        fh.get(NETWORK_TEST_HOST, httpGetCallBack);
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
     * 无论发送成功还是失败,请记得调用{@link Service#stopSelf()}
     * <p>
     * <li>操作通知栏(可选):<br>
     * 更新进度{@link NotificationHelper#refreshProgress(float)}<br>
     * 发送完毕{@link NotificationHelper#onSendFinish(AbsSendFileService)}
     * 
     * @param reportFiles 你要发送的文件已经被打包成zip文件了
     * @param notification
     */
    public abstract void sendToServer(File reportsZip,
            NotificationHelper notification);
}

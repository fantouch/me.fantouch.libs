
package me.fantouch.libs.test;

import android.util.Log;

import me.fantouch.libs.reporter.AbsSendReportsService;
import me.fantouch.libs.reporter.NotificationHelper;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.io.File;
import java.io.FileNotFoundException;

public class SendService extends AbsSendReportsService {
    private static final String TAG = SendService.class.getSimpleName();

    @Override
    public void sendZipReportsToServer(File reportsZip, NotificationHelper notificationHelper) {

        AjaxParams params = new AjaxParams();
        try {
            params.put("reportsZip", reportsZip);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FinalHttp fh = new FinalHttp();
        fh.post("http://192.168.0.163:8888/upload.php", params, new AjaxCallBack<String>() {
            @Override
            public void onStart() {
                Log.i(TAG, "start");
            }

            @Override
            public void onSuccess(String t) {
                Log.i(TAG, t);
                stopSelf();
            }

            @Override
            public void onFailure(Throwable t, String strMsg) {
                Log.i(TAG, "send fail");
                stopSelf();
            }
        });
    }
}

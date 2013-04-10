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
        FinalHttp fh = new FinalHttp();
        AjaxParams params = new AjaxParams();
        try {
            params.put("testCrashZip", reportsZip);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        fh.post("http://192.168.0.111/server/upload.php", params,
                new AjaxCallBack<String>() {
            @Override
            public void onStart() {
                Log.i(TAG, "start post");
            }

            @Override
            public void onSuccess(String t) {
                Log.i(TAG, t);
            }
        });
    }
}

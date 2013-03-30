
package me.fantouch.libs.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import me.fantouch.libs.R;
import me.fantouch.libs.updatehelper.AbsUpdateInfoParser;
import me.fantouch.libs.updatehelper.UpdateHelper;
import me.fantouch.libs.updatehelper.UpdateInfoBean;
import me.fantouch.libs.updatehelper.UpdateListener.ForceUpdateListener;
import me.fantouch.libs.updatehelper.UpdateListener.NormalUpdateListener;

import org.json.JSONObject;

/**
 * 如果要运行此测试,请右击工程,取消勾选Is Library
 * <p>
 * To Run This Test,Please Follow:<br>
 * Right Click This Project,Properties->Android,uncheck "Is Library"
 * 
 * @author Fantouch
 */
public class TestUpdateHelperActivity extends Activity {
    private final static String URL_HOST = "http://wz.ue189.cn/";
    private final static String URL_CHK_UPDATE = URL_HOST
            + "http://wz.ue189.cn/android-version.action";

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_updatehelper);
        tv = (TextView) findViewById(R.id.updateHelperTextView);
    }

    public void onClick(View button) {
        /**
         * 根据实际情况解析更新信息
         */
        AbsUpdateInfoParser infoParser = new AbsUpdateInfoParser() {
            @Override
            public UpdateInfoBean parse(String info) {
                UpdateInfoBean infoBean = new UpdateInfoBean();
                try {
                    JSONObject infoJson = new JSONObject(info).getJSONObject("version");
                    infoBean.setVersionCode(infoJson.getString("build"));
                    infoBean.setVersionName(infoJson.getString("version"));
                    infoBean.setWhatsNew(infoJson.getString("content"));
                    infoBean.setDownUrl(URL_HOST + "filedownload?showname="
                            + infoJson.getString("build") + "&filename="
                            + infoJson.getString("path"));
                    // e.g
                    // http://wz.ue189.cn/filedownload?showname=1&filename=wzchannel_Beta1.0_build111.apk
                } catch (Exception e) {
                    e.printStackTrace();
                    infoBean = null;
                }
                return infoBean;
            }
        };

        /**
         * 普通检查更新结果监听器
         */
        NormalUpdateListener normalUpdateListener = new NormalUpdateListener() {
            @Override
            public void onCheckStart() {
                tv.append("开始检查...\n");
            }

            @Override
            public void onDownloadStart() {
                tv.append("开始下载...\n");
            }

            @Override
            public void onCheckFinish() {
                tv.append("没找到新版本.\n");
            }

            @Override
            public void onCancel() {
                tv.append("下次再说.\n");
            }
        };

        /**
         * 强制检查更新结果监听器
         */
        ForceUpdateListener forceUpdateListener = new ForceUpdateListener() {
            @Override
            public void onCheckStart() {
                tv.append("开始检查...\n");
            }

            @Override
            public void onDownloadStart() {
                tv.append("开始下载...\n");
            }

            @Override
            public void onCheckFinish() {
                tv.append("没找到新版本.\n");
            }

            @Override
            public void onDecline() {
                tv.append("拒绝更新,程序2秒后退出");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TestUpdateHelperActivity.this.finish();
                    }
                }, 2000);
            }
        };

        if (button.getId() == R.id.updateHelperNormalUpdateBtn) {
            normalUpdate(infoParser, normalUpdateListener);
        } else if (button.getId() == R.id.updateHelperForceUpdateBtn) {
            forceUpdate(infoParser, forceUpdateListener);
        }
    }

    private void normalUpdate(AbsUpdateInfoParser parser, NormalUpdateListener listener) {
        new UpdateHelper(this, parser, listener).check(URL_CHK_UPDATE);
    }

    private void forceUpdate(AbsUpdateInfoParser parser, ForceUpdateListener listener) {
        new UpdateHelper(this, parser, listener).check(URL_CHK_UPDATE);
    }
}

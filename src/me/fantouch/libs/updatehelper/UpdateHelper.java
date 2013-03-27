
package me.fantouch.libs.updatehelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import me.fantouch.libs.R;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import java.io.File;
import java.security.InvalidParameterException;

public class UpdateHelper {
    private final String TAG = UpdateHelper.class.getSimpleName();

    private boolean isForceUpdate;
    private Activity mActivity;
    private PackageHelper mPackageHelper;
    private AbsUpdateInfoParser mParser;
    private NotificationHelper mNotificationHelper;
    private UpdateListener mUpdateListener;

    /**
     * @param isForceUpdate 是否强制更新,true则{@link UpdateListener#onDecline()}能被回调,false则
     *            {@link UpdateListener#onCancel()}能被回调
     * @param activity
     * @param parser
     * @param listener
     */
    public UpdateHelper(boolean isForceUpdate, Activity activity, AbsUpdateInfoParser parser,
            UpdateListener listener) {
        if (activity == null) {
            throw new InvalidParameterException("Param Activity can not be null");
        }
        if (parser == null) {
            throw new InvalidParameterException("Param AbsUpdateInfoParser can not be null");
        }
        if (listener == null) {
            throw new InvalidParameterException("Param UpdateListener can not be null");
        }

        mParser = parser;
        mActivity = activity;
        mUpdateListener = listener;
        this.isForceUpdate = isForceUpdate;
        mPackageHelper = new PackageHelper(activity);
        mNotificationHelper = new NotificationHelper(activity, mPackageHelper);
    }

    /**
     * 从指定的URL,以HTTP GET 的方式获取更新信息
     * 
     * @param url
     */
    public void check(String url) {
        new FinalHttp().get(url, new AjaxCallBack<String>() {
            @Override
            public void onStart() {
                mUpdateListener.onStartCheck();
            }

            @Override
            public void onSuccess(String t) {
                UpdateInfoBean infoBean = mParser.parse(t);
                if (infoBean != null // 如果有新版本
                        && infoBean.getVersionCode() > mPackageHelper.getLocalVersionCode()) {
                    showDialog(infoBean);
                } else {// 没有新版本
                    mUpdateListener.onCheckFinish();
                }
            }

            @Override
            public void onFailure(Throwable t, String strMsg) {
                if (t != null) {
                    t.printStackTrace();
                }
                if (strMsg != null) {
                    Log.e(TAG, "Check Update Error:" + strMsg);
                }
                mUpdateListener.onCheckFinish();
            }
        });
    }

    private void showDialog(final UpdateInfoBean bean) {
        StringBuffer msg = new StringBuffer();
        msg.append("最新版本:\n" + bean.getVersionName() + "\n\n");
        msg.append("当前版本:\n" + mPackageHelper.getLocalVersionName() + "\n\n");
        msg.append("更新日志:\n" + bean.getWhatsNew());

        ScrollView scrollView = (ScrollView) View.inflate(mActivity, R.layout.updatehelper_dialog,
                null);
        TextView textView = (TextView) scrollView
                .findViewById(R.id.updatehelper_dialog_tv);
        textView.setText(msg);

        DialogInterface.OnClickListener positiveBtnLsnr = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mUpdateListener.onStartDownload();
                downApk(bean);
            }
        };
        DialogInterface.OnClickListener negativeBtnLsnr = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mUpdateListener.onDecline();
            }
        };
        DialogInterface.OnClickListener neutralBtnLsnr = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mUpdateListener.onCancel();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setView(scrollView);
        builder.setTitle("发现新版本");
        builder.setMessage("是否下载?");
        builder.setPositiveButton("下载", positiveBtnLsnr);
        if (isForceUpdate) {
            builder.setNegativeButton("不更新", negativeBtnLsnr);
        } else {
            builder.setNeutralButton("下次再说", neutralBtnLsnr);
        }
        builder.setCancelable(false);
        builder.create();
        builder.show();

        fixScrollViewHeight(scrollView);
    }

    /**
     * 修正Android2.x上,包含ScrollView的Dialog总是充满屏幕的问题
     * 
     * @param scrollView
     */
    private void fixScrollViewHeight(ScrollView scrollView) {
        int screenHeight = mActivity.getWindowManager().getDefaultDisplay().getHeight();
        LayoutParams lp = scrollView.getLayoutParams();
        lp.height = screenHeight / 3;
        scrollView.setLayoutParams(lp);
    }

    private void downApk(UpdateInfoBean bean) {
        if (SDCardHelper.hasSD()) {
            // e.g
            // http://wz.ue189.cn/filedownload?showname=1&filename=wzchannel_v2.9_build11_ue.apk
            String filePath = SDCardHelper.getSDPath() + File.separator
                    + mPackageHelper.getPackageName() + File.separator
                    + mPackageHelper.getPackageName() + "_" + bean.getVersionName() + "_"
                    + bean.getVersionCode() + ".apk";
            // 删除旧文件
            File oldFile = new File(filePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            new FinalHttp().download(bean.getDownUrl(), filePath, new AjaxCallBack<File>() {
                @Override
                public void onLoading(long count, long current) {
                    float percent = (float) current / count * 100;
                    refreshProgress(percent);
                }

                @Override
                public void onSuccess(File file) {
                    mNotificationHelper.getNotificationManager().notify(1,
                            mNotificationHelper.getDownFinishedNotification(file));
                    Toast.makeText(mActivity, "下载完成", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Throwable t, String strMsg) {
                    if (t != null || strMsg != null) {
                        StringBuffer sb = new StringBuffer("下载失败");
                        if (strMsg != null) {
                            sb.append(strMsg);
                            Toast.makeText(mActivity, sb, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "downApk$onFailure() : " + strMsg);
                        }

                        if (t != null) {
                            t.printStackTrace();
                        }
                    }
                }
            });
        } else {
            Toast.makeText(mActivity, "SD卡不可用,无法下载", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshProgress(final float percent) {
        mNotificationHelper.getNotificationManager().notify(1,
                mNotificationHelper.getDownProgressNotification());
        mNotificationHelper.getRemoteViews().setProgressBar(
                R.id.updatehelper_notification_progress_pb, 100, (int) percent, false);
        mNotificationHelper.getRemoteViews()
                .setTextViewText(R.id.updatehelper_notification_progress_tv,
                        String.format("%.1f", percent));
    }

}

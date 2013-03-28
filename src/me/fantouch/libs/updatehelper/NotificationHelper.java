
package me.fantouch.libs.updatehelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import me.fantouch.libs.R;

import java.io.File;

class NotificationHelper {

    private Context mContext;
    private RemoteViews mRemoteViews;
    private Notification mDownProgrNotif;
    private PackageHelper mPackageHelper;
    private NotificationManager mContextNotificationManager;

    public NotificationHelper(Context ctx, PackageHelper packageHelper) {
        mContext = ctx;
        mPackageHelper = packageHelper;
        mContextNotificationManager = (NotificationManager) ctx
                .getSystemService(Context.NOTIFICATION_SERVICE);
        initDownProgrNotif();
    }

    private void initDownProgrNotif() {
        mDownProgrNotif = new Notification();
        mDownProgrNotif.icon = android.R.drawable.stat_sys_download;
        mDownProgrNotif.flags |= Notification.FLAG_AUTO_CANCEL;

        mRemoteViews = new RemoteViews(mPackageHelper.getPackageName(),
                R.layout.updatehelper_notification_progress);
        mRemoteViews.setImageViewResource(R.id.updatehelper_notification_progress_icon,
                mPackageHelper.getAppIcon());

        mDownProgrNotif.contentView = mRemoteViews;
        mDownProgrNotif.contentIntent = PendingIntent.getService(mContext, 0, new Intent(), 0);
    }

    private Notification getDownFinishedNotification(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification noti = new Notification();
        noti.setLatestEventInfo(mContext, mPackageHelper.getAppName(), "下载完成,点击安装", pendingIntent);
        noti.icon = android.R.drawable.stat_sys_download_done;
        noti.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND
                | Notification.DEFAULT_LIGHTS;
        return noti;
    }

    /**
     * 更新下载进度
     * 
     * @param percent
     */
    public void refreshProgress(final float percent) {
        mContextNotificationManager.notify(1, mDownProgrNotif);
        mRemoteViews.setProgressBar(R.id.updatehelper_notification_progress_pb, 100,
                (int) percent, false);
        mRemoteViews.setTextViewText(R.id.updatehelper_notification_progress_tv,
                String.format("%.1f", percent));
    }

    /**
     * 通知用户下载已经完成
     * 
     * @param file
     */
    public void notifyDownloadFinish(File file) {
        mContextNotificationManager.notify(1,
                getDownFinishedNotification(file));
        Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show();
    }
}

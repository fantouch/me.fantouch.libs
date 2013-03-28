
package me.fantouch.libs.updatehelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

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
    }

    public Notification getDownProgressNotification() {
        if (mDownProgrNotif == null) {
            mDownProgrNotif = new Notification();
            mDownProgrNotif.icon = android.R.drawable.stat_sys_download;
            mDownProgrNotif.flags |= Notification.FLAG_AUTO_CANCEL;

            mRemoteViews = new RemoteViews(mPackageHelper.getPackageName(),
                    R.layout.updatehelper_notification_progress);
            mRemoteViews.setImageViewResource(R.id.updatehelper_notification_progress_icon, mPackageHelper.getAppIcon());

            mDownProgrNotif.contentView = mRemoteViews;

            PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, new Intent(), 0);
            mDownProgrNotif.contentIntent = pendingIntent;
        }
        return mDownProgrNotif;
    }

    public Notification getDownFinishedNotification(File file) {
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

    public NotificationManager getNotificationManager() {
        return mContextNotificationManager;
    }

    public RemoteViews getRemoteViews() {
        return mRemoteViews;
    }

}

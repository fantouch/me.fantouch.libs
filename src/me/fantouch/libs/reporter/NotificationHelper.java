
package me.fantouch.libs.reporter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import me.fantouch.libs.R;

class NotificationHelper {

    private Context mContext;
    private RemoteViews mRemoteViews;
    private Notification mProgrNotif;
    private PackageHelper mPackageHelper;
    private NotificationManager mContextNotificationManager;

    public NotificationHelper(Context ctx, PackageHelper packageHelper) {
        mContext = ctx;
        mPackageHelper = packageHelper;
        mContextNotificationManager = (NotificationManager) ctx
                .getSystemService(Context.NOTIFICATION_SERVICE);
        initProgrNotif();
    }

    private void initProgrNotif() {
        mProgrNotif = new Notification();
        mProgrNotif.icon = android.R.drawable.stat_sys_download;
        mProgrNotif.flags |= Notification.FLAG_AUTO_CANCEL;

        mRemoteViews = new RemoteViews(mPackageHelper.getPackageName(),
                R.layout.reporter_notification_progress);
        mRemoteViews.setImageViewResource(R.id.reporter_notification_progress_icon,
                mPackageHelper.getAppIcon());

        mProgrNotif.contentView = mRemoteViews;
        mProgrNotif.contentIntent = PendingIntent.getService(mContext, 0, new Intent(), 0);
    }

    /**
     * 更新进度
     * 
     * @param percent
     */
    public void refreshProgress(final float percent) {
        mContextNotificationManager.notify(1, mProgrNotif);
        mRemoteViews.setProgressBar(R.id.reporter_notification_progress_pb, 100,
                (int) percent, false);
        mRemoteViews.setTextViewText(R.id.reporter_notification_progress_tv,
                String.format("%.1f", percent));
    }

    public void reportFinishedCancelNotif() {
        mContextNotificationManager.cancel(1);
        Toast.makeText(mContext, "发送完毕,谢谢", Toast.LENGTH_SHORT).show();
    }
}

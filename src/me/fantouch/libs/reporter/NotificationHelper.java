
package me.fantouch.libs.reporter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import me.fantouch.libs.R;

public class NotificationHelper {

    private Context mContext;
    private RemoteViews mRemoteViews;
    private Notification mProgrNotif;
    private PackageHelper mPackageHelper;
    private NotificationManager mNotificationManager;

    public NotificationHelper(Context ctx) {
        mContext = ctx;
        mPackageHelper = new PackageHelper(ctx);
        mNotificationManager = (NotificationManager) ctx
                .getSystemService(Context.NOTIFICATION_SERVICE);
        initProgrNotif();
    }

    private void initProgrNotif() {
        mRemoteViews = new RemoteViews(mPackageHelper.getPackageName(),
                R.layout.reporter_notification_progress);
        mRemoteViews.setImageViewResource(R.id.reporter_notification_progress_icon,
                mPackageHelper.getAppIcon());

        mProgrNotif = new Notification();
        mProgrNotif.contentView = mRemoteViews;
        mProgrNotif.flags |= Notification.FLAG_AUTO_CANCEL;
        mProgrNotif.icon = android.R.drawable.stat_sys_download;
        mProgrNotif.contentIntent = PendingIntent.getService(mContext, 0, new Intent(), 0);
    }

    /**
     * 更新进度
     * 
     * @param percent 百分比0~1
     */
    public void refreshProgress(final float percent) {
        mNotificationManager.notify(1, mProgrNotif);
        mRemoteViews.setProgressBar(R.id.reporter_notification_progress_pb, 100,
                (int) percent, false);
        mRemoteViews.setTextViewText(R.id.reporter_notification_progress_tv,
                String.format("%.1f", percent));
    }

    /**
     * 移除发送进度通知,停止发送服务,并弹出以下Toast提示
     * 
     * <pre>
     * Toast.makeText(mContext, &quot;发送完毕&quot;, Toast.LENGTH_SHORT).show();
     * </pre>
     */
    public void onSendFinish(AbsSendFileService service) {
        mNotificationManager.cancel(1);
        service.stopSelf();
        Toast.makeText(mContext, "发送完毕", Toast.LENGTH_SHORT).show();
    }
}


package me.fantouch.libs.updatehelper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class PackageHelper {
    private PackageInfo info = null;
    private PackageManager pm;

    public PackageHelper(Context context) {
        pm=context.getPackageManager();
        try {
            info = pm.getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getLocalVersionCode() {
        return info != null ? info.versionCode : Integer.MAX_VALUE;
    }

    public String getLocalVersionName() {
        return info != null ? info.versionName : "";
    }
    public String getAppName() {
        return info != null ? (String) info.applicationInfo.loadLabel(pm) : "";
    }

    public String getPackageName() {
        return info != null ? info.packageName : "";
    }

    public int getAppIcon() {
        return info != null ? info.applicationInfo.icon
                : android.R.drawable.ic_dialog_info;
    }

}

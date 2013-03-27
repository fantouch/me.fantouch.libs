
package me.fantouch.libs.updatehelper;

import android.os.Environment;

public class SDCardHelper {

    public static boolean hasSD() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static String getSDPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

}

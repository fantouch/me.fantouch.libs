
package me.fantouch.libs.log;

import android.util.Log;

/**
 * (Logs=>Log with switch)带开关的Log工具,方便发布时一键取消Log.如果总是需要Log信息,则请使用系统自带的{@link Log}
 * 
 * @author Fantouch
 */
public class Logs extends BasicLog {

    public static void d(String TAG, String msg) {
        if (ENABLE_LOGCAT) {
            Log.d(TAG, msg);
        }
    };

    public static void i(String TAG, String msg) {
        if (ENABLE_LOGCAT) {
            Log.i(TAG, msg);
        }
    };

    public static void e(String TAG, String msg) {
        if (ENABLE_LOGCAT) {
            Log.e(TAG, msg);
        }
    };

    public static void w(String TAG, String msg) {
        if (ENABLE_LOGCAT) {
            Log.w(TAG, msg);
        }
    };

    public static void v(String TAG, String msg) {
        if (ENABLE_LOGCAT) {
            Log.v(TAG, msg);
        }
    };
}

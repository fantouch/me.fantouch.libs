
package me.fantouch.libs.log;

import android.util.Log;

/**
 * (SwitchLog=>SLog)带开关的Log工具,方便发布时一键取消Log.如果总是需要Log信息,则请使用系统自带的{@link Log}
 * 
 * @author Fantouch
 */
public class SLog {
    public static void logWithLineNum(String tag, String msg) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        int lineNum = elements[2].getLineNumber();
        Log.v(tag, lineNum + ": " + msg);
    }

    private static boolean DEBUG = true;

    /**
     * 开启调试输出
     */
    public static void enable() {
        DEBUG = true;
    }

    /**
     * 关闭调试输出
     */
    public static void disable() {
        DEBUG = false;
    }

    public static boolean isEnable() {
        return DEBUG;
    }

    public static void d(String TAG, String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    };

    public static void i(String TAG, String msg) {
        if (DEBUG) {
            Log.i(TAG, msg);
        }
    };

    public static void e(String TAG, String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    };

    public static void w(String TAG, String msg) {
        if (DEBUG) {
            Log.w(TAG, msg);
        }
    };

    public static void v(String TAG, String msg) {
        if (DEBUG) {
            Log.v(TAG, msg);
        }
    };

    public static void d(String msg) {
        if (DEBUG) {
            Log.d("", msg);
        }
    };

    public static void i(String msg) {
        if (DEBUG) {
            Log.i("", msg);
        }
    };

    public static void e(String msg) {
        if (DEBUG) {
            Log.e("", msg);
        }
    };

    public static void v(String msg) {
        if (DEBUG) {
            Log.v("", msg);
        }
    };

    public static void w(String msg) {
        if (DEBUG) {
            Log.w("", msg);
        }
    };
}

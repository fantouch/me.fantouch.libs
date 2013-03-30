
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

    private static boolean ENABLE = true;

    /**
     * 开启调试输出
     */
    public static void enable() {
        ENABLE = true;
    }

    /**
     * 关闭调试输出
     */
    public static void disable() {
        ENABLE = false;
    }

    public static boolean isEnable() {
        return ENABLE;
    }

    public static void d(String TAG, String msg) {
        if (ENABLE) {
            Log.d(TAG, msg);
        }
    };

    public static void i(String TAG, String msg) {
        if (ENABLE) {
            Log.i(TAG, msg);
        }
    };

    public static void e(String TAG, String msg) {
        if (ENABLE) {
            Log.e(TAG, msg);
        }
    };

    public static void w(String TAG, String msg) {
        if (ENABLE) {
            Log.w(TAG, msg);
        }
    };

    public static void v(String TAG, String msg) {
        if (ENABLE) {
            Log.v(TAG, msg);
        }
    };

    public static void d(String msg) {
        if (ENABLE) {
            Log.d("", msg);
        }
    };

    public static void i(String msg) {
        if (ENABLE) {
            Log.i("", msg);
        }
    };

    public static void e(String msg) {
        if (ENABLE) {
            Log.e("", msg);
        }
    };

    public static void v(String msg) {
        if (ENABLE) {
            Log.v("", msg);
        }
    };

    public static void w(String msg) {
        if (ENABLE) {
            Log.w("", msg);
        }
    };
}

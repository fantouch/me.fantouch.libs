
package me.fantouch.libs.log;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

class BasicLog {
    private static final String TAG = BasicLog.class.getSimpleName();
    /**
     * 是否启用Log
     */
    static boolean ENABLE_LOGCAT = false;
    /**
     * 是否把Log也输出到文件
     */
    static boolean TO_FILE = false;
    /**
     * 保存Log信息的文件路径
     */
    static String LOG_FILE_PATH = "";
    /**
     * 记录到文件的Log信息的日期前缀
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss  ");

    /**
     * 开启或关闭控制台输出,建议在Application里面设置,安全起见,默认是false.
     */
    public static void setEnableLogCat(boolean enable) {
        if (enable) {
            if (!ENABLE_LOGCAT) {
                ENABLE_LOGCAT = true;
                Log.i(TAG, "Now Enabled");
            } else {
                Log.i(TAG, "Log Already Enabled ");
            }
        } else {
            if (ENABLE_LOGCAT) {
                ENABLE_LOGCAT = false;
                Log.i(TAG, "Now Disabled");
            } else {
                Log.i(TAG, "Log Already Disabled");
            }
        }
    }

    /**
     * 查询是否启用控制台输出
     * 
     * @return
     */
    public static boolean isLogcatEnable() {
        return ENABLE_LOGCAT;
    }



    /**
     * 启用保存Log到文件功能,文件所在目录/mnt/sdcard/packageName/
     * 
     * @param ctx
     */
    public static void setEnableLogToFile(boolean enable, Context ctx) {
        if (enable) {
            if (!TO_FILE) {// 启用保存到文件
                TO_FILE = true;
                LOG_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + ctx.getPackageName();
                Log.i(TAG, "Will Save Log To File");
            } else {
                Log.i(TAG, "Save To File Already Enable");
            }
        } else {
            if (TO_FILE) {
                TO_FILE = false;
                Log.i(TAG, "Will NOT Save Log To File");
            } else {
                Log.i(TAG, "Save To File Already Disable");
            }
        }
    }

    /**
     * 查询当前是否已启用保存Log到文件的功能
     * 
     * @return
     */
    public static boolean isSaveToFileEnable() {
        return TO_FILE;
    }

    public static void logToFile(String msg) {
        File file = new File(LOG_FILE_PATH, "log");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String time = dateFormat.format(new Date(System.currentTimeMillis()));

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file.getAbsolutePath(), true));
            out.append(time);
            out.append(msg);
            out.append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

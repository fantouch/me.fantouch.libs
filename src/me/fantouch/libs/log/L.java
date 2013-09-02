
package me.fantouch.libs.log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.fantouch.libs.reporter.AbsSendFileService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

/**
 * 可以记录行号,类名,方法名,可是实现双击转跳的Android Log增强工具<br>
 * 你只需要填入想输出的内容,繁琐的东西自动帮你补全
 * <p>
 * 性能方面,耗时是{@link Log}的20倍
 * <p>
 * App发布前记得关闭输出 {@link L#disable()}
 * <p>
 * 示例:<br>
 * 
 * <pre>
 * L.d(&quot;Hello World&tilde;&quot;);<p>
 * LogCat输出:
 *  07-25 17:57:15.395: D/MainMenuActivity(17542): Hello~~ =>at me.fantouch.demo.MainMenuActivity.onCreate(MainMenuActivity.java:14)
 * <p>
 * 
 * 效果:
 * 1.自动用类名填充TAG
 * 2.自动填充方法名
 * 3.自动填充文件名,行号
 * 4.Eclipse里面双击Logcat的输出,能转跳到Java文件相应行
 * </pre>
 * <p>
 * 
 * @author Fantouch
 */
public class L {
    private static final String TAG = L.class.getSimpleName();
    /**
     * 日志文件扩展名
     */
    private static final String LOG_FILE_EXTENSION = ".log";
    /**
     * 是否启用Log
     */
    private static boolean ENABLE_LOGCAT = false;
    /**
     * 是否把Log也输出到文件
     */
    private static boolean TO_FILE = false;
    /**
     * 保存Log信息的文件路径
     */
    private static String LOG_FILE_PATH = "";
    /**
     * 记录到文件的Log信息的日期前缀
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm:ss ");

    /**
     * 是否把日志输出到Logcat,建议在Application里面设置
     * 
     * @param enable 默认true
     */
    public static void setEnableLogcat(boolean enable) {
        if (enable) {
            if (!ENABLE_LOGCAT) {
                ENABLE_LOGCAT = true;
                Log.i(TAG, "Logcat Enabled");
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
     * 查询当前是否把日志输出到Logcat
     * 
     * @return 是否已启用
     */
    public static boolean isLogcatEnable() {
        return ENABLE_LOGCAT;
    }

    /**
     * 设置是否保存日志到文件,文件所在目录/data/data/packageName/
     * 
     * @param enable 是否启用,出于安全和性能考虑,默认false
     * @param ctx
     */
    public static void setEnableLogToFile(boolean enable, Context ctx) {
        if (enable) {
            if (!TO_FILE) {// 启用保存到文件
                TO_FILE = true;
                LOG_FILE_PATH = ctx.getFilesDir().getAbsolutePath();
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
     * 查询是否有启用保存日志到文件
     * 
     * @return 是否已启用
     */
    public static boolean isSaveToFileEnable() {
        return TO_FILE;
    }

    /**
     * 记录日志到文件
     * 
     * @param msg 需要记录的日志信息
     */
    public static void save(String msg) {
        File file = getLogFile();
        if (file == null)
            return;

        // 时间应当在msg构造时就生成的,但是我们没这么高的精确度要求.
        String time = TIME_FORMAT.format(new Date(System.currentTimeMillis()));
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
                    out = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static File getLogFile() {
        File file = new File(LOG_FILE_PATH,
                DATE_FORMAT.format(new Date(System.currentTimeMillis())) + LOG_FILE_EXTENSION);
        if (!file.exists()) {
            try {
                new File(LOG_FILE_PATH).mkdirs();
                file.createNewFile();
                return file;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return file;
        }
    }

    private static File getCfgFile() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "log.cfg");
        if (!file.exists()) {
            try {
                new File(LOG_FILE_PATH).mkdirs();
                file.createNewFile();
                return file;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return file;
        }
    }

    // 发送日志文件到服务器
    public static void uploadLogFiles(Context ctx,
            Class<? extends AbsSendFileService> sendService) {
        Intent intent = new Intent(ctx, sendService);
        intent.putExtra(AbsSendFileService.INTENT_DIR, LOG_FILE_PATH);
        intent.putExtra(AbsSendFileService.INTENT_EXTENSION, LOG_FILE_EXTENSION);
        ctx.startService(intent);
    }

    private L() {
    }

    /**
     * 根据配置文件决定Log行为
     * 文件示例如下<br>
     * 
     * <pre>
     * /sdcard/log.cfg
     * </pre>
     * 
     * <pre>
     * logcat=true
     * file=true
     * </pre>
     */
    public static void initFromCfgFile(Context ctx) {
        File cfgFile = getCfgFile();
        if (cfgFile == null)
            return;
        BufferedReader in = null;
        try {
            FileReader fr = new FileReader(cfgFile);
            in = new BufferedReader(fr);
            String line;
            while ((line = in.readLine()) != null) {

                if (line.contains("logcat")) {
                    if (line.split("=")[1].equals("true")) {
                        setEnableLogcat(true);
                    } else if (line.split("=")[1].equals("false")) {
                        setEnableLogcat(false);
                    } else {
                        Log.w(TAG, "wrong cfg file");
                    }
                }

                if (line.contains("file")) {
                    if (line.split("=")[1].equals("true")) {
                        setEnableLogToFile(true, ctx);
                    } else if (line.split("=")[1].equals("false")) {
                        setEnableLogToFile(false, ctx);
                    } else {
                        Log.w(TAG, "wrong cfg file");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void v(String message) {
        if (isLogcatEnable() || isSaveToFileEnable()) {

            StackTraceElement element = Thread.currentThread().getStackTrace()[3];
            String tag = getTag(element);
            String logcatTextTail = getLogcatTextTail(element);

            if (isLogcatEnable())
                Log.v(tag, message + logcatTextTail);
            if (isSaveToFileEnable())
                save(tag + ":" + message + logcatTextTail);
        }
    }

    public static void i(String message) {
        if (isLogcatEnable() || isSaveToFileEnable()) {

            StackTraceElement element = Thread.currentThread().getStackTrace()[3];
            String tag = getTag(element);
            String logcatTextTail = getLogcatTextTail(element);

            if (isLogcatEnable())
                Log.i(tag, message + logcatTextTail);
            if (isSaveToFileEnable())
                save(tag + ":" + message + logcatTextTail);
        }
    }

    public static void d(String message) {
        if (isLogcatEnable() || isSaveToFileEnable()) {

            StackTraceElement element = Thread.currentThread().getStackTrace()[3];
            String tag = getTag(element);
            String logcatTextTail = getLogcatTextTail(element);

            if (isLogcatEnable())
                Log.d(tag, message + logcatTextTail);
            if (isSaveToFileEnable())
                save(tag + ":" + message + logcatTextTail);
        }
    }

    public static void w(String message) {
        if (isLogcatEnable() || isSaveToFileEnable()) {

            StackTraceElement element = Thread.currentThread().getStackTrace()[3];
            String tag = getTag(element);
            String logcatTextTail = getLogcatTextTail(element);

            if (isLogcatEnable())
                Log.w(tag, message + logcatTextTail);
            if (isSaveToFileEnable())
                save(tag + ":" + message + logcatTextTail);
        }
    }

    public static void e(String message) {
        if (isLogcatEnable() || isSaveToFileEnable()) {

            StackTraceElement element = Thread.currentThread().getStackTrace()[3];
            String tag = getTag(element);
            String logcatTextTail = getLogcatTextTail(element);

            if (isLogcatEnable())
                Log.e(tag, message + logcatTextTail);
            if (isSaveToFileEnable())
                save(tag + ":" + message + logcatTextTail);
        }
    }

    private static String getTag(StackTraceElement stackTraceElement) {
        return stackTraceElement.getClassName().substring(
                stackTraceElement.getClassName().lastIndexOf(".") + 1);
    }

    private static String getLogcatTextTail(StackTraceElement stackTraceElement) {
        return " => at "
                + stackTraceElement.getClassName()
                + "."
                + stackTraceElement.getMethodName()
                + "("
                + stackTraceElement.getFileName()
                + ":"
                + stackTraceElement.getLineNumber()
                + ")";
    }

}

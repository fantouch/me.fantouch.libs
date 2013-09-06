
package me.fantouch.libs.log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.fantouch.libs.reporter.AbsSendFileService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * 可以记录行号,类名,方法名,可是实现双击转跳的Android Log增强工具<br>
 * 你只需要填入想输出的内容,繁琐的东西自动帮你补全<br>
 * 1.自动用类名.方法名填充TAG<br>
 * 2.自动填充文件名,行号<br>
 * 3.Eclipse里面双击Logcat的输出,能转跳到Java文件相应行<br>
 * <p>
 * 性能方面,耗时是{@link Log}的20倍
 * <p>
 * 使用示例:
 * 
 * <pre>
 * L.i(&quot;闪电狗&quot;);
 * 
 * LogCat输出:
 * 09-06 16:21:24.262: I/TestLogActivity$2.onClick(3212): 闪电狗
 * 09-06 16:21:24.262: I/TestLogActivity$2.onClick(3212):  at me.fantouch.demo.TestLogActivity$2.onClick(TestLogActivity.java:48)
 * </pre>
 * <p>
 * 
 * @author Fantouch
 */
public class L {
    private static final String TAG = L.class.getSimpleName();

    /**
     * 禁止实例化
     */
    private L() {
    }

    public static void v() {
        log(Log.VERBOSE, null, null);
    }

    public static void v(String msg) {
        log(Log.VERBOSE, msg, null);
    }

    public static void v(Throwable throwable) {
        log(Log.VERBOSE, null, throwable);
    }

    public static void v(String msg, Throwable throwable) {
        log(Log.VERBOSE, msg, throwable);
    }

    public static void i() {
        log(Log.INFO, null, null);
    }

    public static void i(String msg) {
        log(Log.INFO, msg, null);
    }

    public static void i(Throwable throwable) {
        log(Log.INFO, null, throwable);
    }

    public static void i(String msg, Throwable throwable) {
        log(Log.INFO, msg, throwable);
    }

    public static void d() {
        log(Log.DEBUG, null, null);
    }

    public static void d(String msg) {
        log(Log.DEBUG, msg, null);
    }

    public static void d(Throwable throwable) {
        log(Log.DEBUG, null, throwable);
    }

    public static void d(String msg, Throwable throwable) {
        log(Log.DEBUG, msg, throwable);
    }

    public static void w() {
        log(Log.WARN, null, null);
    }

    public static void w(String msg) {
        log(Log.WARN, msg, null);
    }

    public static void w(Throwable throwable) {
        log(Log.WARN, null, throwable);
    }

    public static void w(String msg, Throwable throwable) {
        log(Log.WARN, msg, throwable);
    }

    public static void e() {
        log(Log.ERROR, null, null);
    }

    public static void e(String msg) {
        log(Log.ERROR, msg, null);
    }

    public static void e(Throwable throwable) {
        log(Log.ERROR, null, throwable);
    }

    public static void e(String msg, Throwable throwable) {
        log(Log.ERROR, msg, throwable);
    }

    private static void log(int logLevel, String msg, Throwable throwable) {

        if (isLogcatEnabled() || isLogToFileEnabled()) {

            StackTraceElement element = Thread.currentThread().getStackTrace()[4];
            String tag = getTag(element);
            String codeLocation = getCodeLocation(element);

            if (msg == null && throwable == null) {
                msg = "\t" + codeLocation;
            } else if (msg == null && throwable != null) {
                msg = "";
            } else if (msg != null && throwable == null) {
                msg = msg + "\n\t" + codeLocation;
            } else if (msg != null && throwable != null) {
                // Nothing
            }

            if (isLogcatEnabled()) {
                switch (logLevel) {
                    case Log.VERBOSE:
                        Log.v(tag, msg, throwable);
                        break;

                    case Log.INFO:
                        Log.i(tag, msg, throwable);
                        break;

                    case Log.DEBUG:
                        Log.d(tag, msg, throwable);
                        break;

                    case Log.WARN:
                        Log.w(tag, msg, throwable);
                        break;

                    case Log.ERROR:
                        Log.e(tag, msg, throwable);
                        break;

                    default:
                        break;
                }
            }

            if (isLogToFileEnabled()) {
                String time = DATE_TIME_FORMAT.format(new Date(System.currentTimeMillis()));
                writeFile(time, tag, msg + "\n" + Log.getStackTraceString(throwable));
            }

        }

    }

    private static String getTag(StackTraceElement stackTraceElement) {
        return stackTraceElement.getClassName().substring(
                stackTraceElement.getClassName().lastIndexOf(".") + 1) + "."
                + stackTraceElement.getMethodName();
    }

    private static String getCodeLocation(StackTraceElement stackTraceElement) {
        return "at "
                + stackTraceElement.getClassName()
                + "."
                + stackTraceElement.getMethodName()
                + "("
                + stackTraceElement.getFileName()
                + ":"
                + stackTraceElement.getLineNumber()
                + ")";
    }

    private static final String LOG_FILE_EXTENSION = ".log";

    private static boolean isToLogcat = false;

    private static boolean isToFile = false;

    /**
     * log文件路径
     */
    private static String filePath = "";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm:ss ");

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(
            "[yyyy-MM-dd hh:mm:ss] ");
    private static final String DATE_TIME_PLACEHOLDER = "                      ";
    /**
     * 设置是否把日志输出到Logcat,建议在Application里面设置
     * 
     * @param enable 缺省false
     */
    public static void setLogcatEnable(boolean enable) {
        if (enable) {
            if (!isToLogcat) {
                isToLogcat = true;
                Log.v(TAG, "Logcat Enabled");
            } else {
                Log.w(TAG, "Logcat Already Enabled ");
            }
        } else {
            if (isToLogcat) {
                isToLogcat = false;
                Log.v(TAG, "Logcat Disabled");
            } else {
                Log.w(TAG, "Logcat Already Disabled");
            }
        }
    }

    public static boolean isLogcatEnabled() {
        return isToLogcat;
    }

    /**
     * 设置是否保存日志到文件,文件所在目录示例/data/data/packageName/file/xx.log
     * 
     * @param enable 缺省false
     */
    public static void setLogToFileEnable(boolean enable, Context ctx) {
        if (enable) {
            if (!isToFile) {// 启用保存到文件
                isToFile = true;
                filePath = ctx.getFilesDir().getAbsolutePath();
                Log.v(TAG, "Save Log To File Enabled");
            } else {
                Log.w(TAG, "Save To File Already Enabled");
            }
        } else {
            if (isToFile) {
                isToFile = false;
                Log.v(TAG, "Save Log To File Disabled");
            } else {
                Log.w(TAG, "Save To File Already Disabled");
            }
        }
    }

    public static boolean isLogToFileEnabled() {
        return isToFile;
    }

    /**
     * 根据配置文件决定是否输出Logcat,是否输出Log文件
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
    public static void cfgFromFile(Context ctx) {
        File cfgFile = null;
        try {
            cfgFile = getCfgFile();
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        BufferedReader in = null;
        try {
            FileReader fr = new FileReader(cfgFile);
            in = new BufferedReader(fr);
            String line;
            while ((line = in.readLine()) != null) {

                if (line.contains("logcat")) {
                    if (line.split("=")[1].equals("true")) {
                        setLogcatEnable(true);
                    } else if (line.split("=")[1].equals("false")) {
                        setLogcatEnable(false);
                    } else {
                        Log.w(TAG, "wrong cfg file");
                    }
                }

                if (line.contains("file")) {
                    if (line.split("=")[1].equals("true")) {
                        setLogToFileEnable(true, ctx);
                    } else if (line.split("=")[1].equals("false")) {
                        setLogToFileEnable(false, ctx);
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

    /**
     * 保存String到文件
     * 
     * @param msg 需要记录的日志信息
     * @TODO 异步操作
     */
    private static void writeFile(String time, String tag, String msg) {
        File file = null;
        try {
            file = getLogFile();
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            writer = new BufferedWriter(new FileWriter(file.getAbsolutePath(), true));
            reader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(msg.getBytes())));

            boolean isFirstLoop = true;
            String line;
            while ((line = reader.readLine()) != null) {
                if (isFirstLoop) {
                    isFirstLoop = false;
                    writer.append(time);
                    writer.append(tag);
                    writer.append("\n");

                }

                if (!TextUtils.isEmpty(line)) {
                    writer.append(DATE_TIME_PLACEHOLDER);
                }

                writer.append(line);

                if (!TextUtils.isEmpty(line)) {
                    writer.append("\n");
                }

            }
            writer.append("\n");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException e) {
            }
        }
    }

    private static File getLogFile() throws IOException {
        File file = new File(filePath,
                DATE_FORMAT.format(new Date(System.currentTimeMillis())) + LOG_FILE_EXTENSION);
        return creatFileIfNotExists(file);
    }

    private static File getCfgFile() throws IOException {
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "log.cfg");
        return creatFileIfNotExists(file);
    }

    /**
     * @return null if IOException
     * @throws IOException
     */
    private static File creatFileIfNotExists(File file) throws IOException {
        if (!file.exists()) {
            new File(filePath).mkdirs();
            file.createNewFile();
        }
        return file;
    }

    /**
     * 发送日志文件到服务器
     */
    public static void sendLogFiles(Context ctx,
            Class<? extends AbsSendFileService> sendService) {
        Intent intent = new Intent(ctx, sendService);
        intent.putExtra(AbsSendFileService.INTENT_DIR, filePath);
        intent.putExtra(AbsSendFileService.INTENT_EXTENSION, LOG_FILE_EXTENSION);
        ctx.startService(intent);
    }

}

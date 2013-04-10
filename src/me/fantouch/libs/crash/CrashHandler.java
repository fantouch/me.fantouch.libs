
package me.fantouch.libs.crash;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;

import me.fantouch.libs.reporter.AbsSendReportsService;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

/**
 * 捕捉App全局异常,并由用户决定是否发送到服务器
 */
public class CrashHandler implements UncaughtExceptionHandler {
    /** Debug Log Tag */
    public static final String TAG = CrashHandler.class.getSimpleName();
    /** 是否开启日志输出, 在Debug状态下开启, 在Release状态下关闭以提升程序性能 */
    public static final boolean DEBUG = true;
    /** CrashHandler实例 */
    private static CrashHandler instance;
    /** 程序的Context对象 */
    private Context mContext;
    /** 系统默认的UncaughtException处理类,如果本CrashHandler没能处理,则交由此来处理 */
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    /** 使用Properties来保存设备的信息和错误堆栈信息 */
    private Properties mDeviceCrashInfo = new Properties();
    private static final String VERSION_NAME = "versionName";
    private static final String VERSION_CODE = "versionCode";
    private static final String STACK_TRACE = "STACK_TRACE";
    /** 错误报告文件的扩展名 */
    public static final String CRASH_REPORTER_EXTENSION = ".crash";
    /** 错误报告文件名中的日期格式 */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss",
            Locale.CHINA);

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }

        return instance;
    }

    /**
     * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
     */
    public void init(Context ctx, Class<? extends AbsSendReportsService> sendService) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        sendLastReport(sendService);
    }

    private void sendLastReport(Class<? extends AbsSendReportsService> sendService) {
        Intent intent = new Intent(mContext, sendService);
        intent.putExtra(AbsSendReportsService.INTENT_DIR, mContext.getFilesDir()
                .getAbsolutePath());
        intent.putExtra(AbsSendReportsService.INTENT_EXTENSION, CRASH_REPORTER_EXTENSION);
        mContext.startService(intent);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {

        // 开始处理异常
        if (mDefaultHandler == null || ex == null) {
            shutDown();
        } else {
            new Thread() {
                @Override
                public void run() {
                    // 在当前线程创建消息队列(对话框的显示需要消息队列)
                    Looper.prepare();

                    AlertDialog dialog = showExceptionDialog();
                    collectCrashDeviceInfo(mContext);
                    saveCrashInfoToFile(ex);
                    dismissExceptionDialog(dialog);

                    // 启动消息队列(在队列推出前,后面的代码不会被执行,在这里,后面没有代码了.)
                    Looper.loop();
                }
            }.start();
        }
    }

    /**
     * 强制关闭程序
     * FIXME 并不能退出所有Activity,目前尚未找到比较优雅的做法
     */
    private void shutDown() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private AlertDialog showExceptionDialog() {
        ProgressBar pb = new ProgressBar(mContext, null,
                android.R.attr.progressBarStyleInverse);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(pb);
        builder.setCancelable(false);
        builder.setTitle("程序出错了,即将退出");
        builder.setMessage("正在收集错误信息...");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
        return dialog;
    }

    private void dismissExceptionDialog(final AlertDialog dialog) {
        dialog.setMessage("正在退出...");
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                dialog.dismiss();
                shutDown();
            }
        }, 1000);
    }

    public void collectCrashDeviceInfo(Context ctx) {
        PackageHelper packageHelper = new PackageHelper(mContext);
        mDeviceCrashInfo.put(VERSION_NAME, packageHelper.getLocalVersionName());
        mDeviceCrashInfo.put(VERSION_CODE, packageHelper.getLocalVersionCode() + "");
        // 使用反射来收集设备信息.在Build类中包含各种设备信息,
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String fieldStr = "";
                try {
                    fieldStr = field.get(null).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mDeviceCrashInfo.put(field.getName(), fieldStr);
            } catch (Exception e) {
                Log.e(TAG, "Error while collect crash info", e);
            }
        }
    }

    private void saveCrashInfoToFile(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);

        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        String result = info.toString();
        printWriter.close();
        mDeviceCrashInfo.put(STACK_TRACE, result);

        try {
            String fileName = dateFormat.format(new Date(System.currentTimeMillis()))
                    + CRASH_REPORTER_EXTENSION;
            // 保存文件
            FileOutputStream trace = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            mDeviceCrashInfo.store(trace, "");
            trace.flush();
            trace.close();
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing report file", e);
        }
    }

}

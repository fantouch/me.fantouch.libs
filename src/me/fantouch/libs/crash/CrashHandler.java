
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

import me.fantouch.libs.reporter.AbsSendFileService;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * 捕捉App全局异常,并由用户决定是否发送到服务器
 */
public class CrashHandler implements UncaughtExceptionHandler {
    public static final String TAG = CrashHandler.class.getSimpleName();
    private static CrashHandler instance;
    private Context mContext;
    private UncaughtExceptionHandler mDefaultHandler;

    /** 使用Properties来保存设备的信息和错误堆栈信息 */
    private Properties mCrashInfo = new Properties();
    private static final String VERSION_NAME = "versionName";
    private static final String VERSION_CODE = "versionCode";
    /** 错误报告文件的扩展名 */
    public static final String CRASH_REPORTER_EXTENSION = ".crash";
    /** 错误报告文件名中的日期格式 */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }

        return instance;
    }

    /**
     * @param ctx
     * @param sendService 用户自行实现的发送服务
     */
    public void init(Context ctx, Class<? extends AbsSendFileService> sendService) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        sendLastReport(sendService);
    }

    /**
     * 发送上次的报告
     * 
     * @param sendService
     */
    private void sendLastReport(Class<? extends AbsSendFileService> sendService) {
        if (sendService == null)
            return;
        Intent intent = new Intent(mContext, sendService);
        intent.putExtra(AbsSendFileService.INTENT_DIR, mContext.getFilesDir()
                .getAbsolutePath());
        intent.putExtra(AbsSendFileService.INTENT_EXTENSION, CRASH_REPORTER_EXTENSION);
        mContext.startService(intent);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        if (mDefaultHandler == null || ex == null) {
            exitCurrentApp();
        } else {
            ex.printStackTrace();
            new Thread() {
                @Override
                public void run() {
                    // 在当前线程创建消息队列(对话框的显示需要消息队列)
                    Looper.prepare();

                    AlertDialog dialog = showExceptionDialog();
                    collectDeviceInfo(mContext);
                    saveCrashInfoToFile(ex);
                    dismissExceptionDialog(dialog);

                    // 启动消息队列(在队列推出前,后面的代码不会被执行,在这里,后面没有代码了.)
                    Looper.loop();
                }
            }.start();
        }
    }

    /**
     * 强制关闭程序<br>
     * FIXME 并不能退出所有Activity,目前尚未找到比较优雅的做法
     */
    private void exitCurrentApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 弹出异常说明对话框<br>
     * 使用系统级别的对话框
     * 需要权限 {@link WindowManager.LayoutParams.TYPE_SYSTEM_ALERT}
     * <p>
     * See <a href="http://android.35g.tw/?p=191">http://android.35g.tw/?p=191</a>
     */
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

        // <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
        return dialog;
    }

    private void dismissExceptionDialog(final AlertDialog dialog) {
        // 使用postDelayed让用户能有足够时间看清提示信息
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                dialog.setMessage("正在退出...");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        dialog.dismiss();
                        exitCurrentApp();

                    }
                }, 2 * 1000);
            }
        }, 2 * 1000);
    }

    private void collectDeviceInfo(Context ctx) {
        PackageHelper packageHelper = new PackageHelper(mContext);
        mCrashInfo.put(VERSION_NAME, packageHelper.getLocalVersionName());
        mCrashInfo.put(VERSION_CODE, packageHelper.getLocalVersionCode() + "");
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
                mCrashInfo.put(field.getName(), fieldStr);
            } catch (Exception e) {
                Log.e(TAG, "Error while collecting device info", e);
            }
        }
    }

    private void saveCrashInfoToFile(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);

        printWriter.write("\n=========printStackTrace()==========\n");
        ex.printStackTrace(printWriter);

        printWriter.write("\n\n=========getCause()==========\n");
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        String stackTrace = info.toString();
        printWriter.close();

        try {
            String fileName = dateFormat.format(new Date(System.currentTimeMillis()))
                    + CRASH_REPORTER_EXTENSION;
            // 保存文件
            FileOutputStream trace = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            mCrashInfo.store(trace, "");
            trace.write(stackTrace.getBytes());
            trace.flush();
            trace.close();
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing report file", e);
        }
    }
}

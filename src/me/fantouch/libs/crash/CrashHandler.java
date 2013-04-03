
package me.fantouch.libs.crash;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;

import me.fantouch.libs.reporter.AbsSendReportsService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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
    /** 崩溃日志发送器 */
    private Class<? extends AbsSendReportsService> mSendService;

    /** 使用Properties来保存设备的信息和错误堆栈信息 */
    private Properties mDeviceCrashInfo = new Properties();
    private static final String VERSION_NAME = "versionName";
    private static final String VERSION_CODE = "versionCode";
    private static final String STACK_TRACE = "STACK_TRACE";
    /** 错误报告文件的扩展名 */
    private static final String CRASH_REPORTER_EXTENSION = ".crash";
    /** 错误报告文件名中的日期格式 */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");

    /** 保证只有一个CrashHandler实例 */
    private CrashHandler() {
    }

    /** 获取CrashHandler实例 ,单例模式 */
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
        mSendService = sendService;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
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

                    // 收集设备信息
                    collectCrashDeviceInfo(mContext);
                    // 保存错误报告文件
                    saveCrashInfoToFile(ex);
                    // 显示用户友好的对话框
                    showExceptionDialog(ex);

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
        System.exit(10);
    }

    /**
     * 弹出异常对话框,并让用户选择是否发送错误信息到服务器
     */
    private void showExceptionDialog(final Throwable ex) {
        // 发送按钮监听器
        DialogInterface.OnClickListener positiveBtnLsnr = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendCrashReportsToServer(mContext);
            }
        };
        // 不发送按钮监听器
        DialogInterface.OnClickListener negativeBtnLsnr = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                shutDown();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setCancelable(true);
        builder.setTitle("程序出错了,即将退出");
        builder.setMessage("发送错误信息,帮助我们修复问题");
        builder.setPositiveButton("发送", positiveBtnLsnr);
        builder.setNegativeButton("不发送", negativeBtnLsnr);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    /**
     * 收集程序崩溃的设备信息
     * 
     * @param ctx
     */
    public void collectCrashDeviceInfo(Context ctx) {
        try {
            // Class for retrieving various kinds of information related to the
            // application packages that are currently installed on the device.
            // You can find this class through getPackageManager().
            PackageManager pm = ctx.getPackageManager();
            // getPackageInfo(String packageName, int flags)
            // Retrieve overall information about an application package that is installed on the
            // system.
            // public static final int GET_ACTIVITIES
            // Since: API Level 1 PackageInfo flag: return information about activities in the
            // package in activities.
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                // public String versionName The version name of this package,
                // as specified by the <manifest> tag's versionName attribute.
                mDeviceCrashInfo.put(VERSION_NAME, pi.versionName == null ? "not set"
                        : pi.versionName);
                // public int versionCode The version number of this package,
                // as specified by the <manifest> tag's versionCode attribute.
                mDeviceCrashInfo.put(VERSION_CODE, pi.versionCode + "");
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Error while collect package info", e);
        }
        // 使用反射来收集设备信息.在Build类中包含各种设备信息,
        // 例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
        // 返回 Field 对象的一个数组，这些对象反映此 Class 对象所表示的类或接口所声明的所有字段
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                // setAccessible(boolean flag)
                // 将此对象的 accessible 标志设置为指示的布尔值。
                // 通过设置Accessible属性为true,才能对私有变量进行访问，不然会得到一个IllegalAccessException的异常
                field.setAccessible(true);
                String fieldStr = "";
                try {
                    fieldStr = field.get(null).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mDeviceCrashInfo.put(field.getName(), fieldStr);
                if (DEBUG) {
                    Log.d(TAG, field.getName() + " : " + fieldStr);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error while collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     * 
     * @param ex
     * @return
     */
    private String saveCrashInfoToFile(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        // printStackTrace(PrintWriter s)
        // 将此 throwable 及其追踪输出到指定的 PrintWriter
        ex.printStackTrace(printWriter);

        // getCause() 返回此 throwable 的 cause；如果 cause 不存在或未知，则返回 null。
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        // toString() 以字符串的形式返回该缓冲区的当前值。
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
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing report file...", e);
        }
        return null;
    }

    /**
     * 启动后台服务,把错误报告发送给服务器,包含新产生的和以前没发送的.服务会在发送完毕后自动停止
     */
    private void sendCrashReportsToServer(Context ctx) {
        String[] crFileNames = getCrashReportFiles(ctx);
        if (crFileNames != null && crFileNames.length > 0) {
            Intent intent = new Intent(mContext, mSendService);
            intent.putExtra(AbsSendReportsService.CRASH_REPORT_FILE_NAMES, crFileNames);
            mContext.startService(intent);
            shutDown();
        }
    }

    /**
     * 获取错误报告文件名
     * 
     * @param ctx
     * @return
     */
    private String[] getCrashReportFiles(Context ctx) {
        File filesDir = ctx.getFilesDir();
        // 实现FilenameFilter接口的类实例可用于过滤器文件名
        FilenameFilter filter = new FilenameFilter() {
            // 测试指定文件是否应该包含在某一文件列表中。
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(CRASH_REPORTER_EXTENSION);
            }
        };
        // 返回一个字符串数组，这些字符串指定此抽象路径名表示的目录中满足指定过滤器的文件和目录
        return filesDir.list(filter);
    }

    /**
     * 在程序启动时候, 可以调用该函数来发送以前没有发送的报告
     */
    public void sendPreviousReportsToServer() {
        sendCrashReportsToServer(mContext);
    }

}


package me.fantouch.libs.crash;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeSet;

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
    private static final String CRASH_REPORTER_EXTENSION = ".cr";

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
     * 
     * @param ctx 如果需要弹出对话框,则必须传入一个Activity,因为ApplicationContext不能显示对话框
     */
    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {

        // 定义异常处理完毕的回调函数
        final ExceptionHandleResultCallback exceptionHandleResultCallback = new ExceptionHandleResultCallback() {

            @Override
            public void onHandleSucc() {
                shutDown();
            }

            @Override
            public void onHandleFail() {
                // 如果用户没有处理则让系统默认的异常处理器来处理
                if (mDefaultHandler != null) {
                    mDefaultHandler.uncaughtException(thread, ex);
                } else {
                    shutDown();
                }
            }
        };

        // 开始处理异常
        if (mDefaultHandler == null || ex == null) {
            exceptionHandleResultCallback.onHandleFail();
        } else {
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    showExceptionDialog(ex, exceptionHandleResultCallback);
                    Looper.loop();
                }
            }.start();
        }
    }

    /**
     * 强制关闭程序
     */
    private void shutDown() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    private final int PROGRESSBAR_ID = 1;
    private final int POSITIVEBTN_ID = 2;
    private final int NEGATIVEBTN_ID = 3;

    /**
     * 弹出异常对话框,并让用户选择是否发送错误信息到服务器
     */
    private void showExceptionDialog(final Throwable ex, final ExceptionHandleResultCallback cb) {
        final View dialogView = getDialogView();

        OnClickListener positiveBtnLsnr = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME http 异步发送错误信息,然后执行shutDown()
                // 收集设备信息
                collectCrashDeviceInfo(mContext);
                // 保存错误报告文件
                String crashFileName = saveCrashInfoToFile(ex);
                // 发送错误报告到服务器
                sendCrashReportsToServer(mContext,
                        (ProgressBar) dialogView.findViewById(PROGRESSBAR_ID), cb);
            }
        };
        dialogView.findViewById(POSITIVEBTN_ID).setOnClickListener(positiveBtnLsnr);

        OnClickListener negativeBtnLsnr = new OnClickListener() {
            @Override
            public void onClick(View v) {
                cb.onHandleFail();
            }
        };
        dialogView.findViewById(NEGATIVEBTN_ID).setOnClickListener(negativeBtnLsnr);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("程序出错了,即将退出");
        builder.setMessage("发送错误信息,帮助我们更快地修复问题");
        builder.create();
        builder.setCancelable(false);
        builder.setView(dialogView);
        builder.show();
    }

    /**
     * 代码绘制UI,避免此工具类对外部文件的依赖,方便移植 系统Dialog的Button一经按下就会导致Dialog消失,所以这里使用自己的Button
     */
    private View getDialogView() {
        // 父容器
        LinearLayout container = new LinearLayout(mContext);
        container.setOrientation(LinearLayout.VERTICAL);

        // 进度条
        ProgressBar pb = new ProgressBar(mContext, null,
                android.R.attr.progressBarStyleInverse);
        pb.setMax(100);
        pb.setVisibility(View.INVISIBLE);
        pb.setId(PROGRESSBAR_ID);
        container.addView(pb);

        // 按钮容器
        LinearLayout btnContainer = new LinearLayout(mContext);
        btnContainer.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);

        Button positiveBtn = new Button(mContext);
        positiveBtn.setLayoutParams(btnLp);
        positiveBtn.setText("发送");
        positiveBtn.setId(POSITIVEBTN_ID);
        btnContainer.addView(positiveBtn);

        Button negativeBtn = new Button(mContext);
        negativeBtn.setLayoutParams(btnLp);
        negativeBtn.setText("不发送");
        negativeBtn.setId(NEGATIVEBTN_ID);
        btnContainer.addView(negativeBtn);

        container.addView(btnContainer);
        return container;
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
            long timestamp = System.currentTimeMillis();
            String fileName = "crash-" + timestamp + CRASH_REPORTER_EXTENSION;
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
     * 把错误报告发送给服务器,包含新产生的和以前没发送的.
     * 
     * @param ctx
     * @param pb !! maybe NULL !!
     * @param cb !! maybe NULL !!
     */
    private void sendCrashReportsToServer(Context ctx, ProgressBar pb,
            ExceptionHandleResultCallback cb) {
        String[] crFileNames = getCrashReportFiles(ctx);
        if (crFileNames != null && crFileNames.length > 0) {

            TreeSet<String> sortedFiles = new TreeSet<String>();
            sortedFiles.addAll(Arrays.asList(crFileNames));

            HashMap<String, File> crFiles = new HashMap<String, File>();

            for (String fileName : sortedFiles) {
                File crFile = new File(ctx.getFilesDir(), fileName);
                if (crFile.exists()) {
                    crFiles.put(fileName, crFile);
                }
            }

            // 发送错误文件,文件发送成功后会被删除
            if (crFiles.size() != 0) {
                postReport(crFiles, pb, cb);
            } else {
                if (cb != null) {
                    cb.onHandleFail();
                }
            }
        } else {
            if (cb != null) {
                cb.onHandleFail();
            }
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
            // accept(File dir, String name)
            // 测试指定文件是否应该包含在某一文件列表中。
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(CRASH_REPORTER_EXTENSION);
            }
        };
        // list(FilenameFilter filter)
        // 返回一个字符串数组，这些字符串指定此抽象路径名表示的目录中满足指定过滤器的文件和目录
        return filesDir.list(filter);
    }

    private void postReport(HashMap<String, File> crFiles, final ProgressBar pb,
            final ExceptionHandleResultCallback cb) {
        // TODO 使用HTTP Post 发送错误报告到服务器
        AjaxParams params = new AjaxParams();

        try {// for test
            File f = new File("/mnt/sdcard/3200x2000.jpg");
            params.put("file", f);
        } catch (FileNotFoundException e1) {
            if (cb != null) {
                cb.onHandleFail();
            }
            e1.printStackTrace();
        }

        // 上传成功后文件会被删除
        final ArrayList<File> filesWillBeDel = new ArrayList<File>();

        Iterator<Entry<String, File>> crFilesIter = crFiles.entrySet().iterator();
        while (crFilesIter.hasNext()) {
            Entry<String, File> entry = crFilesIter.next();
            try {
                params.put(entry.getKey(), entry.getValue());
                filesWillBeDel.add(entry.getValue());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        FinalHttp fh = new FinalHttp();

        fh.post("url"/* //FIXME */, params,
                new AjaxCallBack<String>() {
            @Override
            public void onStart() {
                Log.i(TAG, "postReport, onStart()");
                if (pb != null) {
                    pb.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoading(long count, long current) {
                if (pb != null) {
                    pb.setVisibility(View.VISIBLE);
                    pb.setProgress((int) (current / (float) count * 100));
                }
                Log.i(TAG, count + "/" + current);
            }

            @Override
            public void onSuccess(String t) {
                Log.i(TAG, "onSuccess:" + t);
                for (File file : filesWillBeDel) {
                    file.delete();
                }
                if (cb != null) {
                    cb.onHandleSucc();
                }
            }

            @Override
            public void onFailure(Throwable t, String strMsg) {
                if (t != null) {
                    t.printStackTrace();
                }
                if (strMsg != null) {
                    Log.i(TAG, "strMsg = " + strMsg);
                }
                if ((t != null || strMsg != null) && cb != null) {
                    cb.onHandleFail();
                }
            }
        });
    }

    /**
     * 在程序启动时候, 可以调用该函数来发送以前没有发送的报告
     */
    public void sendPreviousReportsToServer() {
        sendCrashReportsToServer(mContext, null, null);
    }

}

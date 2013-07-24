
package me.fantouch.libs.updatehelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import me.fantouch.libs.R;
import me.fantouch.libs.updatehelper.UpdateListener.ForceUpdateListener;
import me.fantouch.libs.updatehelper.UpdateListener.NormalUpdateListener;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

public class UpdateHelper {
    private final String TAG = UpdateHelper.class.getSimpleName();

    private Activity mActivity;
    private PackageHelper mPackageHelper;
    private AbsUpdateInfoParser mParser;
    private NotificationHelper mNotificationHelper;
    private UpdateListener mUpdateListener;

    /**
     * 强制更新
     * 
     * @param activity
     * @param parser
     * @param listener
     */
    public UpdateHelper(Activity activity, AbsUpdateInfoParser parser,
            ForceUpdateListener listener) {
        if (activity == null) {
            throw new InvalidParameterException("Param Activity can not be null");
        }
        if (parser == null) {
            throw new InvalidParameterException("Param AbsUpdateInfoParser can not be null");
        }
        if (listener == null) {
            throw new InvalidParameterException("Param UpdateListener can not be null");
        }

        mParser = parser;
        mActivity = activity;
        mUpdateListener = listener;
        mPackageHelper = new PackageHelper(activity);
        mNotificationHelper = new NotificationHelper(activity, mPackageHelper);
    }

    /**
     * 普通更新
     * 
     * @param activity
     * @param parser
     * @param listener
     */
    public UpdateHelper(Activity activity, AbsUpdateInfoParser parser,
            NormalUpdateListener listener) {
        if (activity == null) {
            throw new InvalidParameterException("Param Activity can not be null");
        }
        if (parser == null) {
            throw new InvalidParameterException("Param AbsUpdateInfoParser can not be null");
        }
        if (listener == null) {
            throw new InvalidParameterException("Param UpdateListener can not be null");
        }

        mParser = parser;
        mActivity = activity;
        mUpdateListener = listener;
        mPackageHelper = new PackageHelper(activity);
        mNotificationHelper = new NotificationHelper(activity, mPackageHelper);
    }

    /**
     * 从指定的URL,以HTTP GET 的方式获取更新信息
     * 
     * @param url
     */
    public void check(String url) {
        new FinalHttp().get(url, new AjaxCallBack<String>() {
            @Override
            public void onStart() {
                mUpdateListener.onCheckStart();
            }

            @Override
            public void onSuccess(String t) {
                UpdateInfoBean infoBean = mParser.parse(t);
                if (infoBean != null // 如果有新版本
                        && infoBean.getVersionCode() > mPackageHelper.getLocalVersionCode()) {
                    showDialog(infoBean);
                } else {// 没有新版本
                    mUpdateListener.onCheckFinish();
                }
            }

            @Override
            public void onFailure(Throwable t, int errorNum, String strMsg) {
                if (t != null) {
                    t.printStackTrace();
                }
                if (strMsg != null) {
                    Log.e(TAG, "Check Update Error:" + strMsg);
                }
                mUpdateListener.onCheckFinish();
            }
        });
    }

    private void showDialog(final UpdateInfoBean bean) {
        StringBuffer msg = new StringBuffer();
        msg.append("最新版本:\n" + bean.getVersionName() + "\n\n");
        msg.append("当前版本:\n" + mPackageHelper.getLocalVersionName() + "\n\n");
        msg.append("更新日志:\n" + bean.getWhatsNew());

        ScrollView scrollView = (ScrollView) View.inflate(mActivity, R.layout.updatehelper_dialog,
                null);
        TextView textView = (TextView) scrollView
                .findViewById(R.id.updatehelper_dialog_tv);
        textView.setText(msg);

        DialogInterface.OnClickListener positiveBtnLsnr = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mUpdateListener.onDownloadStart();
                downApk(bean);
            }
        };
        DialogInterface.OnClickListener negativeBtnLsnr = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mUpdateListener instanceof ForceUpdateListener) {
                    ((ForceUpdateListener) mUpdateListener).onDecline();
                }
            }
        };
        DialogInterface.OnClickListener neutralBtnLsnr = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mUpdateListener instanceof NormalUpdateListener) {
                    ((NormalUpdateListener) mUpdateListener).onCancel();
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setView(scrollView);
        builder.setTitle("发现新版本");
        builder.setMessage("是否下载?");
        builder.setPositiveButton("下载", positiveBtnLsnr);
        if (mUpdateListener instanceof ForceUpdateListener) {
            builder.setNegativeButton("退出", negativeBtnLsnr);
        } else if (mUpdateListener instanceof NormalUpdateListener) {
            builder.setNeutralButton("下次再说", neutralBtnLsnr);
        }
        builder.setCancelable(false);
        builder.create();
        builder.show();

        fixScrollViewHeight(scrollView);
    }

    /**
     * 修正Android2.x上,包含ScrollView的Dialog总是充满屏幕的问题
     * 
     * @param scrollView
     */
    private void fixScrollViewHeight(ScrollView scrollView) {
        int screenHeight = mActivity.getWindowManager().getDefaultDisplay().getHeight();
        LayoutParams lp = scrollView.getLayoutParams();
        lp.height = screenHeight / 3;
        scrollView.setLayoutParams(lp);
    }

    private void downApk(UpdateInfoBean bean) {
        new FinalHttp().download(bean.getDownUrl(), getDownfilePath(bean),
                new AjaxCallBack<File>() {

            @Override
            public void onLoading(long count, long current) {
                float percent = (float) current / count * 100;
                mNotificationHelper.refreshProgress(percent);
            }

            @Override
            public void onSuccess(File file) {
                chmod(file);
                mNotificationHelper.notifyDownloadFinish(file);
            }

            @Override
                    public void onFailure(Throwable t, int errorNum, String strMsg) {
                if (t != null || strMsg != null) {
                    StringBuffer sb = new StringBuffer("下载失败");
                    if (strMsg != null) {
                        sb.append(strMsg);
                        Toast.makeText(mActivity, sb, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "downApk$onFailure() : " + strMsg);
                    }

                    if (t != null) {
                        t.printStackTrace();
                    }
                }
            }
        });
    }

    private String getDownfilePath(UpdateInfoBean bean) {
        String dir = mActivity.getCacheDir().getAbsoluteFile() + File.separator + "Downloads"
                + File.separator + bean.getVersionCode() + ".apk";

        File file = new File(dir);
        if (file.exists()) {// 存在旧文件
            file.delete();// 删除旧文件
        } else {// 不存在旧文件
            file.getParentFile().mkdirs();// 可能是第一次,创建需要的文件夹
        }

        return dir;
    }

    /**
     * 改变apk文件以及其父目录的权限,以支持/data/data/xx/yy.apk类型路径的apk安装<br>
     * http://blog.csdn.net/sodino/article/details/6549082
     * 
     * @param apkFile
     */
    private void chmod(File apkFile) {
        // [文件夹705:drwx---r-x]
        String[] parentFolderMod = {
                "chmod", "705", apkFile.getParent()
        };
        exec(parentFolderMod);
        // [文件604:-rw----r--]
        String[] apkFileMod = {
                "chmod", "604", apkFile.getAbsolutePath()
        };
        exec(apkFileMod);
    }

    private String exec(String[] args) {
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write("\n".getBytes());
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        Log.i(TAG, "exec:" + result);
        return result;
    }
}

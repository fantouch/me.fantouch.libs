
package me.fantouch.libs.crash;

import android.widget.ProgressBar;

import java.io.File;
import java.util.HashMap;

public interface AbsCrashReportsSender {
    /**
     * 请根据实际情况实现文件上传功能
     * 
     * @param crFiles 待崩溃信息文件
     * @param pb 进度条,你应该根据上传情况,处理好它的 {@link ProgressBar#setVisibility(int)}和
     *            {@link ProgressBar#setProgress(int)}
     * @param cb 请根据上传结果回调{@link ExceptionHandleResultCallback#onHandleSucc()}或者
     *            {@link ExceptionHandleResultCallback#onHandleFail()}
     */
    public void send(HashMap<String, File> crFiles, final ProgressBar pb,
            final ExceptionHandleResultCallback cb);
}

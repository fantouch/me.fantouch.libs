
package me.fantouch.libs.updatehelper;

/**
 * 更新信息检查结果监听器
 * 
 * @author Fantouch
 */
public interface UpdateListener {
    /**
     * 开始检查
     */
    public void onCheckStart();

    /**
     * 用户选择下载更新
     */
    public void onDownloadStart();

    /**
     * 检查完成(没有可用更新,或者检查更新中途出现异常)
     */
    public void onCheckFinish();

    public static interface ForceUpdateListener extends UpdateListener {
        /**
         * 用户明确拒绝更新(用于强制更新)<br>
         * 你应该在这里执行退出程序的操作,否则就不叫强制更新了
         */
        public void onDecline();
    }

    public static interface NormalUpdateListener extends UpdateListener {

        /**
         * 用户拒绝更新(用于资源更新,例如"下次再说")
         */
        public void onCancel();
    }
}

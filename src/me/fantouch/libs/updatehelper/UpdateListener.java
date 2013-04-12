
package me.fantouch.libs.updatehelper;

/**
 * 更新信息检查结果监听器
 * 
 * @author Fantouch
 */
public interface UpdateListener {
    /**
     * 可以在这提示用户正在检查更新
     */
    public void onCheckStart();

    /**
     * 如果用户选择下载更新,这个方法会被调用
     */
    public void onDownloadStart();

    /**
     * 检查完成(没有可用更新,或者检查更新中途出现异常)
     * 例如你是在程序Loading界面进行检查更新,那么现在可以跳过Loading进入程序首页了
     */
    public void onCheckFinish();

    /**
     * 强制更新结果监听器
     * 
     * @author Fantouch
     */
    public static interface ForceUpdateListener extends UpdateListener {
        /**
         * 用户明确拒绝更新<br>
         * 应该在这里执行退出程序的操作,否则就不叫强制更新了.<br>
         * 目前Android没有提供退出程序的方法,而非官方方法各有不足,适应性不强,这里不方便集成,请自行实现
         */
        public void onDecline();
    }

    /**
     * 自愿更新结果监听器
     * 
     * @author Fantouch
     */
    public static interface NormalUpdateListener extends UpdateListener {

        /**
         * 用户取消本次更新(选择了"下次再说")
         * 例如你是在程序Loading界面进行检查更新,那么现在可以跳过Loading进入程序首页了
         */
        public void onCancel();
    }
}

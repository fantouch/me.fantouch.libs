package me.fantouch.libs.scrolladv;

import android.os.Bundle;

public interface LifeCycleInterface {

    /**
     * 请根据生命周期调用
     */
    public void onSaveInstanceState(Bundle outState);

    /**
     * 请根据生命周期调用
     */
    public void onRestoreInstanceState(Bundle savedInstanceState);

    /**
     * 请根据生命周期调用
     */
    public void onPause();

    /**
     * 请根据生命周期调用
     */
    public void onResume();

    /**
     * 请根据生命周期调用
     */
    public void onDestroy();
}

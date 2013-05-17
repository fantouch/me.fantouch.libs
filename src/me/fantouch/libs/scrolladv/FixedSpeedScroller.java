package me.fantouch.libs.scrolladv;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

class FixedSpeedScroller extends Scroller {
    private int mDuration = 2000;

    /**
     * @param context
     * @param duration 动画时长
     */
    public FixedSpeedScroller(Context context, int duration) {
        super(context);
        mDuration = duration;
    }

    /**
     * 设置动画时长
     * 
     * @param duration
     */
    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public FixedSpeedScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}

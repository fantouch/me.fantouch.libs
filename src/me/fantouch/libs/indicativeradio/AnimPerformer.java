
package me.fantouch.libs.indicativeradio;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

/**
 * 简化动画代码,使动画开始与动画结束相关代码块紧凑,类似线程的一般匿名类用法
 * 
 * @author Fantouch
 */
public abstract class AnimPerformer {
    private View v;
    private Animation anim;

    /**
     * 简化动画代码,使动画开始与动画结束相关代码块紧凑,类似线程的一般匿名类用法
     * 
     * @param v 需要执行动画的View
     * @param anim 动画
     */
    public AnimPerformer(View v, Animation anim) {
        this.v = v;
        this.anim = anim;
        this.anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                onAnimStart(AnimPerformer.this.v);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onAnimEnd(AnimPerformer.this.v);
            }
        });
    }

    public void start() {
        v.startAnimation(anim);
    }

    abstract public void onAnimStart(View v);

    abstract public void onAnimEnd(View v);
}

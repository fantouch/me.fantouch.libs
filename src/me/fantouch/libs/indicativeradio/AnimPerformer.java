
package me.fantouch.libs.indicativeradio;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

/**
 * 简化动画代码,使动画开始与动画结束相关代码块紧凑,类似线程的一般匿名类用法
 * 
 * @author Fantouch
 */
public class AnimPerformer {

    /**
     * 简化动画代码,使动画开始与动画结束相关代码块紧凑,类似线程的一般匿名类用法
     * 
     * @param v 需要执行动画的View
     * @param anim 动画
     */
    public AnimPerformer(final View v, Animation anim) {
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                onAnimStart();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                onAnimRepeat();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onAnimEnd();
            }
        });

        v.startAnimation(anim);
    }



    public void onAnimStart() {
    };

    public void onAnimRepeat() {
    };

    public void onAnimEnd() {
    };
}

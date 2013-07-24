
package me.fantouch.libs.indicativeradio;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

/**
 * 动画执行器
 * <p>
 * 简化动画代码,使动画开始与动画结束相关代码块紧凑,类似线程的一般匿名类用法 <br>
 * new 完会立即执行,无需调用start什么的(其实我好几次忘记调用start了,所以干脆取消start方法)
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
    public AnimPerformer(final View v, final Animation anim) {
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                onAnimStart();
                onAnimStart(v, anim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                onAnimRepeat();
                onAnimRepeat(v, anim);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onAnimEnd();
                onAnimEnd(v, anim);
            }
        });

        v.startAnimation(anim);
    }

    public void onAnimStart() {
    };

    public void onAnimStart(View v, Animation anim) {
    };

    public void onAnimRepeat() {
    };

    public void onAnimRepeat(View v, Animation anim) {
    };

    public void onAnimEnd() {
    };

    public void onAnimEnd(View v, Animation anim) {
    };
}

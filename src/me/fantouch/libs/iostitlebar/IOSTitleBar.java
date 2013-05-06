
package me.fantouch.libs.iostitlebar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.fantouch.libs.R;
import me.fantouch.libs.indicativeradio.AnimPerformer;

public class IOSTitleBar extends RelativeLayout {
    private Button titleBtnBack, titleBtn;
    private TextView titleText0, titleText1;

    public IOSTitleBar(Context context) {
        super(context);
    }

    public IOSTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public IOSTitleBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        View.inflate(getContext(), R.layout.iostitlebar, this);

        titleBtnBack = (Button) findViewById(R.id.iostitlebar_leftButton);
        titleBtn = (Button) findViewById(R.id.iostitlebar_rightButton);
        titleText0 = (TextView) findViewById(R.id.iostitlebar_titleTextViewA);
        titleText1 = (TextView) findViewById(R.id.iostitlebar_titleTextViewB);
    }

    public void performBundle(TitleStateBundle bundle, boolean isForward, boolean isAnimated) {
        // 总是动画切换
        /* 标题文字 */
        setTitleTxtListener(bundle.getTitleTxtListener());
        /* 标题按钮 */
        titleBtn.setText(bundle.getTitleBtnTxt());
        setTitleBtnListener(bundle.getTitleBtnListener());
        if (bundle.isTitleBtnVisibility()) {
            showTitleBtn();
        } else {
            hideTitleBtn();
        }
        // 判断是否需要动画切换
        if (!isAnimated) {// 没动画
            /* 后退按钮 */
            if (bundle.isBackBtnVisible()) {
                showBackBtnWithoutAnim();
            } else {
                hideBackBtnWithoutAnim();
            }
            /* 标题文字 */
            changeTitleText(bundle.getTitleTxt(), isForward, false);
        } else {// 有动画
            /* 后退按钮 */
            if (bundle.isBackBtnVisible()) {
                showBackBtn();
            } else {
                hideBackBtn();
            }
            /* 标题文字 */
            changeTitleText(bundle.getTitleTxt(), isForward, true);
        }
    }

    public void setTitleTxtListener(OnClickListener l) {
        titleText0.setOnClickListener(l);
        titleText1.setOnClickListener(l);
    }

    public void setTitleBtnListener(OnClickListener l) {
        titleBtn.setOnClickListener(l);
    }

    private TextView getVisibleTitleTextView() {
        if (titleText0.getVisibility() == View.VISIBLE)
            return titleText0;
        else
            return titleText1;
    }

    private TextView getInvisibleTitleText() {
        if (titleText0.getVisibility() == View.VISIBLE)
            return titleText1;
        else
            return titleText0;
    }

    private TextView titleTextWhichWillDisappear, titleTextWhichWillAppear;

    private void changeTitleText(String txt, boolean isForward, boolean isAnimated) {
        // 因为两个对象交替使用,先根据可见性判断哪个需要退出,哪个需要进入
        titleTextWhichWillDisappear = getVisibleTitleTextView();
        titleTextWhichWillAppear = getInvisibleTitleText();
        // 将进入的对象准备文字
        titleTextWhichWillAppear.setText(txt);
        if (!isAnimated) {// 如果不需要动画
            titleTextWhichWillDisappear.setVisibility(View.GONE);
            titleTextWhichWillAppear.setVisibility(View.VISIBLE);
            return;// 结束本方法
        }
        // 准备动画
        Animation animDisappear, animAppear;
        if (isForward) {
            animDisappear = AnimationUtils.loadAnimation(getContext(),
                    R.anim.main_title_textview_hide_for_forward);
            animAppear = AnimationUtils.loadAnimation(getContext(),
                    R.anim.main_title_textview_show_for_forward);
        } else {
            animDisappear = AnimationUtils.loadAnimation(getContext(),
                    R.anim.main_title_textview_hide_for_back);
            animAppear = AnimationUtils.loadAnimation(getContext(),
                    R.anim.main_title_textview_show_for_back);
        }
        animDisappear.setFillAfter(true);
        animAppear.setFillAfter(true);
        // 执行动画
        new AnimPerformer(titleTextWhichWillDisappear, animDisappear) {
            @Override
            public void onAnimEnd() {
                titleTextWhichWillDisappear.clearAnimation();
                titleTextWhichWillDisappear.setVisibility(View.GONE);
            }
        };

        new AnimPerformer(titleTextWhichWillAppear, animAppear) {
            @Override
            public void onAnimStart() {
                titleTextWhichWillAppear.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimEnd() {
                titleTextWhichWillAppear.clearAnimation();
            }
        };

    }

    private void hideBackBtnWithoutAnim() {
        titleBtnBack.setVisibility(View.GONE);
    }

    private void showBackBtnWithoutAnim() {
        titleBtnBack.setVisibility(View.VISIBLE);
    }

    private void hideBackBtn() {
        if (titleBtnBack.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(getContext(),
                    R.anim.main_title_button_back_hide);
            animation.setFillAfter(true);
            new AnimPerformer(titleBtnBack, animation) {
                @Override
                public void onAnimEnd() {
                    titleBtnBack.clearAnimation();
                    titleBtnBack.setVisibility(View.GONE);
                }
            };
        }
    }

    private void showBackBtn() {
        if (titleBtnBack.getVisibility() != View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(getContext(),
                    R.anim.main_title_button_back_show);
            animation.setFillAfter(true);
            new AnimPerformer(titleBtnBack, animation) {
                @Override
                public void onAnimStart() {
                    titleBtnBack.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimEnd() {
                    titleBtnBack.clearAnimation();
                }
            };
        }
    }

    private void hideTitleBtn() {
        if (titleBtn.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(getContext(),
                    R.anim.main_title_button_hide);
            animation.setFillAfter(true);
            new AnimPerformer(titleBtn, animation) {

                @Override
                public void onAnimEnd() {
                    titleBtn.clearAnimation();
                    titleBtn.setVisibility(View.GONE);
                }
            };
        }
    }

    private void showTitleBtn() {
        if (titleBtn.getVisibility() != View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(getContext(),
                    R.anim.main_title_button_show);
            animation.setFillAfter(true);
            new AnimPerformer(titleBtn, animation) {
                @Override
                public void onAnimStart() {
                    titleBtn.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimEnd() {
                    titleBtn.clearAnimation();
                }
            };
        }
    }
}

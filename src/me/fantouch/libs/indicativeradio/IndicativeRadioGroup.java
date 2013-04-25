
package me.fantouch.libs.indicativeradio;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import me.fantouch.libs.R;

import java.security.InvalidParameterException;

public class IndicativeRadioGroup extends RelativeLayout {
    private int mIndicatorImgResId;
    private ImageView mIndicatator;
    private int mRadioGroupLayoutId;
    private RadioGroup mRadioGroup;
    private OnCheckedChangeListener mUsersOnCheckedChangeListener;
    private Animation mHideAnimation;
    private Animation mShowAnimation;
    private int mIndicatorMoveAnimationDuration;
    private int mIndicatorRestoreAnimationDuration;

    public IndicativeRadioGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttributes(context, attrs);
    }

    public IndicativeRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs);
    }

    public IndicativeRadioGroup(Context context) {
        super(context);
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IndicativeRadioGroup);

        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            switch (attr) {
                case R.styleable.IndicativeRadioGroup_radioGroup:
                    setRadioGroup(a.getResourceId(attr, View.NO_ID));
                    break;
                case R.styleable.IndicativeRadioGroup_indicatorDrawable:
                    setIndicator(a
                            .getResourceId(attr, R.drawable.indicativeradio_default_indicator));
                    break;
                case R.styleable.IndicativeRadioGroup_indicatorMoveAnimationDuration:
                    setIndicatorMoveAnimationDuration(a.getInt(attr, 500));
                    break;
                case R.styleable.IndicativeRadioGroup_indicatorRestoreAnimationDuration:
                    setIndicatorRestoreAnimationDuration(a.getInt(attr, 1500));
                    break;
                case R.styleable.IndicativeRadioGroup_hideAnimation:
                    setHideAnimation(AnimationUtils.loadAnimation(context,
                            a.getResourceId(attr, R.anim.indicativeradio_hide)));
                    break;
                case R.styleable.IndicativeRadioGroup_showAnimation:
                    setShowAnimation(AnimationUtils.loadAnimation(context,
                            a.getResourceId(attr, R.anim.indicativeradio_show)));
                    break;

                default:
                    break;
            }
        }

        a.recycle();
    }

    // BEGIN >>>>>>>>>>>> set attributes by code

    public void setRadioGroup(int radioGpLayoutId) {
        if (radioGpLayoutId == View.NO_ID)
            throw new InvalidParameterException(
                    "you must assign radioGroup for IndicativeRadioGroup in xml or in code");

        mRadioGroupLayoutId = radioGpLayoutId;
    }

    public void setIndicator(int resIdOfDrawableOrColor) {
        mIndicatorImgResId = resIdOfDrawableOrColor;
    }

    public void setIndicatorMoveAnimationDuration(int durationInMillisecond) {
        mIndicatorMoveAnimationDuration = durationInMillisecond;
    }

    public void setIndicatorRestoreAnimationDuration(int durationInMillisecond) {
        mIndicatorRestoreAnimationDuration = durationInMillisecond;
    }

    public void setHideAnimation(Animation anim) {
        mHideAnimation = anim;
    }

    public void setShowAnimation(Animation anim) {
        mShowAnimation = anim;
    }

    // END <<<<<<<<<<<< set attributes by code

    public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
        mUsersOnCheckedChangeListener = l;
    }

    @Override
    protected void onFinishInflate() {
        addRadioGp();
        super.onFinishInflate();
    }

    private void addRadioGp() {
        mRadioGroup = (RadioGroup) View.inflate(getContext(), mRadioGroupLayoutId, null);
        mRadioGroup.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRadioGroup.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int indicatorWith = (int) (mRadioGroup.getWidth() * 1.0 / mRadioGroup
                        .getChildCount());
                addIndicator(mIndicatorImgResId, indicatorWith);
            }
        });
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);
        addView(mRadioGroup, lp);

        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                invokeUsersOnCheckedChangeListener(group, checkedId);
                moveIndicator();
            }
        });
    }

    private void addIndicator(int indicatorImgResId, int indicatorWith) {
        mIndicatator = new ImageView(getContext());
        mIndicatator.setImageResource(indicatorImgResId);
        mIndicatator.setScaleType(ScaleType.FIT_XY);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(indicatorWith,
                LayoutParams.FILL_PARENT);

        addView(mIndicatator, lp);
    }

    private void invokeUsersOnCheckedChangeListener(RadioGroup group, int checkedId) {
        if (mUsersOnCheckedChangeListener != null) {
            mUsersOnCheckedChangeListener.onCheckedChanged(group, checkedId);
        }
    }

    private int mIndicatorStartXForNextMove = 0;

    private void moveIndicator() {
        int mIndicatorEndX = mIndicatator.getWidth() * getCheckedIdx();
        TranslateAnimation anim = new TranslateAnimation(mIndicatorStartXForNextMove,
                mIndicatorEndX, 0, 0);
        mIndicatorStartXForNextMove = mIndicatorEndX;// 下次起始位置就是现在的结束位置
        anim.setDuration(mIndicatorMoveAnimationDuration);
        anim.setInterpolator(new OvershootInterpolator());
        anim.setFillAfter(true);
        mIndicatator.startAnimation(anim);
    }

    private void restoreIndicatatorPosition() {
        TranslateAnimation anim = new TranslateAnimation(-mIndicatator.getWidth(),
                getCheckedIdx() * mIndicatator.getWidth(), 0, 0);
        anim.setDuration(mIndicatorRestoreAnimationDuration);
        anim.setInterpolator(new OvershootInterpolator());
        anim.setFillAfter(true);
        mIndicatator.startAnimation(anim);
    }

    private int getCheckedIdx() {
        int idx = 0;
        for (int i = 0; i < mRadioGroup.getChildCount(); i++) {
            if (mRadioGroup.getChildAt(i).getId() == mRadioGroup.getCheckedRadioButtonId()) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    /**
     * 隐藏本控件,你可以通过{@link IndicativeRadioGroup#setHideAnimation(Animation)}自定义动画
     */
    public void hide() {
        if (IndicativeRadioGroup.this.getVisibility() == View.VISIBLE) {

            if (mHideAnimation == null) {
                IndicativeRadioGroup.this.setVisibility(View.GONE);
                return;
            }

            new AnimPerformer(IndicativeRadioGroup.this, mHideAnimation) {
                @Override
                public void onAnimStart(View v) {
                }

                @Override
                public void onAnimEnd(View v) {
                    IndicativeRadioGroup.this.clearAnimation();
                    IndicativeRadioGroup.this.setVisibility(View.GONE);
                }
            }.start();
        }
    }

    /**
     * 显示本控件,你可以通过{@link IndicativeRadioGroup#setShowAnimation(Animation)}自定义动画
     */
    public void show() {
        if (IndicativeRadioGroup.this.getVisibility() != View.VISIBLE) {

            IndicativeRadioGroup.this.setVisibility(View.VISIBLE);
            if (mShowAnimation == null) {
                restoreIndicatatorPosition();
                return;
            }

            mShowAnimation.setFillAfter(true);
            new AnimPerformer(IndicativeRadioGroup.this, mShowAnimation) {
                @Override
                public void onAnimStart(View v) {
                    mIndicatator.clearAnimation();
                    mIndicatator.setVisibility(View.GONE);
                }

                @Override
                public void onAnimEnd(View v) {
                    IndicativeRadioGroup.this.clearAnimation();
                    restoreIndicatatorPosition();
                }
            }.start();
        }
    }

}

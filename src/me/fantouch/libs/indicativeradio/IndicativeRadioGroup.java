
package me.fantouch.libs.indicativeradio;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import me.fantouch.libs.R;

import java.security.InvalidParameterException;

public class IndicativeRadioGroup extends RelativeLayout {
    private ImageView indicatator;
    private RadioGroup radioGroup;

    public IndicativeRadioGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initFromAttributes(context, attrs);
    }

    public IndicativeRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFromAttributes(context, attrs);
    }

    public IndicativeRadioGroup(Context context) {
        super(context);

    }

    private void initFromAttributes(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IndicativeRadioGroup);

        int radioGroupLayoutId = a.getResourceId(R.styleable.IndicativeRadioGroup_radioGroup, -1);
        if (radioGroupLayoutId == -1)
            throw new InvalidParameterException(
                    "you must assign radioGroup for IndicativeRadioGroup in xml");
        LayoutInflater.from(context).inflate(radioGroupLayoutId, this, true);

        final int indicatorImgResId = a.getResourceId(
                R.styleable.IndicativeRadioGroup_indicatorDrawable,
                R.drawable.indicativeradio_default_indicator);
        for (int i = 0; i < getChildCount(); i++) {
            final View view = getChildAt(i);
            if (view instanceof RadioGroup) {
                view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        int indicatorWith = (int) (view.getWidth() * 1.0 / ((RadioGroup) view)
                                .getChildCount());
                        addIndicator(indicatorImgResId, indicatorWith);
                    }
                });
                break;
            }
        }

        a.recycle();
    }

    private void addIndicator(int indicatorImgResId, int indicatorWith) {
        ImageView indicator = new ImageView(getContext());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(indicatorWith,
                LayoutParams.FILL_PARENT);
        indicator.setImageResource(indicatorImgResId);
        indicator.setScaleType(ScaleType.FIT_XY);
        addView(indicator, lp);
    }

}

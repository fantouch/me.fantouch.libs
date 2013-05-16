package me.fantouch.libs.scrolladv;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.tsz.afinal.FinalBitmap;

import me.fantouch.libs.R;
import me.fantouch.libs.multiviewpager.RefImgDownloader;

import java.lang.reflect.Field;
import java.util.List;

public class ScrollAdv extends FrameLayout {
    private int remainDur = 2000;
    private int switchAnimDur = 1000;
    private int indicatorMargin;
    private int indicatorDefaultId = R.drawable.scrolladv_indicator_default;
    private int indicatorFocusedId = R.drawable.scrolladv_indicator_focused;
    private ViewPager mViewPager;
    private LinearLayout mIndicatorContainer;
    private ImageView[] indicators;
    private OnItemClickListener mOnItemClickListener;
    private List<String> mImgUrls;
    /**
     * 暂时不支持代码实例化
     * 
     * @param context
     */
    private ScrollAdv(Context context) {
        super(context);
    }

    public ScrollAdv(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrsFromXML(context, attrs);
        init(context);
    }

    public ScrollAdv(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrsFromXML(context, attrs);
        init(context);
    }


    private void initAttrsFromXML(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollAdv);
        try {
            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.ScrollAdv_remainDur:
                        remainDur = a.getInt(attr, 2000);
                        break;
                    case R.styleable.ScrollAdv_switchAnimDur:
                        switchAnimDur = a.getInt(attr, 1000);
                        break;
                    case R.styleable.ScrollAdv_indicator_default:
                        indicatorDefaultId = a.getResourceId(attr,
                                R.drawable.scrolladv_indicator_default);
                        break;
                    case R.styleable.ScrollAdv_indicator_focused:
                        indicatorFocusedId = a.getResourceId(attr,
                                R.drawable.scrolladv_indicator_focused);
                        break;
                    case R.styleable.ScrollAdv_indicator_margin:
                        indicatorMargin = a.getDimensionPixelSize(attr,
                                R.dimen.indicator_default_margin);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a.recycle();
        }

    }

    private void init(Context context) {
        mViewPager = new ViewPager(context);
        addView(mViewPager);

        mIndicatorContainer = new LinearLayout(context);
        mIndicatorContainer.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.RIGHT);
        lp.setMargins(indicatorMargin, indicatorMargin, indicatorMargin, indicatorMargin);
        mIndicatorContainer.setLayoutParams(lp);
        addView(mIndicatorContainer);
    }

    private void drawIndicators(Context context, int indicatorCount) {
        mIndicatorContainer.removeAllViews();

        indicators = new ImageView[indicatorCount];
        for (int i = 0; i < indicatorCount; i++) {
            indicators[i] = new ImageView(context);
            if (i == 0) {
                indicators[i].setImageResource(indicatorFocusedId);
            } else {
                indicators[i].setImageResource(indicatorDefaultId);
            }
            mIndicatorContainer.addView(indicators[i]);
        }
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void setImgs(List<String> urlStrings) {
        mImgUrls = urlStrings;
    }

    private void setFixedSpeedScroller(Context context, int duration) {
        FixedSpeedScroller fixedSpeedScroller = new FixedSpeedScroller(context, duration);
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(this, fixedSpeedScroller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public interface OnItemClickListener {
        public void OnItemClick(int postion);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    private class MyPagerAdapter extends PagerAdapter {
        private FinalBitmap fb;

        public MyPagerAdapter(Context context) {
            super();
            initFinalBitmap(context);
        }

        private void initFinalBitmap(Context context) {
            fb = FinalBitmap.create(context);
            fb.configDownlader(new RefImgDownloader(context));
            fb.configCompressFormat(Bitmap.CompressFormat.PNG);
            fb.configLoadingImage(android.R.drawable.ic_menu_sort_by_size);
            fb.configLoadfailImage(android.R.drawable.ic_menu_close_clear_cancel);
            fb.configBitmapMaxWidth(getResources().getDisplayMetrics().widthPixels);
            fb.configBitmapMaxHeight(getResources().getDisplayMetrics().heightPixels);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            ImageView imageView = new ImageView(getContext());
            fb.display(imageView,
                    "http://www.fantouch.me/imgs.demo.fantouch.me/img"
                            + (position + 1) +
                            ".jpg");

            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 22;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }
    }
}

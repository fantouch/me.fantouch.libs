package me.fantouch.libs.multiviewpager;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import me.fantouch.libs.R;
import me.fantouch.libs.log.Logg;

/**
 * PagerContainer: A layout that displays a ViewPager with its children that are outside
 * the typical pager bounds.
 * <p>
 * 
 * @author fantouch
 * @powerBy https://gist.github.com/devunwired/8cbe094bb7a783e37ad1
 */
public class PagerContainer extends FrameLayout implements ViewPager.OnPageChangeListener {

    private ViewPager mPager;
    boolean mNeedsRedraw = false;

    private int pageWidth, pageHeight, pageMargin;

    /**
     * 暂不支持使用代码方式实例化
     * 
     * @param context
     */
    private PagerContainer(Context context) {
        super(context);
        init(context);
    }

    public PagerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributesFromXml(context, attrs);
        init(context);
    }

    public PagerContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttributesFromXml(context, attrs);
        init(context);
    }

    /* 生命周期 */
    private static final String TAG = PagerContainer.class.getName();

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(TAG, mPager.getCurrentItem());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mPager.setCurrentItem(savedInstanceState.getInt(TAG, 0));
    }
    /* 生命周期 */

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void init(Context context) {
        // Disable clipping of children so non-selected pages are visible
        setClipChildren(false);

        // Child clipping doesn't work with hardware acceleration in Android 3.x/4.x
        // You need to set this value here if using hardware acceleration in an
        // application targeted at these releases.
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        // 添加ViewPager
        mPager = new ViewPager(context);
        mPager.setId(R.id.pager);
        // If hardware acceleration is enabled, you should also remove
        // clipping on the pager for its children.
        mPager.setClipChildren(false);
        mPager.setPageMargin(pageMargin);
        mPager.setOnPageChangeListener(this);
        // Necessary or the pager will only have one extra page to show
        // make this at least however many pages you can see
        mPager.setOffscreenPageLimit(calcOffscreenPageLimit(context));
        mPager.setLayoutParams(new FrameLayout.LayoutParams(pageWidth, pageHeight,
                Gravity.CENTER_HORIZONTAL));
        this.addView(mPager);
    }

    /**
     * 计算需要缓存的Pager数量
     * <p>
     * 假设屏幕可以显示=3页,除去当前被聚焦的一页,则需要缓存2页,<br>
     * 为了流畅,我们缓存2+1,那么就是缓存=3,<br>
     * 所以,所需缓存数量3=屏幕可以显示的数量3
     * 
     * @param context
     * @return 需要缓存的Pager数量
     */
    private int calcOffscreenPageLimit(Context context) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int eachPageWidth = pageWidth + pageMargin;
        int pageCountCanBeSeen = (int) Math.ceil(screenWidth * 1.0f / eachPageWidth);
        Logg.i(pageCountCanBeSeen + "");
        return pageCountCanBeSeen;
    }

    /**
     * 解析布局文件的属性
     * 
     * @author fantouch
     * @param context
     * @param attrs
     */
    private void initAttributesFromXml(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PagerContainer);
        try {
            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.PagerContainer_page_width:
                        pageWidth = a.getDimensionPixelSize(attr,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        break;
                    case R.styleable.PagerContainer_page_height:
                        pageHeight = a.getDimensionPixelSize(attr,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        break;
                    case R.styleable.PagerContainer_page_margin:
                        pageMargin = a.getDimensionPixelSize(attr, R.dimen.default_page_margin);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a.recycle();
        }
    }

    public ViewPager getViewPager() {
        return mPager;
    }

    public void setAdapter(PagerAdapter adapter) {
        mPager.setAdapter(adapter);
    }

    private Point mCenter = new Point();
    private Point mInitialTouch = new Point();

    private OnPageChangeListener usersListener;

    public void setOnPageChangeListener(OnPageChangeListener l) {
        usersListener = l;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCenter.x = w / 2;
        mCenter.y = h / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // We capture any touches not already handled by the ViewPager
        // to implement scrolling from a touch outside the pager bounds.
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialTouch.x = (int) ev.getX();
                mInitialTouch.y = (int) ev.getY();
            default:
                ev.offsetLocation(mCenter.x - mInitialTouch.x, mCenter.y - mInitialTouch.y);
                break;
        }

        return mPager.dispatchTouchEvent(ev);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Force the container to redraw on scrolling.
        // Without this the outer pages render initially and then stay static
        if (mNeedsRedraw) {
            invalidate();
        }

        // invoke usersListener
        if (usersListener != null) {
            usersListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        // invoke usersListener
        if (usersListener != null) {
            usersListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mNeedsRedraw = (state != ViewPager.SCROLL_STATE_IDLE);

        // invoke usersListener
        if (usersListener != null) {
            usersListener.onPageScrollStateChanged(state);
        }
    }
}
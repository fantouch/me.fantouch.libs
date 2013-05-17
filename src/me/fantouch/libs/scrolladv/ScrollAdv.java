package me.fantouch.libs.scrolladv;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import me.fantouch.libs.R;

import java.lang.reflect.Field;
import java.util.List;

public class ScrollAdv extends FrameLayout {
    private static final String TAG = ScrollAdv.class.getName();

    /* Attributes */
    private int remainDur = 500;
    private int switchAnimDur = 300;
    private int indicatorMargin;
    private int indicatorDefaultId = R.drawable.scrolladv_indicator_default;
    private int indicatorFocusedId = R.drawable.scrolladv_indicator_focused;
    private int loadingImgId = android.R.drawable.stat_notify_sync;
    private int loadFailImgId = android.R.drawable.stat_notify_error;
    /* Attributes */

    private ViewPager mViewPager;
    private LinearLayout mIndicatorContainer;
    private ImageView[] indicators;
    private int lastSelectostion = 0;
    private AutoInt autoInt;
    private HeartBeatThread heartBeatThread;

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
        initScrollAdv();
    }

    public ScrollAdv(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrsFromXML(context, attrs);
        initScrollAdv();
    }

    /**
     * 从布局文件获取属性
     * 
     * @param context
     * @param attrs
     */
    private void initAttrsFromXML(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollAdv);
        try {
            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.ScrollAdv_remainDur:
                        remainDur = a.getInt(attr, remainDur);
                        break;
                    case R.styleable.ScrollAdv_switchAnimDur:
                        switchAnimDur = a.getInt(attr, switchAnimDur);
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
                    case R.styleable.ScrollAdv_loading_img:
                        loadingImgId = a.getResourceId(attr, loadingImgId);
                        break;
                    case R.styleable.ScrollAdv_load_fail_img:
                        loadFailImgId = a.getResourceId(attr, loadFailImgId);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a.recycle();
        }

    }

    private void initScrollAdv() {
        // 实例化ViewPager
        mViewPager = new ViewPager(getContext());
        setFixedSpeedScroller(mViewPager, switchAnimDur);
        addView(mViewPager);

        // 实例化指示器容器
        mIndicatorContainer = new LinearLayout(getContext());
        mIndicatorContainer.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.RIGHT);
        lp.setMargins(indicatorMargin, indicatorMargin, indicatorMargin, indicatorMargin);
        mIndicatorContainer.setLayoutParams(lp);
        addView(mIndicatorContainer);
    }

    /**
     * 绘制指示器
     * 
     * @param indicatorCount 指示器个数
     */
    private void drawIndicators(int indicatorCount) {
        mIndicatorContainer.removeAllViews();

        indicators = new ImageView[indicatorCount];
        for (int i = 0; i < indicatorCount; i++) {
            indicators[i] = new ImageView(getContext());
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

    /**
     * 设置广告内容
     * 
     * @param urlStrings 图片列表
     * @param listener 图片点击监听器
     */
    public void setImgs(final List<String> urlStrings, OnImgClickListener listener) {
        drawIndicators(urlStrings.size());
        autoInt = new AutoInt(0, urlStrings.size() - 1);
        setupViewPager(urlStrings, listener);
    }

    private void setupViewPager(final List<String> urlStrings, OnImgClickListener listener) {
        ScrollAdvAdapter adapter = new ScrollAdvAdapter(getContext(), urlStrings, listener);
        adapter.getFinalBitmap().configLoadingImage(loadingImgId);
        adapter.getFinalBitmap().configLoadfailImage(loadFailImgId);
        mViewPager.setAdapter(adapter);

        // 用户操作的时候停止页面自动切换
        mViewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        stopHeartBeat();
                        break;
                    case MotionEvent.ACTION_UP:
                        startHeartBeat();
                        break;
                    default:
                        startHeartBeat();
                        break;
                }
                return false;
            }
        });

        // 监听页面切换,刷新指示器
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                autoInt.set(position, position - lastSelectostion > 0 ? true : false);// 刷新指示器记录,并根据是否已到达最值来设定步进方向
                lastSelectostion = position;

                for (int i = 0; i < indicators.length; i++) {
                    indicators[position]
                            .setImageResource(R.drawable.scrolladv_indicator_focused);
                    if (position != i) {
                        indicators[i]
                                .setImageResource(R.drawable.scrolladv_indicator_default);
                    }
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * 开始发布心跳事件
     */
    private void startHeartBeat() {
        if (heartBeatThread == null || !heartBeatThread.isAlive()) {
            heartBeatThread = new HeartBeatThread(remainDur + switchAnimDur, heartBeatHandler);
            heartBeatThread.start();
        }
    }

    /**
     * 停止发布心跳事件
     */
    private void stopHeartBeat() {
        if (heartBeatThread != null) {
            heartBeatThread.kill();
            heartBeatThread = null;
        }
    }

    /**
     * 处理心跳事件的Handler,实现图片切换
     */
    private final Handler heartBeatHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(autoInt.get());
            }
        }
    };

    /**
     * 设置图片切换动画时长
     * 
     * @param viewPager 需要设置切换动画时长的ViewPager
     * @param duration 动画时长,单位毫秒
     */
    private void setFixedSpeedScroller(ViewPager viewPager, int duration) {
        FixedSpeedScroller fixedSpeedScroller = new FixedSpeedScroller(viewPager.getContext(),
                duration);
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(viewPager, fixedSpeedScroller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 图片点击监听器
     * 
     * @author Fantouch
     */
    public interface OnImgClickListener {
        public void onImgClick(int position);
    }

    /**
     * 请根据生命周期调用
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(TAG, lastSelectostion);
    }

    /**
     * 请根据生命周期调用
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mViewPager.setCurrentItem(savedInstanceState.getInt(TAG, 0));
    }

    /**
     * 请根据生命周期调用
     */
    public void onPause() {
        stopHeartBeat();
        ((ScrollAdvAdapter) mViewPager.getAdapter()).getFinalBitmap().onPause();
    }

    /**
     * 请根据生命周期调用
     */
    public void onResume() {
        startHeartBeat();
        ((ScrollAdvAdapter) mViewPager.getAdapter()).getFinalBitmap().onResume();
    }

    /**
     * 请根据生命周期调用
     */
    public void onDestroy() {
        ((ScrollAdvAdapter) mViewPager.getAdapter()).getFinalBitmap().onDestroy();
    }

}

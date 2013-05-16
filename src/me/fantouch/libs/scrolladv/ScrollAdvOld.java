
package me.fantouch.libs.scrolladv;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Scroller;

import me.fantouch.libs.R;
import me.fantouch.libs.crash.MyApplication;

import net.tsz.afinal.FinalBitmap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ScrollAdvOld {
    private final String TAG = ScrollAdvOld.class.getSimpleName();
    private ImageView[] indicators = null;
    private ImageView imageView = null;
    private ViewPager advPager = null;
    private boolean isContinue = true;
    private FinalBitmap fb;
    private AutoInt autoInt;
    private int lastSelectostion = 0;
    private int remainDur = 2000;
    private int switchAnimDur = 1000;
    private FixedSpeedScroller fixedSpeedScroller;

    public ScrollAdvOld(View parent, ArrayList<String> urls) {
        autoInt = new AutoInt(0, urls.size() - 1);
        initViewPager(parent, urls);
    }

    public void setRemainDur(int remainDur) {
        this.remainDur = remainDur;
    }

    public void setSwitchAnimDur(int switchAnimDur) {
        this.switchAnimDur = switchAnimDur;
        fixedSpeedScroller.setDuration(switchAnimDur);
    }

    private void initViewPager(View v, ArrayList<String> urls) {
        advPager = (ViewPager) v.findViewById(R.id.pager);
        fixedSpeedScroller = new FixedSpeedScroller(advPager.getContext(), switchAnimDur);
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(advPager, fixedSpeedScroller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        ViewGroup indicatorContainer = (ViewGroup) v.findViewById(R.id.indicatorContainer);

        List<View> advPics = new ArrayList<View>();


        fb.configBitmapMaxHeight(250);
        fb.configBitmapMaxWidth(500);
        for (int i = 0; i < urls.size(); i++) {
            ImageView img = new ImageView(v.getContext());
            img.setBackgroundColor(0xffF6F6F6);
            img.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemSelectListener != null) {
                        itemSelectListener.onItemSelect(advPager.getCurrentItem());
                    }
                }
            });
            fb.display(img, urls.get(i));
            advPics.add(img);
        }

        indicators = new ImageView[advPics.size()];

        for (int i = 0; i < advPics.size(); i++) {
            imageView = new ImageView(v.getContext());
            indicators[i] = imageView;
            if (i == 0) {
                indicators[i]
                        .setBackgroundResource(R.drawable.scrolladv_indicator_focused);
            } else {
                indicators[i]
                        .setBackgroundResource(R.drawable.scrolladv_indicator_default);
            }
            indicatorContainer.addView(indicators[i]);
        }

        advPager.setAdapter(new AdvAdapter(advPics));
        advPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                autoInt.set(position, position - lastSelectostion > 0 ? true : false);
                lastSelectostion = position;

                for (int i = 0; i < indicators.length; i++) {
                    indicators[position]
                            .setBackgroundResource(R.drawable.scrolladv_indicator_focused);
                    if (position != i) {
                        indicators[i]
                                .setBackgroundResource(R.drawable.scrolladv_indicator_default);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {}

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
        advPager.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        isContinue = false;
                        break;
                    case MotionEvent.ACTION_UP:
                        isContinue = true;
                        break;
                    default:
                        isContinue = true;
                        break;
                }
                return false;
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (isContinue) {
                        viewHandler.sendEmptyMessage(autoInt.get());
                    }
                    try {
                        Thread.sleep(remainDur);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private final Handler viewHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            advPager.setCurrentItem(msg.what);
        }

    };

    private final class AdvAdapter extends PagerAdapter {
        private List<View> views = null;

        public AdvAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(views.get(arg1));
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(views.get(arg1), 0);
            return views.get(arg1);

        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    private OnItemSelectListener itemSelectListener;

    public interface OnItemSelectListener {
        public void onItemSelect(int postion);
    }

    public void setOnItemSelectListener(OnItemSelectListener l) {
        itemSelectListener = l;
    }

    private static class AutoInt {
        private boolean willAdd = true;
        private int min = 0, max = 0;
        private AtomicInteger auto;

        public AutoInt(int min, int max) {
            this.min = min;
            this.max = max;
            this.auto = new AtomicInteger(min);
        }

        public int get() {
            if (willAdd) {// 递增
                if (auto.get() == max) {// 到最大值了,下一次get将会递减
                    willAdd = false;
                    return auto.getAndDecrement();
                } else {// 没到最大值,照常递增
                    return auto.getAndIncrement();
                }
            } else {// 递减
                if (auto.get() == min) {// 到最小值了,下一次get将会递增
                    willAdd = true;
                    return auto.getAndIncrement();
                } else {
                    return auto.getAndDecrement();
                }
            }
        }

        public void set(int current, boolean willAdd) {
            if (current < min) {
                auto.set(min);
                this.willAdd = true;
            } else if (current > max) {
                auto.set(max);
                this.willAdd = false;
            } else {
                auto.set(current);
                this.willAdd = willAdd;
            }

        }

        public static void unitTest() {
            AutoInt auto = new AutoInt(0, 4);
            for (int i = 0; i < 20; i++) {
                System.out.println(auto.get());
            }

            System.out.println("\nset(-10,false):");
            auto.set(-10, false);
            for (int i = 0; i < 20; i++) {
                System.out.println(auto.get());
            }
            System.out.println("\nset(-10,true):");
            auto.set(-10, true);
            for (int i = 0; i < 20; i++) {
                System.out.println(auto.get());
            }

            System.out.println("\nset(10,false):");
            auto.set(10, false);
            for (int i = 0; i < 20; i++) {
                System.out.println(auto.get());
            }
            System.out.println("\nset(10,true):");
            auto.set(10, true);
            for (int i = 0; i < 20; i++) {
                System.out.println(auto.get());
            }

            System.out.println("\nset(0,false):");
            auto.set(0, false);
            for (int i = 0; i < 20; i++) {
                System.out.println(auto.get());
            }
            System.out.println("\nset(0,true):");
            auto.set(0, true);
            for (int i = 0; i < 20; i++) {
                System.out.println(auto.get());
            }

            System.out.println("\nset(4,false):");
            auto.set(4, false);
            for (int i = 0; i < 20; i++) {
                System.out.println(auto.get());
            }
            System.out.println("\nset(4,true):");
            auto.set(4, true);
            for (int i = 0; i < 20; i++) {
                System.out.println(auto.get());
            }

            System.out.println("\nset(2,false):");
            auto.set(2, false);
            for (int i = 0; i < 20; i++) {
                System.out.println(auto.get());
            }
            System.out.println("\nset(2,true):");
            auto.set(2, true);
            for (int i = 0; i < 20; i++) {
                System.out.println(auto.get());
            }
        }
    }

}

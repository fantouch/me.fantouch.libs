package me.fantouch.libs.simplecustomview;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * 此ScrollChildScrollView允许子View水平滑动<br>
 * 直接替换Android SDK的ScrollView即可
 * 
 * @author fantouch
 */
public class ScrollChildScrollView extends ScrollView {
    private GestureDetector mGestureDetector;

    public ScrollChildScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ScrollChildScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScrollChildScrollView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mGestureDetector = new GestureDetector(context, new YScrollDetecotr());
        setFadingEdgeLength(0);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	return super.onInterceptTouchEvent(ev)
                && mGestureDetector.onTouchEvent(ev);
    }

    private static class YScrollDetecotr extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {

            double angle = Math.atan2(Math.abs(distanceY), Math.abs(distanceX));
            /* 上下=90,左右=0 */
            double direction = (180 * angle) / Math.PI;
            // System.out.println("direction-->" + direction);

            /* 如果大于55则认为是在上下滚动 */
            if (direction > 55) {
                return true;
            }
            return false;
        }
    }

    public void scrollToBottom(final View scroll, final View inner) {
        post(new Runnable() {
            @Override
            public void run() {
                if (scroll == null || inner == null) {
                    return;
                }

                int offset = inner.getMeasuredHeight() - scroll.getHeight();
                if (offset < 0) {
                    offset = 0;
                }
                smoothScrollTo(0, offset);
            }
        });
    }
}

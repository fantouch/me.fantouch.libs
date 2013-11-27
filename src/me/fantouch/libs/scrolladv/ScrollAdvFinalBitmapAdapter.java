package me.fantouch.libs.scrolladv;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import me.fantouch.libs.scrolladv.ScrollAdv.OnImgClickListener;

import net.tsz.afinal.FinalBitmap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

class ScrollAdvAdapter extends PagerAdapter {
    private FinalBitmap fb;
    private List<String> imgUrls;
    private OnImgClickListener mOnPagerItemClickListener;

    public ScrollAdvAdapter(Context context, List<String> imgUrls, OnImgClickListener listener) {
        super();
        this.imgUrls = imgUrls;
        this.mOnPagerItemClickListener = listener;
        initFinalBitmap(context);
    }

    public FinalBitmap getFinalBitmap() {
        return fb;
    }

    private void initFinalBitmap(Context context) {
        fb = getFinalBitmapInstanceWithoutSingleton(context);
        // fb.configDownlader(new RefImgDownloader(context));//为图片增加倒影
        fb.configCompressFormat(Bitmap.CompressFormat.PNG);
        fb.configLoadingImage(android.R.drawable.stat_notify_sync);
        fb.configLoadfailImage(android.R.drawable.stat_notify_error);
        fb.configBitmapMaxWidth(context.getResources().getDisplayMetrics().widthPixels);
        fb.configBitmapMaxHeight(context.getResources().getDisplayMetrics().heightPixels);
    }

    /**
     * FinalBitmap是单例模式,假如用户在其它地方也使用了单例模式,<br>
     * 那么跟这里的finalBitmap将会是同一个实例<br>
     * 配置会冲突,这里为了实现不同配置的FinalBitmap,用反射来破解单例<br>
     * 此方法不影响以前或以后通过单例模式获得的finalBitmap实例
     * 
     * @param context
     * @return
     */
    public FinalBitmap getFinalBitmapInstanceWithoutSingleton(Context context) {
        FinalBitmap cloneFb = null;
        try {
            Class<FinalBitmap> fbCls = FinalBitmap.class;
            // 获得构造方法 FinalBitmap(Context context)
            Constructor<FinalBitmap> constructor = fbCls.getDeclaredConstructor(Context.class);
            constructor.setAccessible(true);
            // 获得init()方法
            Method method = fbCls.getDeclaredMethod("init");
            method.setAccessible(true);
            // 仿照源码,分别执行构造方法和init()方法
            cloneFb = constructor.newInstance(context);
            method.invoke(cloneFb);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cloneFb;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        ImageView imageView = new ImageView(container.getContext());
        imageView.setScaleType(ScaleType.FIT_XY);
        imageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnPagerItemClickListener != null) {
                    mOnPagerItemClickListener.onImgClick(position);
                }

            }
        });
        fb.display(imageView, imgUrls.get(position));

        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return imgUrls.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }
}

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

/**
 * 适配器,加载工程drawable文件夹里面的图片(本地图片),图片过大可能会OOM(待完善)
 * 
 * @author fantouch
 */
class ScrollAdvResIdAdapter extends PagerAdapter {
    private List<Integer> mDrawableIds;
    private OnImgClickListener mOnPagerItemClickListener;

    public ScrollAdvResIdAdapter(Context context, List<Integer> drawableIds,
            OnImgClickListener listener) {
        super();
        this.mDrawableIds = drawableIds;
        this.mOnPagerItemClickListener = listener;
    }

    public FinalBitmap getFinalBitmap() {
        return null;
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
        imageView.setImageResource(mDrawableIds.get(position));

        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mDrawableIds.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }
}

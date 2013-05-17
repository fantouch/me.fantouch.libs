package me.fantouch.libs.scrolladv;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import me.fantouch.libs.multiviewpager.RefImgDownloader;
import me.fantouch.libs.scrolladv.ScrollAdv.OnImgClickListener;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.bitmap.download.Downloader;
import net.tsz.afinal.bitmap.download.SimpleHttpDownloader;

import java.io.OutputStream;
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
        fb = FinalBitmap.create(context);
        // fb.configDownlader(new RefImgDownloader(context));//为图片增加倒影
        fb.configDownlader(new SimpleHttpDownloader());// 默认下载器,无阴影
        fb.configCompressFormat(Bitmap.CompressFormat.PNG);
        fb.configLoadingImage(android.R.drawable.stat_notify_sync);
        fb.configLoadfailImage(android.R.drawable.stat_notify_error);
        fb.configBitmapMaxWidth(context.getResources().getDisplayMetrics().widthPixels);
        fb.configBitmapMaxHeight(context.getResources().getDisplayMetrics().heightPixels);
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

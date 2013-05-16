package me.fantouch.libs.scrolladv;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import me.fantouch.libs.multiviewpager.RefImgDownloader;

import net.tsz.afinal.FinalBitmap;

import java.util.List;

public class ScrollAdvAdapter extends PagerAdapter {
    private FinalBitmap fb;
    private List<String> imgUrls;

    public ScrollAdvAdapter(Context context, List<String> imgUrls) {
        super();
        this.imgUrls = imgUrls;
        initFinalBitmap(context);
    }

    public FinalBitmap getFinalBitmap() {
        return fb;
    }

    private void initFinalBitmap(Context context) {
        fb = FinalBitmap.create(context);
        fb.configDownlader(new RefImgDownloader(context));
        fb.configCompressFormat(Bitmap.CompressFormat.PNG);
        fb.configLoadingImage(android.R.drawable.ic_menu_sort_by_size);
        fb.configLoadfailImage(android.R.drawable.ic_menu_close_clear_cancel);
        fb.configBitmapMaxWidth(context.getResources().getDisplayMetrics().widthPixels);
        fb.configBitmapMaxHeight(context.getResources().getDisplayMetrics().heightPixels);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        ImageView imageView = new ImageView(container.getContext());
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

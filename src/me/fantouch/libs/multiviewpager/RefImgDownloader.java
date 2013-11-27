
package me.fantouch.libs.multiviewpager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.bitmap.core.BitmapDecoder;
import net.tsz.afinal.bitmap.download.Downloader;
import net.tsz.afinal.bitmap.download.SimpleDownloader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.util.Log;

/**
 * 下载图片过程中为图片添加倒影的下载器
 * <p>
 * 实现了{@link net.tsz.afinal.bitmap.download.Downloader}接口,用于
 * {@link FinalBitmap#configDownlader(Downloader)}
 * 
 * @author Fantouch
 */
public class RefImgDownloader implements Downloader {
    private static final String TAG = RefImgDownloader.class.getSimpleName();

    private SimpleDownloader mSimpleHttpDownloader;
    private String mCacheDir;
    private int mMaxWidth, mMaxHeight;

    public RefImgDownloader(Context context) {
        mSimpleHttpDownloader = new SimpleDownloader();
        mCacheDir = context.getCacheDir().getAbsolutePath();
        mMaxWidth = context.getResources().getDisplayMetrics().widthPixels;
        mMaxHeight = context.getResources().getDisplayMetrics().heightPixels;
    }


    @Override
    public byte[] download(String urlString) {
        Log.d(TAG, "download:" + urlString);

        // 创建缓存文件
        File origFile = null;
        try {
            origFile = new File(mCacheDir, MD5Util.getStringMD5String(urlString) + "origFile.jpg");
            if (origFile.exists())
                origFile.delete();
            origFile.getParentFile().mkdirs();
            origFile.createNewFile();
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }

        // 下载图片到缓冲文件
        try {
            OutputStream tempFileOutStrm = new FileOutputStream(origFile);
            tempFileOutStrm.write(mSimpleHttpDownloader.download(urlString));
            tempFileOutStrm.flush();
            tempFileOutStrm.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // 创建倒影图片并返回
        try {
            FileInputStream inputStream = new FileInputStream(origFile);
            Bitmap resizedBmp = BitmapDecoder.decodeSampledBitmapFromDescriptor(
                    inputStream.getFD(),
                    mMaxWidth, mMaxHeight);
            inputStream.close();
            origFile.delete();

            if (resizedBmp != null) {
                Bitmap refBmp = drawReflection(resizedBmp);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                refBmp.compress(Bitmap.CompressFormat.PNG, 80, baos);// 返回倒影图片到outputStream
                return baos.toByteArray();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @param originalImage 方法返回的时候,会被回收(originalImage.recycle(); )
     * @return
     */
    private Bitmap drawReflection(Bitmap originalImage) {
        final float imageReflectionRatio = 0.25f;
        final int reflectionGap = 0;
        final int width = originalImage.getWidth();
        final int height = originalImage.getHeight();
        final Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        /* 裁剪出需要倒影的部分,并垂直翻转 */
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage,// 原图
                0, (int) (height * imageReflectionRatio),// 裁剪的起点坐标
                width, (int) (height - height * imageReflectionRatio),// 裁剪后的尺寸
                matrix,// 把裁剪结果垂直翻转
                false);// 不开启抗锯齿?

        /* 创建目标尺寸的空bmp */
        Bitmap bitmapWithReflection = Bitmap.createBitmap(
                width, (int) (height * (1 + imageReflectionRatio)),// 目标尺寸
                Config.ARGB_8888);// 色彩质量

        /* 在画布上画出原图和倒影 */
        Canvas canvas = new Canvas(bitmapWithReflection);// 创建画布
        canvas.drawBitmap(originalImage, 0, 0, null);// 画出原图
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);// 画出倒影

        /* 创建渐变图层 */
        LinearGradient shader = new LinearGradient(
                0, originalImage.getHeight(), // 起点x,y
                0, bitmapWithReflection.getHeight() + reflectionGap,// 终点x,y
                0x70ffffff, 0x00ffffff, // 起点颜色,终点颜色
                TileMode.MIRROR);

        /* 画出渐变图层 */
        Paint paint = new Paint();
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);

        /* 回收工作 */
        originalImage.recycle();
        originalImage = null;
        reflectionImage.recycle();
        reflectionImage = null;

        return bitmapWithReflection;
    }

}

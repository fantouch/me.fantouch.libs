
package me.fantouch.libs.updatehelper;

import android.os.Environment;

import java.io.File;

class SDCardHelper {

    public static boolean hasSD() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static String getSDPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getFilePath(PackageHelper mPackageHelper,UpdateInfoBean bean){
        // e.g
        // http://wz.ue189.cn/filedownload?showname=1&filename=wzchannel_v2.9_build11_ue.apk
        String folderPath = SDCardHelper.getSDPath() + File.separator
                + mPackageHelper.getPackageName();
        File file = new File(folderPath, mPackageHelper.getPackageName() + "_" + bean.getVersionName() + "_"
                + bean.getVersionCode() + ".apk");
        
        if (file.exists()) {// 如果存在旧文件,删除之
            file.delete();
        }else{
            new File(folderPath).mkdirs();// 如果文件不存在,可能需要创建必要的文件夹
        }
        return file.getAbsolutePath();
    }
}

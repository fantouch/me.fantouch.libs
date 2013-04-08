
package me.fantouch.libs.reporter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * zip压缩工具<br>
 * 含有中文请谨慎,zip文件在不同操作系统容易中文乱码.
 * 
 * @author Fantouch
 */
public class ZipCompressor {
    static final int BUFFER = 8192;

    private File zipFile;

    /**
     * 压缩器构造函数
     * 
     * @param pathName 压缩包路径<br>
     *            e.g /Users/Admin/Desktop/test.zip
     */
    public ZipCompressor(String pathName) {
        zipFile = new File(pathName);
        if (zipFile.exists()) {
            System.out.println("删除旧的压缩文件:" + pathName);
            zipFile.delete();
        }
    }

    /**
     * 压缩一个文件,或者一个文件夹
     * 
     * @param srcPathName 文件路径,或文件夹路径
     */
    public void compress(String srcPathName) {
        File file = new File(srcPathName);
        if (!file.exists())
            throw new RuntimeException(srcPathName + "不存在！");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,
                    new CRC32());
            ZipOutputStream out = new ZipOutputStream(cos);
            String basedir = "";
            compress(file, out, basedir);
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 压缩多个文件
     * 
     * @param filePaths 文件路径
     */
    public void compress(String[] filePaths) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,
                    new CRC32());
            ZipOutputStream out = new ZipOutputStream(cos);
            String basedir = "";
            for (int i = 0; i < filePaths.length; i++) {
                compress(new File(filePaths[i]), out, basedir);
            }
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void compress(File file, ZipOutputStream out, String basedir) {
        /* 判断是目录还是文件 */
        if (file.isDirectory()) {
            System.out.println("压缩：" + basedir + file.getName());
            this.compressDirectory(file, out, basedir);
        } else {
            System.out.println("压缩：" + basedir + file.getName());
            this.compressFile(file, out, basedir);
        }
    }

    /** 压缩一个目录 */
    private void compressDirectory(File dir, ZipOutputStream out, String basedir) {
        if (!dir.exists())
            return;

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            /* 递归 */
            compress(files[i], out, basedir + dir.getName() + "/");
        }
    }

    /** 压缩一个文件 */
    private void compressFile(File file, ZipOutputStream out, String basedir) {
        if (!file.exists())
            return;
        try {
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            ZipEntry entry = new ZipEntry(basedir + file.getName());
            out.putNextEntry(entry);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            bis.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
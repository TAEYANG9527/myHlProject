package com.itcalf.renhe.context.wukong.im.kit;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class FileUtil {

    public static String copyImageToThumb(Context context,String srcFile){
        String destName = UUID.randomUUID().toString() + ".jpg";
        String destDir = FileUtil.getCacheDir(context).getAbsolutePath();
        String destFile = destDir + File.separator +destName;

        File fileDir = new File(destDir);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }

        try{
            InputStream streamFrom = new FileInputStream(srcFile);
            OutputStream streamTo = new FileOutputStream(destFile);
            byte buffer[]=new byte[1024];
            int len;
            while ((len= streamFrom.read(buffer)) > 0){
                streamTo.write(buffer, 0, len);
            }
            streamFrom.close();
            streamTo.close();
            return destFile;
        } catch(Exception ex){
            return null;
        }
    }


    public static boolean isCanUseSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static File getCacheDir(Context context) {
        File cacheDir = null;
        if (isCanUseSDCard()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                cacheDir = getExternalCacheDir8(context);
            } else {
                cacheDir = getExternalCacheDir7(context);
            }
        } else {
            cacheDir = context.getCacheDir();
        }
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        return cacheDir;
    }

    private static File getExternalCacheDir8(Context context) {
        return context.getExternalCacheDir();
    }

    private static final File getExternalCacheDir7(Context context) {
        return new File(Environment.getExternalStorageDirectory(), "/Android/data/"
                + context.getApplicationInfo().packageName + "/cache/");
    }

    public static String getFileNameSuffix(String filePath) {
        String suffix = "";
        if (!TextUtils.isEmpty(filePath)) {
            int dotIndex = filePath.lastIndexOf(".");
            if (dotIndex > 0) {
                suffix = filePath.substring(dotIndex + 1);
            }
        }
        return suffix;
    }


    public static boolean isLocalUrl(String url){
        return (url != null && (url.startsWith("/") || url.toLowerCase().startsWith("file:")));
    }

    public static long getFileSize(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }

        long size = 0;
        if (file.isFile()) {
            size = file.length();
        } else {
            for (File f : file.listFiles()) {
                size += getFileSize(f);
            }
        }
        return size;
    }


    public static void deleteFile(String filePath){
        if(TextUtils.isEmpty(filePath))
            return;

        File file = new File(filePath);
        if(file.exists())
            file.delete();
    }
}


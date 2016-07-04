package com.itcalf.renhe.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.service.RenheService;

import java.io.File;
import java.io.IOException;

/**
 * @author Chan
 * @description 日志搜集帮助类
 * @date 2015-5-19
 */
public class LoggerFileUtil {

    public static String LOGGER_SDPATH = Environment.getExternalStorageDirectory() + File.separator + "renhe" + File.separator
            + "logger" + File.separator;

    //	public static String LOGGER_FILE = LOGGER_SDPATH + "hl_log.log";

    /**
     * 写入文件        ---判断文件夹内是否有文件，存在，取出最近创建的一个文件写入；不存在，创建新文件写入---
     *
     * @param 内容
     * @param 追加 -被追加，如果是真的，写文件的末尾，否则文件中明确的内容，写进去
     * @return 返回true
     * @throws RuntimeException if an error occurs while operator FileWriter
     */
    public static boolean writeFile(String content, boolean append) {
//        String path = "";
//        OutputStreamWriter out = null;
//        try {
//            //判断存文件的文件夹是否存在，有没有为空
//            if (!isFileExist(LOGGER_SDPATH)) {
//                path = CreateFile();
//            } else {
//                /**判断文件是否可写**/
//                File file = new File(LOGGER_SDPATH);
//                //获取文件夹下所有的文件
//                File[] files = file.listFiles();
//                if (files.length > 0) {
//                    //获取最晚修改的文件
//                    int k = 0;
//                    for (int i = 0; i < files.length; i++) {
//                        k = files[0].lastModified() > files[i].lastModified() ? 0 : i;
//                    }
//                    //判断最新创建的文件是否可写,这里可以判断文件大小，是否需要重新创建
//                    if (!files[k].canWrite()) {
//                        path = CreateFile();
//                    } else {
//                        path = files[k].getAbsolutePath();
//                    }
//                }
//            }
//            if (!TextUtils.isEmpty(path)) {
//                out = new OutputStreamWriter(new FileOutputStream(path, true), "GBK");
//                BufferedWriter writer = new BufferedWriter(out);
//                writer.write(content);
//                writer.write("\n");
//                writer.flush();
//                writer.close();
//                out.close();
//                return true;
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("IOException occurred. ", e);
//        } finally {
//            if (out != null) {
//                try {
//                    out.close();
//                } catch (IOException e) {
//                    throw new RuntimeException("IOException occurred. ", e);
//                }
//            }
//        }
        return false;
    }

    //创建文件夹及文件 ,返回文件路径
    public static String CreateFile() throws IOException {
        File file = new File(LOGGER_SDPATH);
        if (!file.exists()) {
            try {
                //按照指定的路径创建文件夹
                file.mkdirs();
            } catch (Exception e) {
                return "";
            }
        }
        File dir = new File(LOGGER_SDPATH + System.currentTimeMillis() + "hl_log.log");
        if (!dir.exists()) {
            try {
                //在指定的文件夹中创建文件
                dir.createNewFile();
            } catch (Exception e) {
                return "";
            }
        }
        return dir.getAbsolutePath();
    }

    /**
     * 判断文件夹是否为空
     *
     * @param
     * @return
     */
    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isDirectory()) {
            if (null != file.listFiles() && file.listFiles().length > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除文件
     *
     * @param fileName
     */
    public static void delLoggerFile(String filePath) {
        File file = new File(filePath);
        if (file == null || !file.exists() || file.isDirectory())
            return;
        if (file.isFile()) {
            file.delete();
        }
        file.exists();
    }

    public static String getConstantInfo(Context context) {
        String userSid = RenheApplication.getInstance().getUserInfo() == null ? ""
                : RenheApplication.getInstance().getUserInfo().getSid();
        String info = "|1|" + System.currentTimeMillis() + "|" + RenheApplication.getInstance().getDeviceIMEI() + "|"
                + RenheApplication.getInstance().getDeviceIMSI() + "|" + userSid + "|" + ManifestUtil.getUmengChannel(context)
                + "|" + NetworkUtil.getNetworkType(context) + "|"
                + (TextUtils.isEmpty(RenheService.cityName) ? "" : RenheService.cityName);
        return info;
    }
}

package com.itcalf.renhe.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.dto.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

/**
 * Title: CheckUpdateUtil.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2015    <br>
 * Create DateTime: 2015-1-30 上午10:52:58 <br>
 *
 * @author wangning
 */
public class CheckUpdateUtil {

    private Context context;
    private Notification notification;
    private NotificationManager notificationManager;
    private int DOWNLOAD_ID = 1;
    private int icon;
    //下载块大小
    private final static int DOWNLOAD_FILE_SIZE = 1024 * 10; // 下载块大小：1K

    public CheckUpdateUtil(Context context) {
        this.context = context;
    }

    public void checkUpdate(final boolean flag) {
        // 异步线程去检查版本更新
        new AsyncTask<Void, Void, Version>() {

            @Override
            protected Version doInBackground(Void... params) {
                try {
                    return RenheApplication.getInstance().getPhoneCommand().getLastedVersion(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(final Version result) {
                super.onPostExecute(result);
                if (null != result && 1 == result.getState()) {
//                    String ver = ManifestUtil.getVersionName(context);
                    if (null != result.getVersion()) {
                        // 比较系统版本，如果有新版本提示是否更新
                        if (checkIfNeedUpdate(result.getVersion())) {
                            MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(context);
                            materialDialogsUtil.getBuilder("版本更新(V" + result.getVersion() + ")", result.getUpdatelog(),
                                    R.string.version_update_sure, R.string.material_dialog_cancel)
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            update(result.getNewVersionDownloadUrl());
                                        }

                                        @Override
                                        public void onNeutral(MaterialDialog dialog) {
                                        }

                                        @Override
                                        public void onNegative(MaterialDialog dialog) {
                                            if (result.getForceUpdate() == 1) {
                                                TabMainFragmentActivity.exitApp(context);
                                            }
                                        }
                                    }).cancelable(result.getForceUpdate() != 1);
                            materialDialogsUtil.show();
                        } else {
                            if (flag)
                                ToastUtil.showToast(context, context.getResources().getString(R.string.app_current_version));
                            // finish();
                        }
                    }
                } else {
                    ToastUtil.showErrorToast(context, context.getResources().getString(R.string.network_error_message));
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool());
    }

    /**
     * 根据服务端返回的versionName以及本地的versionCode来判断要不要更新
     * 本地versionCode命名要按照指定的规范，比如
     * eg1：版本VersionName是5.5.0，versionCode就是50500
     * eg2：版本VersionName是5.5.10，versionCode就是50510
     * eg2：版本VersionName是10.5.10，versionCode就是100510
     * 将服务端返回的versionName转换为相应的versionCode（将小数点"."替换为"0"）
     *
     * @param newVersionName 服务端返回的versionName
     * @return
     */
    private boolean checkIfNeedUpdate(String newVersionName) {
        boolean flag = false;
        if (!TextUtils.isEmpty(newVersionName)) {
            String newVersionCodeString = newVersionName.replace(".", "0");
            try {
                int newVersionCode = Integer.parseInt(newVersionCodeString);
                int localVersionCode = ManifestUtil.getVersionCode(context);
                if (newVersionCode > localVersionCode) {
                    flag = true;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    private void update(final String url) {
        if (-1 != NetworkUtil.hasNetworkConnection(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startDownload(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            ToastUtil.showNetworkError(context);
        }
    }

    /**
     * 启动新版本下载
     *
     * @param fileUrl 下载地址
     */
    private void startDownload(String fileUrl) {
        HttpURLConnection urlConn = null;
        InputStream inputStream = null;
        FileOutputStream fileOutput = null;
        try {
            downloadNotification();
            URL url = new URL(fileUrl);
            urlConn = (HttpURLConnection) url.openConnection();
            File file = new File(Environment.getExternalStorageDirectory(), "renhe.apk");
            fileOutput = new FileOutputStream(file);
            inputStream = urlConn.getInputStream();
            byte[] buffer = new byte[DOWNLOAD_FILE_SIZE];
            long length = urlConn.getContentLength();
            long downSize = 0;
            float totalSize = length;
            int percent = 0;
            do {
                int numread = inputStream.read(buffer);
                if (numread == -1) {
                    break;
                }
                fileOutput.write(buffer, 0, numread);
                downSize += numread;
                int nowPercent = (int) ((downSize / totalSize) * 100);
                // 如果百分比有变动则更新进度条
                if (nowPercent > percent) {
                    percent = nowPercent;
                    // 更新
                    updateProgressBar(percent);
                }

            } while (true);
            fileOutput.flush();
            // 完成
            updateProgressBar(0);
        } catch (Exception e) {
            // 异常取消
            updateProgressBar(-1);
            e.printStackTrace();
        } finally {
            try {
                fileOutput.close();
                inputStream.close();
                fileOutput = null;
                inputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 下载进度通知
     */
    private void downloadNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        notificationManager = (NotificationManager) context.getSystemService(ns);
        icon = android.R.drawable.stat_sys_download;
        // the text that appears first on the status bar
        String tickerText = "和聊下载中...";
        long time = System.currentTimeMillis();
        notification = new Notification(icon, tickerText, time);
        // the text that needs to change
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        notificationIntent.setType("audio/*");
        notification.contentView = new RemoteViews(context.getPackageName(), R.layout.versionupdate);
        notification.contentView.setTextViewText(R.id.downloadText, tickerText);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;
        notificationManager.notify(DOWNLOAD_ID, notification);

    }

    /**
     * 更新进度条状态
     *
     * @param percent
     */
    private void updateProgressBar(int percent) {
        switch (percent) {
            case 0:
                notificationManager.cancel(DOWNLOAD_ID);
                openFile(context, new File(Environment.getExternalStorageDirectory(), "renhe.apk"));
                break;
            case -1:
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                notification.icon = android.R.drawable.stat_notify_error;
                notification.contentView.setImageViewResource(R.id.downloadImg, android.R.drawable.stat_sys_warning);
                notification.contentView.setTextViewText(R.id.downloadText, "网络异常，停止下载!");
                notification.contentView.setTextColor(R.id.downloadText, Color.RED);
                notificationManager.notify(DOWNLOAD_ID, notification);
                break;
            case 100:
                notification.contentView.setProgressBar(R.id.downloadProgress, 100, percent, false);
                notification.contentView.setTextViewText(R.id.percetText, percent + "%");
                notification.contentView.setTextViewText(R.id.percetText, "下载完成");
                notificationManager.notify(DOWNLOAD_ID, notification);
                break;
            default:
                notification.contentView.setProgressBar(R.id.downloadProgress, 100, percent, false);
                notification.contentView.setTextViewText(R.id.percetText, percent + "%");
                notificationManager.notify(DOWNLOAD_ID, notification);
                break;
        }
    }

    /**
     * 下载完成后打开文件，更新应用。
     *
     * @param act
     * @param f
     */
    private void openFile(Context act, File f) {
        if (f.isFile()) {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            /* 调用getMIMEType()来取得MimeType */
            String type = getMIMEType(f);
            /* 设置intent的file与MimeType */
            intent.setDataAndType(Uri.fromFile(f), type);
            act.startActivity(intent);
        }
    }

    /* 判断文件MimeType的method */
    private static String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
        /* 取得扩展名 */
        String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
        /* 依扩展名的类型决定MimeType */
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") || end.equals("ogg")
                || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
            type = "image";
        } else if (end.equals("apk")) {
            /* android.permission.INSTALL_PACKAGES */
            type = "application/vnd.android.package-archive";
        } else {
            type = "*";
        }
        /* 如果无法直接打开，就跳出软件列表给用户选择 */
        if (end.equals("apk")) {
        } else {
            type += "/*";
        }
        return type;
    }
}

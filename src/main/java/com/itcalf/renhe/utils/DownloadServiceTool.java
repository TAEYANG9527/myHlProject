package com.itcalf.renhe.utils;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class DownloadServiceTool {
	private static final String TAG = "DownloadService";

	private Context mContext;
	private Map<String, com.itcalf.renhe.utils.ApkDownload> apkDownloadMap;

	public DownloadServiceTool(Context mContext) {
		super();
		this.mContext = mContext;

		create();
	}

	public void create() {

		apkDownloadMap = new HashMap<String, com.itcalf.renhe.utils.ApkDownload>();
	}

	public void startDownload(DownLoadFile downLoadFile) {
		com.itcalf.renhe.utils.ApkDownload apkDownload = apkDownloadMap.get(downLoadFile.getUrl());
		if (apkDownload == null) {
			apkDownload = new com.itcalf.renhe.utils.ApkDownload(this, mContext, downLoadFile);
			apkDownload.start();
			apkDownloadMap.put(downLoadFile.getUrl(), apkDownload);
		}
	}

	public void stop(DownLoadFile downLoadFile) {
		com.itcalf.renhe.utils.ApkDownload apkDownload = apkDownloadMap.get(downLoadFile.getUrl());
		if (apkDownload != null) {
			apkDownload.stop();
			remove(downLoadFile.getUrl());
		}
	}

	public void remove(String fileUrl) {
		com.itcalf.renhe.utils.ApkDownload apkDownload = apkDownloadMap.get(fileUrl);
		if (apkDownload != null) {
			apkDownload.cancle();
			apkDownloadMap.remove(fileUrl);
			apkDownload.stop();
		}

	}
}

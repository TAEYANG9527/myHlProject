package com.itcalf.renhe.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.itcalf.renhe.Constants;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class ApkDownload extends FileDownLoadUtil {
	private static final String TAG = "ApkDownload";
	private DownloadServiceTool downloadServiceTool;
	private DownLoadFile downLoadFile;
	private Context mContext;

	private int mFileSize;
	private int mStart;
	private String mFileUrl;

	public ApkDownload(DownloadServiceTool downloadServiceTool, Context mContext, DownLoadFile downLoadFile) {
		super();
		this.mContext = mContext;
		this.downLoadFile = downLoadFile;
		this.downloadServiceTool = downloadServiceTool;
		setResume(true);
		setNoticeBitSize(1024);
	}

	@Override
	protected void onStart(int filesize, int startLoaction) {
		this.mFileSize = filesize + startLoaction;
		this.mStart = startLoaction;
		downLoadFile.setState(Constants.FILE_DOWNLOAD_STATE.DOWNLOAD_STATE_DOWNLOADING);
	}

	@Override
	protected void onWorking(int size) {
	}

	@Override
	protected void onWorking(int size, int startLoaction) {

		this.mStart = startLoaction + size;

		if (downLoadFile != null && null != downLoadFile.getProgressBar()) {
			if (Constants.DEBUG_MODE) {
				Log.d("文件接收**total**", mFileSize + "");
				Log.d("文件接收**已接收**", mStart + "");
			}
			downLoadFile.getProgressBar().setMax(mFileSize);
			downLoadFile.getProgressBar().setProgress(mStart);
			if (null != downLoadFile.getDownLoadTv()) {
				downLoadFile.getDownLoadTv().setVisibility(View.VISIBLE);
				downLoadFile.getDownLoadTv().setText("正在下载...(" + FileSizeUtil.FormetFileSize((long) mStart) + "/"
						+ FileSizeUtil.FormetFileSize((long) mFileSize) + ")");
			}
		}

	}

	@Override
	protected void onEnd(int size) {
		//修改状态，下载完毕
		downloadServiceTool.remove(mFileUrl);
		downLoadFile.setState(Constants.FILE_DOWNLOAD_STATE.DOWNLOAD_STATE_SUCCESS);
		if (null != downLoadFile.getDownLoadTv()) {
			downLoadFile.getDownLoadTv().setVisibility(View.VISIBLE);
			downLoadFile.getDownLoadTv().setText("下载完成");
		}
	}

	@Override
	protected void onError(int size) {
		downLoadFile.setState(Constants.FILE_DOWNLOAD_STATE.DOWNLOAD_STATE_WAITTIGN);
		Toast.makeText(mContext, "下载失败，请检查网络后重试", Toast.LENGTH_SHORT).show();
		downloadServiceTool.remove(mFileUrl);
		if (null != downLoadFile.getDownLoadTv()) {
			downLoadFile.getDownLoadTv().setVisibility(View.VISIBLE);
			downLoadFile.getDownLoadTv().setText("下载失败");
		}
	}

	@Override
	protected void onStop(int size) {
		downLoadFile.setState(Constants.FILE_DOWNLOAD_STATE.DOWNLOAD_STATE_WAITTIGN);
		String filePath = downLoadFile.getName();
		filePath = filePath.substring(0, filePath.indexOf("."));
		filePath = filePath + ".temp";
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
		downloadServiceTool.remove(mFileUrl);
	}

	@Override
	protected void onPause(int size) {
		downLoadFile.setState(Constants.FILE_DOWNLOAD_STATE.DOWNLOAD_STATE_WAITTIGN);
		downloadServiceTool.remove(mFileUrl);
	}

	@Override
	protected void cancle() {
		super.cancle();
	}

	public void start() {
		mFileUrl = downLoadFile.getUrl();//下载路径
		String path = Constants.CACHE_PATH.IM_DOWNLOAD_PATH + downLoadFile.getId() + downLoadFile.getName();
		//        File file = new File(path);
		//        if(file.exists() && !file.isDirectory()){
		//            path =  Constants.DOWNLOAD_PATH + downLoadFile.getId() + downLoadFile.getName();
		//        }
		try {
			this.start(mFileUrl, path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

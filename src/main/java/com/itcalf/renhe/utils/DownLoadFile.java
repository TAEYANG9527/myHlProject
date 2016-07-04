package com.itcalf.renhe.utils;

import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by wangning on 2015/7/22.
 */
public class DownLoadFile {
	private String id;
	private String name;
	private String url;
	private long size;
	private ProgressBar progressBar;
	private TextView downLoadTv;
	private int state;//-1 等待下载 1 正在下载 2 下载完成

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public TextView getDownLoadTv() {
		return downLoadTv;
	}

	public void setDownLoadTv(TextView downLoadTv) {
		this.downLoadTv = downLoadTv;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}

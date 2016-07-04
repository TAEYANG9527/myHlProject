package com.itcalf.renhe.http;

public interface Callback<T> {
	/** Run in main thread */
	public void onSuccess(int type, T result);

	/** Run in main thread */
	public void onFailure(int type, String msg);

	/** Run in background thread */
	public void cacheData(int type, Object data);

}

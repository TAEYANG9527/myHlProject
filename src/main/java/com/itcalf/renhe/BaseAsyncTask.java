package com.itcalf.renhe;

import android.content.Context;
import android.os.AsyncTask;

public abstract class BaseAsyncTask<T> extends AsyncTask<String, Void, T> {
	protected Context mContext;

	@SuppressWarnings("unused")
	private BaseAsyncTask() {
		super();
	}

	public BaseAsyncTask(Context mContext) {
		super();
		this.mContext = mContext;
	}

	protected Context getContext() {
		return mContext;
	}

	protected RenheApplication getMyApplication() {
		return (RenheApplication) mContext.getApplicationContext();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		doPre();
	}

	@Override
	protected void onPostExecute(T result) {
		super.onPostExecute(result);
		doPost(result);
	}

	public abstract void doPre();

	public abstract void doPost(T result);
}

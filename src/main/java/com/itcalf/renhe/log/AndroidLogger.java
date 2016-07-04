package com.itcalf.renhe.log;

import android.util.Log;

/**
 * The logger implementation for Android.
 * <p>
 * This implementation can be used when the code integrating the library runs environment with the Android runtime.
 * </p>
 * 
 * @see LoggerFactory
 */
public class AndroidLogger implements Logger {

	private final String category;

	public AndroidLogger(String category) {
		this.category = category;
	}

	public AndroidLogger(Class<?> theClass) {
		this(theClass.getSimpleName());
	}

	public void verbose(String message) {
		Log.v(category, message);
	}

	public void debug(String message) {
		Log.d(category, message);
	}

	public void info(String message) {
		Log.i(category, message);
	}

	public void warn(String message) {
		Log.w(category, message);
	}

	public void warn(String message, Throwable throwable) {
		Log.w(category, message, throwable);
	}

	public void warn(StringBuffer message, Throwable throwable) {
		warn(message.toString(), throwable);
	}

	public void error(String message) {
		Log.e(category, message);
	}

	public void error(String message, Throwable throwable) {
		Log.e(category, message, throwable);
	}

	public void error(StringBuffer message, Throwable throwable) {
		error(message.toString(), throwable);
	}

	public void fatal(String message) {
		error(message);
	}

	public void fatal(String message, Throwable throwable) {
		error(message, throwable);
	}

	public boolean isDebugEnabled() {
		return LoggerFactory.logLevel <= Log.DEBUG;
	}

	public boolean isInfoEnabled() {
		return LoggerFactory.logLevel <= Log.INFO;
	}

	public boolean isWarnEnabled() {
		return LoggerFactory.logLevel <= Log.WARN;
	}

	public boolean isErrorEnabled() {
		return LoggerFactory.logLevel <= Log.ERROR;
	}

	public boolean isFatalEnabled() {
		return LoggerFactory.logLevel <= Log.ERROR;
	}

}

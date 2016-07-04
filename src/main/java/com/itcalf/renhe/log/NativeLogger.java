package com.itcalf.renhe.log;

import android.util.Log;

/**
 * An implementation which uses the Java standard output and error streams.
 * <p>
 * This implementation can be used when the code integrating the library needs to run on an environment with no Android
 * runtime available.
 * </p>
 * 
 * @see LoggerFactory
 */
public class NativeLogger implements Logger {

	private final String prefix;

	public NativeLogger(String category) {
		this.prefix = "[" + category + "] ";
	}

	public NativeLogger(Class<?> theClass) {
		this(theClass.getSimpleName());
	}

	protected final String getPrefix() {
		return "[" + System.currentTimeMillis() + "] " + prefix + " [" + Thread.currentThread().getName() + "] ";
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

	public void verbose(String message) {
		System.out.println(getPrefix() + "[V] " + message);
	}

	public void debug(String message) {
		System.out.println(getPrefix() + "[D] " + message);
	}

	public void error(String message) {
		System.err.println(getPrefix() + "[E] " + message);
	}

	public void error(String message, Throwable throwable) {
		System.err.println(getPrefix() + "[E] " + message);
		throwable.printStackTrace(System.err);
	}

	public void error(StringBuffer message, Throwable throwable) {
		System.err.println(getPrefix() + message);
		throwable.printStackTrace(System.err);
	}

	public void fatal(String message) {
		System.err.println(getPrefix() + "[F] " + message);
	}

	public void fatal(String message, Throwable throwable) {
		System.err.println(getPrefix() + "[F] " + message);
		throwable.printStackTrace(System.err);
	}

	public void info(String message) {
		System.out.println(getPrefix() + "[I] " + message);
	}

	public void warn(String message) {
		System.out.println(getPrefix() + "[W] " + message);
	}

	public void warn(String message, Throwable throwable) {
		System.out.println(getPrefix() + "[W] " + message);
		throwable.printStackTrace(System.out);
	}

	public void warn(StringBuffer message, Throwable throwable) {
		System.out.println(getPrefix() + "[W] " + message);
		throwable.printStackTrace(System.out);
	}

}

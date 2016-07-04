package com.itcalf.renhe.log;

/**
 * Just in order to have various loggers.
 */
public interface Logger {

	void verbose(String message);

	void debug(String message);

	void info(String message);

	void warn(String message);

	void warn(String message, Throwable throwable);

	void warn(StringBuffer append, Throwable throwable);

	void error(String message);

	void error(String message, Throwable throwable);

	void error(StringBuffer message, Throwable throwable);

	void fatal(String message);

	void fatal(String message, Throwable throwable);

	boolean isDebugEnabled();

	boolean isInfoEnabled();

	boolean isWarnEnabled();

	boolean isErrorEnabled();

	boolean isFatalEnabled();

}

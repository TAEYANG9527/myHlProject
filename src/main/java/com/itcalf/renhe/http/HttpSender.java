package com.itcalf.renhe.http;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

/**
 * HTTP Request sender
 *
 * @author YuZhiQiang
 */
public interface HttpSender {

    /**
     * sends HTTP request by get method
     *
     * @param id
     * @param url
     * @param params
     * @param headers
     * @param clazz
     */
    public <T> void get(int id, String url, Map<String, Serializable> params, Map<String, Serializable> headers, Class<T> clazz);

    /**
     * sends HTTP request by get method
     *
     * @param id
     * @param url
     * @param params
     * @param headers
     */
    public void get(int id, String url, Map<String, Serializable> params, Map<String, Serializable> headers);

    /**
     * sends HTTP request by get method
     *
     * @param id
     * @param url
     * @param params
     * @param clazz
     */
    public <T> void get(int id, String url, Map<String, Serializable> params, Class<T> clazz);

    /**
     * sends HTTP request by get method
     *
     * @param id
     * @param url
     * @param clazz
     */
    public <T> void get(int id, String url, Class<T> clazz);

    /**
     * sends HTTP request by get method
     *
     * @param id
     * @param url
     */
    public void get(int id, String url);

    /**
     * sends HTTP request by post method
     *
     * @param id
     * @param url
     * @param params
     * @param headers
     * @param clazz
     */
    public <T> void post(int id, String url, Map<String, Serializable> params, Map<String, Serializable> headers, Class<T> clazz);

    /**
     * sends HTTP request by post method
     *
     * @param id
     * @param url
     * @param params
     * @param headers
     */
    public void post(int id, String url, Map<String, Serializable> params, Map<String, Serializable> headers);

    /**
     * sends HTTP request by post method
     *
     * @param id
     * @param url
     * @param params
     * @param clazz
     */
    public <T> void post(int id, String url, Map<String, Serializable> params, Class<T> clazz);

    /**
     * sends HTTP request by post method
     *
     * @param id
     * @param url
     * @param params
     */
    public void post(int id, String url, Map<String, Serializable> params);

    /**
     * 上传文件（支持多文件、多参数）
     *
     * @param id
     * @param url
     * @param params           可以放文件、form表单参数
     * @param progressListener
     * @param clazz
     * @param <T>
     */
    public <T> void uploadFilesWithProgress(int id, String url, Map<String, Serializable> params,
                                            CountingRequestBody.ProgressListener progressListener, Class<T> clazz);

    /**
     * download file
     *
     * @param id
     * @param url
     * @param file
     * @param progressListener
     */
    public void downloadFile(int id, String url, File file, CountingRequestBody.ProgressListener progressListener);

    /**
     * cancel the request
     *
     * @param tag request's category. You can cancel all request of the tag
     */
    public void cancel(Object tag);
}

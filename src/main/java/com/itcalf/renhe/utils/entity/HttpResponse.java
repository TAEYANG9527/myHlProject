//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.itcalf.renhe.utils.entity;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private String url;
    private String responseBody;
    private Map<String, Object> responseHeaders;
    private int type;
    private long expiredTime;
    private boolean isInCache;
    private boolean isInitExpiredTime;
    private int responseCode = -1;

    public HttpResponse(String url) {
        this.url = url;
        this.type = 0;
        this.isInCache = false;
        this.isInitExpiredTime = false;
        this.responseHeaders = new HashMap();
    }

    public HttpResponse() {
        this.responseHeaders = new HashMap();
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    private Map<String, Object> getResponseHeaders() {
        return this.responseHeaders;
    }

    public void setResponseHeaders(Map<String, Object> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        if(type < 0) {
            throw new IllegalArgumentException("The type of HttpResponse cannot be smaller than 0.");
        } else {
            this.type = type;
        }
    }

    public HttpResponse setInCache(boolean isInCache) {
        this.isInCache = isInCache;
        return this;
    }

    public String getExpiresHeader() {
        try {
            return this.responseHeaders == null?null:(String)this.responseHeaders.get("expires");
        } catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }

    private int getCacheControlMaxAge() {
        try {
            String e = (String)this.responseHeaders.get("cache-control");
            if(!TextUtils.isEmpty(e)) {
                int start = e.indexOf("max-age=");
                if(start != -1) {
                    int end = e.indexOf(",", start);
                    String maxAge;
                    if(end != -1) {
                        maxAge = e.substring(start + "max-age=".length(), end);
                    } else {
                        maxAge = e.substring(start + "max-age=".length());
                    }

                    return Integer.parseInt(maxAge);
                }
            }

            return -1;
        } catch (Exception var5) {
            var5.printStackTrace();
            return -1;
        }
    }


    public void setResponseHeader(String field, String newValue) {
        if(this.responseHeaders != null) {
            this.responseHeaders.put(field, newValue);
        }

    }

    private Object getResponseHeader(String field) {
        return this.responseHeaders == null?null:this.responseHeaders.get(field);
    }
}

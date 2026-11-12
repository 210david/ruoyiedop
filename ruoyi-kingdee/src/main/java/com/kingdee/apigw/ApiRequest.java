package com.kingdee.apigw;

import java.util.HashMap;
import java.util.Map;

/**
 * API 请求对象（与官方 SDK 类名完全一致）
 */
public class ApiRequest
{
    private HttpMethod method;
    private String domain;
    private String path;
    private Map<String, String> querys = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private byte[] bodyJson;
    private String timeStamp;
    private String nonce;

    public ApiRequest(HttpMethod method, String domain, String path)
    {
        this.method = method;
        this.domain = domain;
        this.path = path;
    }

    public HttpMethod getMethod() { return method; }
    public void setMethod(HttpMethod method) { this.method = method; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Map<String, String> getQuerys() { return querys; }
    public void setQuerys(Map<String, String> querys) { this.querys = querys; }

    public void addQuery(String key, String value) { this.querys.put(key, value); }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public void addHeaders(Map<String, String> headers) { this.headers.putAll(headers); }
    public void addHeader(String key, String value) { this.headers.put(key, value); }

    public byte[] getBodyJson() { return bodyJson; }
    public void setBodyJson(byte[] bodyJson) { this.bodyJson = bodyJson; }

    public String getTimeStamp() { return timeStamp; }
    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }
}

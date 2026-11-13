package com.kingdee.apigw;

import java.util.Map;

/**
 * API 响应对象（与官方 SDK 类名完全一致）
 */
public class ApiResult
{
    private int httpCode;
    private String body;
    private Map<String, String> headers;

    public ApiResult() {}

    public ApiResult(int httpCode, String body, Map<String, String> headers)
    {
        this.httpCode = httpCode;
        this.body = body;
        this.headers = headers;
    }

    public int getHttpCode() { return httpCode; }
    public void setHttpCode(int httpCode) { this.httpCode = httpCode; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public boolean isSuccess() { return httpCode >= 200 && httpCode < 300; }

    @Override
    public String toString()
    {
        return "ApiResult{httpCode=" + httpCode + ", body=" + (body == null ? "null" : body.length() + " chars") + "}";
    }
}

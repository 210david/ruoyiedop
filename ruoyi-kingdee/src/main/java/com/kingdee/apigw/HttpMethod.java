package com.kingdee.apigw;

/**
 * HTTP 方法枚举（与官方 SDK 保持一致）
 */
public enum HttpMethod
{
    GET,
    POST,
    /** POST 请求带 JSON Body — 官方 SDK 专用名 */
    POST_BODY,
    PUT,
    DELETE
}

package com.ruoyi.kingdee.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

public final class KingdeeSignUtil
{
    public static final String SIGN_HEADERS = "X-Api-TimeStamp,X-Api-Nonce";
    private static final Logger log = LoggerFactory.getLogger(KingdeeSignUtil.class);

    private KingdeeSignUtil() {}

    public static String sign(String clientSecret, String method, String path,
                              Map<String, String> params, String nonce, String timeStamp)
    {
        String seg1 = method.toUpperCase();
        String seg2 = urlEncodeOnce(path);
        String seg3 = buildParamsDoubleEncoded(params);
        String seg4 = "x-api-nonce:" + nonce;
        String seg5 = "x-api-timestamp:" + timeStamp;

        String signStr = seg1 + "\n" + seg2 + "\n" + seg3 + "\n" + seg4 + "\n" + seg5 + "\n";

        // DEBUG 日志
        if (log.isDebugEnabled())
        {
            log.debug("=== 金蝶签名原文 ===");
            log.debug("seg1(method): [{}]", seg1);
            log.debug("seg2(path)  : [{}]", seg2);
            log.debug("seg3(params): [{}]", seg3);
            log.debug("seg4(nonce) : [{}]", seg4);
            log.debug("seg5(ts)    : [{}]", seg5);
            log.debug("full(esc): {}", signStr.replace("\n", "\\n"));
        }

        byte[] raw = hmacSha256(clientSecret, signStr);
        String hex = bytesToHex(raw);
        String signature = Base64.getEncoder().encodeToString(hex.getBytes(StandardCharsets.UTF_8));

        log.info("金蝶签名 path={} method={} sig={}", path, method, signature);
        return signature;
    }

    public static String appSignature(String appSecret, String appKey)
    {
        byte[] raw = hmacSha256(appSecret, appKey);
        String hex = bytesToHex(raw);
        return Base64.getEncoder().encodeToString(hex.getBytes(StandardCharsets.UTF_8));
    }

    private static String buildParamsDoubleEncoded(Map<String, String> params)
    {
        if (params == null || params.isEmpty()) return "";
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet())
        {
            if (sb.length() > 0) sb.append('&');
            sb.append(doubleUrlEncode(e.getKey()));
            sb.append('=');
            sb.append(doubleUrlEncode(e.getValue() == null ? "" : e.getValue()));
        }
        return sb.toString();
    }

    private static String doubleUrlEncode(String s) { return urlEncodeOnce(urlEncodeOnce(s)); }

    private static String urlEncodeOnce(String s)
    {
        try { return URLEncoder.encode(s, "UTF-8").replace("+", "%2B"); }
        catch (Exception e) { return s; }
    }

    private static byte[] hmacSha256(String secret, String data)
    {
        try
        {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("HMAC-SHA256 加密失败: " + e.getMessage(), e);
        }
    }

    private static String bytesToHex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}

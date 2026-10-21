package com.ruoyi.framework.interceptor;

import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * TraceId拦截器，为每个请求生成唯一跟踪ID
 * 
 * @author ruoyi
 */
@Component
public class TraceIdInterceptor implements HandlerInterceptor
{
    /** TraceId的MDC键名 */
    public static final String TRACE_ID_KEY = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        // 为每个请求生成唯一的TraceId并放入MDC
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(TRACE_ID_KEY, traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception
    {
        // 请求处理完成后清除TraceId
        MDC.remove(TRACE_ID_KEY);
    }
}

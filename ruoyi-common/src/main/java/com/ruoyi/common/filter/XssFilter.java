package com.ruoyi.common.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.html.EscapeUtil;
import com.ruoyi.common.enums.HttpMethod;

/**
 * 防止XSS攻击的过滤器
 * 
 * @author ruoyi
 */
public class XssFilter implements Filter
{
    /**
     * 排除链接
     */
    public List<String> excludes = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        String tempExcludes = filterConfig.getInitParameter("excludes");
        if (StringUtils.isNotEmpty(tempExcludes))
        {
            String[] urls = tempExcludes.split(",");
            for (String url : urls)
            {
                excludes.add(url);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (handleExcludeURL(req, resp))
        {
            chain.doFilter(request, response);
            return;
        }
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }

    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response)
    {
        String url = request.getServletPath();
        String method = request.getMethod();
        // GET DELETE 不过滤
        if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method))
        {
            return true;
        }
        // PUT请求也需要进行XSS过滤
        return StringUtils.matches(url, excludes);
    }

    /**
     * 对请求头进行XSS过滤检查
     */
    private boolean hasXssInHeaders(HttpServletRequest request)
    {
        // 检查常见可能包含注入的请求头
        String[] headerNames = { "Referer", "User-Agent", "Origin", "X-Forwarded-For" };
        for (String headerName : headerNames)
        {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null)
            {
                String cleaned = EscapeUtil.clean(headerValue);
                if (!cleaned.equals(headerValue))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void destroy()
    {

    }
}
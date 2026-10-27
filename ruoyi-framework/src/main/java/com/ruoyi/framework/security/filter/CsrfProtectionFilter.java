package com.ruoyi.framework.security.filter;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CSRF防护过滤器
 * 对POST/PUT/DELETE等写操作请求验证CSRF Token
 *
 * @author ruoyi
 */
public class CsrfProtectionFilter implements Filter
{
    @Value("${token.header}")
    private String tokenHeader;

    @Value("${token.secret}")
    private String secret;

    @Autowired
    private RedisCache redisCache;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();

        // GET、HEAD、OPTIONS请求跳过CSRF检查
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method))
        {
            chain.doFilter(request, response);
            return;
        }

        // 登录请求跳过CSRF检查
        String uri = httpRequest.getRequestURI();
        if (uri != null && (uri.endsWith("/login") || uri.endsWith("/csrf-token")))
        {
            chain.doFilter(request, response);
            return;
        }

        // 获取用户令牌中的UUID
        String token = getToken(httpRequest);
        if (StringUtils.isEmpty(token))
        {
            // 未携带令牌，继续执行（由Spring Security处理认证）
            chain.doFilter(request, response);
            return;
        }

        try
        {
            // 解析JWT获取用户UUID
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);

            if (StringUtils.isNotEmpty(uuid))
            {
                // 从请求头获取CSRF Token
                String csrfToken = httpRequest.getHeader("X-CSRF-TOKEN");

                if (StringUtils.isEmpty(csrfToken))
                {
                    sendError(httpResponse, "缺少CSRF Token");
                    return;
                }

                // 从Redis获取用户存储的CSRF Token
                String csrfKey = CacheConstants.CSRF_TOKEN_KEY + uuid;
                String storedToken = redisCache.getCacheObject(csrfKey);

                if (StringUtils.isEmpty(storedToken) || !storedToken.equals(csrfToken))
                {
                    sendError(httpResponse, "CSRF Token验证失败");
                    return;
                }
            }
        }
        catch (Exception e)
        {
            // JWT解析失败，继续执行（由Spring Security处理）
        }

        chain.doFilter(request, response);
    }

    /**
     * 获取请求中的Token
     */
    private String getToken(HttpServletRequest request)
    {
        String token = request.getHeader(tokenHeader);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    /**
     * 返回403错误
     */
    private void sendError(HttpServletResponse response, String message) throws IOException
    {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"msg\":\"" + message + "\"}");
    }
}

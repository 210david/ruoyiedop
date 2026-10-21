package com.ruoyi.framework.interceptor;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.OpenApiApp;
import com.ruoyi.system.service.IOpenApiAppService;

/**
 * 开放接口API Key校验拦截器
 * 拦截 /openapi/** 路径，校验请求头 X-API-Key
 *
 * @author ruoyi
 */
@Component
public class OpenApiInterceptor implements HandlerInterceptor
{
    @Autowired
    private IOpenApiAppService openApiAppService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException
    {
        // 从请求头获取 API Key
        String apiKey = request.getHeader("X-API-Key");
        if (StringUtils.isEmpty(apiKey))
        {
            renderError(response, HttpStatus.UNAUTHORIZED, "缺少API Key，请在请求头中传递 X-API-Key");
            return false;
        }

        // 校验 API Key
        OpenApiApp app = openApiAppService.selectOpenApiAppByKey(apiKey);
        if (app == null)
        {
            renderError(response, HttpStatus.UNAUTHORIZED, "无效的API Key或应用已停用");
            return false;
        }

        // 校验模块权限
        String requestURI = request.getRequestURI();
        String module = extractModule(requestURI);
        if (StringUtils.isNotEmpty(module) && !"*".equals(app.getAllowedModules()))
        {
            String[] allowed = app.getAllowedModules().split(",");
            boolean hasPermission = false;
            for (String m : allowed)
            {
                if (m.trim().equalsIgnoreCase(module))
                {
                    hasPermission = true;
                    break;
                }
            }
            if (!hasPermission)
            {
                renderError(response, HttpStatus.FORBIDDEN, "该应用无权访问模块：" + module);
                return false;
            }
        }

        // 将应用信息存入请求属性，供Controller使用
        request.setAttribute("openApiApp", app);
        return true;
    }

    /**
     * 从URI中提取模块名
     * /openapi/v1/wms/material/list → wms
     * /openapi/v1/dms/equipment/list → dms
     */
    private String extractModule(String uri)
    {
        if (uri == null) return null;
        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length - 1; i++)
        {
            if ("v1".equals(parts[i]) && i + 1 < parts.length)
            {
                return parts[i + 1];
            }
        }
        return null;
    }

    private void renderError(HttpServletResponse response, int code, String msg) throws IOException
    {
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, msg)));
    }
}

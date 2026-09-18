package com.ruoyi.ai.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.ai.service.IAiNlQueryService;
import com.ruoyi.ai.service.IAiToolRouteService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * NL2Query 自然语言查数 Controller
 * 安全：可查范围按登录人权限动态裁剪、只读SELECT、强制LIMIT（详见 AiNlQueryServiceImpl）
 *
 * 方案3（Text-to-API）增强：先走工具路由（高频问题秒回），未命中降级NL2SQL
 */
@RestController
@RequestMapping("/ai/nlquery")
public class AiNlQueryController extends BaseController
{
    @Autowired
    private IAiNlQueryService nlQueryService;

    @Autowired
    private IAiToolRouteService toolRouteService;

    /**
     * 自然语言查询（管理层权限）：先工具直答，未命中NL2SQL兜底
     */
    @PreAuthorize("@ss.hasPermi('ai:nlquery:query')")
    @PostMapping("/query")
    public AjaxResult query(@RequestBody Map<String, String> body)
    {
        return AjaxResult.success(toolRouteService.query(body.get("question")));
    }
}

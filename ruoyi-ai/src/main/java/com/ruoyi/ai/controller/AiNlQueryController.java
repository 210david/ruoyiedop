package com.ruoyi.ai.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.ai.service.IAiNlQueryService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * NL2Query 自然语言查数 Controller
 * 安全：仅白名单表、只读SELECT、强制LIMIT（详见 AiNlQueryServiceImpl）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ai/nlquery")
public class AiNlQueryController extends BaseController
{
    @Autowired
    private IAiNlQueryService nlQueryService;

    /**
     * 自然语言查询（管理层权限）
     */
    @PreAuthorize("@ss.hasPermi('ai:nlquery:query')")
    @PostMapping("/query")
    public AjaxResult query(@RequestBody Map<String, String> body)
    {
        return AjaxResult.success(nlQueryService.query(body.get("question")));
    }
}

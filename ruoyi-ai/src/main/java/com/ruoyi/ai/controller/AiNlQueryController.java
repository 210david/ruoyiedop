package com.ruoyi.ai.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.ai.domain.AiNlqHistory;
import com.ruoyi.ai.service.IAiNlQueryService;
import com.ruoyi.ai.service.IAiToolRouteService;
import com.ruoyi.ai.mapper.AiNlqHistoryMapper;
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

    @Autowired
    private AiNlqHistoryMapper historyMapper;

    /**
     * 自然语言查询（管理层权限）：先工具直答，未命中NL2SQL兜底
     * 完成后自动保存历史记录（按用户隔离，落库失败不影响查询结果）
     */
    @PreAuthorize("@ss.hasPermi('ai:nlquery:query')")
    @PostMapping("/query")
    public AjaxResult query(@RequestBody Map<String, String> body)
    {
        String question = body.get("question");
        if (question == null || question.trim().isEmpty())
        {
            return AjaxResult.error("请输入问题");
        }
        Map<String, Object> result = toolRouteService.query(question);
        // 异步落库失败不影响查询返回；保存历史按用户隔离，仅本人可查
        try
        {
            AiNlqHistory h = new AiNlqHistory();
            h.setUserId(getUserId());
            h.setUserName(getUsername());
            h.setQuestion(question.length() > 1000 ? question.substring(0, 1000) : question);
            Object summary = result.get("summary");
            h.setAnswer(summary == null ? "" : summary.toString());
            // 结果JSON：columns/rows/sql/emptyReason，供历史回放
            Map<String, Object> snapshot = new java.util.HashMap<>();
            snapshot.put("columns", result.get("columns"));
            snapshot.put("rows", result.get("rows"));
            snapshot.put("sql", result.get("sql"));
            snapshot.put("emptyReason", result.get("emptyReason"));
            h.setResultJson(com.alibaba.fastjson2.JSON.toJSONString(snapshot));
            h.setStatus(result.get("error") == null ? "0" : "1");
            historyMapper.insertHistory(h);
        }
        catch (Exception e)
        {
            logger.warn("保存数据查询历史失败（不影响查询）: {}", e.getMessage());
        }
        return AjaxResult.success(result);
    }

    /**
     * 查询本人数据查询历史（按用户隔离，每人只能看到自己的记录）
     */
    @PreAuthorize("@ss.hasPermi('ai:nlquery:query')")
    @GetMapping("/history")
    public AjaxResult history(@RequestParam(defaultValue = "20") int pageSize,
                              @RequestParam(defaultValue = "1") int pageNum,
                              @RequestParam(required = false) String question)
    {
        AiNlqHistory query = new AiNlqHistory();
        query.setUserId(getUserId());
        query.setQuestion(question);
        query.setPageSize(Math.min(Math.max(pageSize, 1), 100));
        query.setOffset((Math.max(pageNum, 1) - 1) * query.getPageSize());
        List<AiNlqHistory> list = historyMapper.selectHistoryByUser(query);
        return AjaxResult.success(list);
    }

    /**
     * 清空本人数据查询历史
     */
    @PreAuthorize("@ss.hasPermi('ai:nlquery:query')")
    @DeleteMapping("/history")
    public AjaxResult clearHistory()
    {
        return toAjax(historyMapper.deleteHistoryByUser(getUserId()));
    }
}

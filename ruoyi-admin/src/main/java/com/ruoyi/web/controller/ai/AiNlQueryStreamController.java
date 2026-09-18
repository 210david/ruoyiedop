package com.ruoyi.web.controller.ai;

import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.ai.service.IAiNlQueryService;
import com.ruoyi.ai.service.IAiToolRouteService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * NL2Query 流式查询 Controller（SSE）
 * 实时推送阶段进度：理解问题 → 检索数据表 → 生成SQL → 执行查询
 *
 * 方案3（Text-to-API）增强：先走工具路由，高频问题工具直答（秒回），
 * 未命中工具时降级原NL2SQL两阶段流程（长尾兜底）
 */
@RestController
@RequestMapping("/ai/nlquery")
public class AiNlQueryStreamController
{
    @Autowired
    private IAiNlQueryService nlQueryService;

    @Autowired
    private IAiToolRouteService toolRouteService;

    /**
     * 自然语言查询——流式进度（SSE）
     */
    @PreAuthorize("@ss.hasPermi('ai:nlquery:query')")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody Map<String, String> body)
    {
        String question = body.get("question");
        // 在请求线程内捕获登录人信息与权限（异步线程无安全上下文）
        Set<String> perms = SecurityUtils.getLoginUser().getPermissions();
        // 审计上下文透传：异步线程中AI调用审计落库操作人用（避免操作人为空）
        Long auditUserId;
        String auditUserName;
        try
        {
            auditUserId = SecurityUtils.getUserId();
            auditUserName = SecurityUtils.getUsername();
        }
        catch (Exception e)
        {
            auditUserId = null;
            auditUserName = null;
        }
        final Long auditUserIdFinal = auditUserId;
        final String auditUserNameFinal = auditUserName;
        String userInfo;
        try
        {
            com.ruoyi.common.core.domain.entity.SysUser u = SecurityUtils.getLoginUser().getUser();
            userInfo = "账号:" + u.getUserName() + "，昵称:" + u.getNickName()
                    + (u.getDept() != null ? "，部门:" + u.getDept().getDeptName() : "");
        }
        catch (Exception e)
        {
            userInfo = null;
        }
        final String userInfoFinal = userInfo;
        SseEmitter emitter = new SseEmitter(120_000L);

        new Thread(() -> {
            AiClient.setUserContext(auditUserIdFinal, auditUserNameFinal);
            try
            {
                send(emitter, "stage", "{\"stage\":\"understand\",\"status\":\"start\"}");
                send(emitter, "stage", "{\"stage\":\"understand\",\"status\":\"done\"}");
                // 方案3：先工具路由（高频问题秒回），未命中内部自动降级NL2SQL
                // 阶段回调实时推送真实进度（检索表/生成SQL/执行查询）
                Map<String, Object> result = toolRouteService.query(question, (name, status) -> {
                    send(emitter, "stage", "{\"stage\":\"" + name + "\",\"status\":\"" + status + "\"}");
                });
                send(emitter, "result", com.alibaba.fastjson2.JSON.toJSONString(result));
                emitter.complete();
            }
            catch (Exception e)
            {
                send(emitter, "result", com.alibaba.fastjson2.JSON.toJSONString(
                        java.util.Collections.singletonMap("error", "查询失败：" + e.getMessage())));
                emitter.complete();
            }
            finally
            {
                AiClient.clearUserContext();
            }
        }, "nl2query-stream").start();

        return emitter;
    }

    private void send(SseEmitter emitter, String event, String json)
    {
        try
        {
            emitter.send(SseEmitter.event().name(event).data(json, MediaType.APPLICATION_JSON));
        }
        catch (Exception ignore) { }
    }
}

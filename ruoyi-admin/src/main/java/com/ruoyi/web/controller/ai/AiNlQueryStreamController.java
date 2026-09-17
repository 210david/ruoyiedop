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
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.ai.service.IAiNlQueryService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * NL2Query 流式查询 Controller（SSE）
 * 实时推送阶段进度：理解问题 → 检索数据表 → 生成SQL → 执行查询
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ai/nlquery")
public class AiNlQueryStreamController
{
    @Autowired
    private IAiNlQueryService nlQueryService;

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
            try
            {
                send(emitter, "stage", "{\"stage\":\"understand\",\"status\":\"start\"}");
                send(emitter, "stage", "{\"stage\":\"understand\",\"status\":\"done\"}");
                Map<String, Object> result = nlQueryService.queryWithStages(question, perms, userInfoFinal, (stage, status) -> {
                    send(emitter, "stage", "{\"stage\":\"" + stage + "\",\"status\":\"" + status + "\"}");
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

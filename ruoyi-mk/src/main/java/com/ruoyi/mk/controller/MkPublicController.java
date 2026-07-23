package com.ruoyi.mk.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.LimitType;
import com.ruoyi.mk.domain.MkActivityParticipant;
import com.ruoyi.mk.service.IMkPublicService;

/**
 * 营销活动公开接口 Controller（P3-15: 报名链接）
 *
 * <p>安全设计：
 * <ul>
 *   <li>@Anonymous：自动注册到Security白名单，跳过JWT鉴权</li>
 *   <li>@RateLimiter：基于Redis的IP限流，防止接口被恶意刷请求</li>
 *   <li>所有接口仅暴露最小必要功能，不泄露内部数据</li>
 * </ul>
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/mk/public")
public class MkPublicController extends BaseController
{
    @Autowired
    private IMkPublicService mkPublicService;

    /**
     * 获取活动报名信息（公开接口）
     *
     * <p>限流策略：同一IP每60秒最多30次请求
     */
    @RateLimiter(time = 60, count = 30, limitType = LimitType.IP)
    @GetMapping("/activity/{activityId}")
    public AjaxResult getActivityInfo(@PathVariable Long activityId, @RequestParam String token)
    {
        Map<String, Object> info = mkPublicService.getPublicActivityInfo(activityId, token);
        return AjaxResult.success(info);
    }

    /**
     * 提交报名（公开接口）
     *
     * <p>限流策略：同一IP每60秒最多5次报名请求（防止恶意批量报名）
     */
    @RateLimiter(time = 60, count = 5, limitType = LimitType.IP)
    @PostMapping("/register")
    public AjaxResult register(@RequestBody MkActivityParticipant participant, @RequestParam String token)
    {
        // 强制设置activityId来自路径安全层，不接受前端任意指定
        Map<String, Object> result = mkPublicService.register(participant, token);
        return AjaxResult.success("报名成功", result);
    }

    /**
     * 签到码签到（公开接口，现场扫码签到）
     *
     * <p>限流策略：同一IP每60秒最多10次签到请求
     */
    @RateLimiter(time = 60, count = 10, limitType = LimitType.IP)
    @PostMapping("/signIn")
    public AjaxResult signIn(@RequestParam String signInCode)
    {
        Map<String, Object> result = mkPublicService.signInByCode(signInCode);
        return AjaxResult.success("签到成功", result);
    }
}

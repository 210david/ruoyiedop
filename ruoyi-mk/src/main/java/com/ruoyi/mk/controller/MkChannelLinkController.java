package com.ruoyi.mk.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.enums.LimitType;
import com.ruoyi.mk.domain.MkActivityChannelLink;
import com.ruoyi.mk.service.IMkChannelLinkService;

/**
 * 归因链接 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/channelLink")
public class MkChannelLinkController extends BaseController
{
    @Autowired
    private IMkChannelLinkService mkChannelLinkService;

    @PreAuthorize("@ss.hasPermi('marketing:channelLink:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkActivityChannelLink link)
    {
        startPage();
        List<MkActivityChannelLink> list = mkChannelLinkService.selectLinkList(link);
        return getDataTable(list);
    }

    /** 批量生成归因链接：{activityId, channelIds[]} */
    @Log(title = "归因链接生成", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:channelLink:generate')")
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody Map<String, Object> body)
    {
        Long activityId = Long.valueOf(String.valueOf(body.get("activityId")));
        @SuppressWarnings("unchecked")
        List<Object> rawIds = (List<Object>) body.get("channelIds");
        Long[] channelIds = rawIds.stream().map(o -> Long.valueOf(String.valueOf(o))).toArray(Long[]::new);
        return AjaxResult.success(mkChannelLinkService.generateLinks(activityId, channelIds));
    }

    /** 归因跟踪（公开接口：H5 报名页带 ch 参数访问时计点击） */
    @Anonymous
    @RateLimiter(time = 60, count = 30, limitType = LimitType.IP)
    @GetMapping("/track")
    public AjaxResult track(@RequestParam String code)
    {
        MkActivityChannelLink link = mkChannelLinkService.trackByCode(code);
        if (link == null)
        {
            return AjaxResult.success();
        }
        // 仅返回最小公开信息
        return AjaxResult.success(Map.of(
                "activityId", link.getActivityId(),
                "channelCode", link.getChannelCode()));
    }

    @Log(title = "归因链接", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:channelLink:remove')")
    @DeleteMapping("/{linkIds}")
    public AjaxResult remove(@PathVariable Long[] linkIds)
    {
        return toAjax(mkChannelLinkService.deleteLinkByIds(linkIds));
    }
}

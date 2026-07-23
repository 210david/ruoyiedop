package com.ruoyi.mk.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mk.domain.MkActivity;
import com.ruoyi.mk.service.IMkActivityService;
import com.ruoyi.mk.service.IMkPublicService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 营销活动 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/activity")
public class MkActivityController extends BaseController
{
    @Autowired
    private IMkActivityService mkActivityService;

    @Autowired
    private IMkPublicService mkPublicService;

    @PreAuthorize("@ss.hasPermi('marketing:activity:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkActivity activity)
    {
        startPage();
        List<MkActivity> list = mkActivityService.selectActivityList(activity);
        return getDataTable(list);
    }

    @Log(title = "营销活动", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:activity:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkActivity activity)
    {
        List<MkActivity> list = mkActivityService.selectActivityList(activity);
        ExcelUtil<MkActivity> util = new ExcelUtil<>(MkActivity.class);
        util.exportExcel(response, list, "营销活动数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:activity:query')")
    @GetMapping(value = "/{activityId}")
    public AjaxResult getInfo(@PathVariable("activityId") Long activityId)
    {
        return AjaxResult.success(mkActivityService.selectActivityById(activityId));
    }

    @Log(title = "营销活动", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:activity:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkActivity activity)
    {
        return toAjax(mkActivityService.insertActivity(activity));
    }

    @Log(title = "营销活动", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:activity:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkActivity activity)
    {
        return toAjax(mkActivityService.updateActivity(activity));
    }

    @Log(title = "营销活动", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:activity:remove')")
    @DeleteMapping("/{activityIds}")
    public AjaxResult remove(@PathVariable Long[] activityIds)
    {
        return toAjax(mkActivityService.deleteActivityByIds(activityIds));
    }

    // ==================== P0-1: 活动状态流转 ====================

    /** P0-1: 活动状态流转 */
    @Log(title = "活动状态流转", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:activity:status')")
    @PutMapping("/changeStatus/{activityId}")
    public AjaxResult changeStatus(@PathVariable Long activityId, @RequestBody Map<String, String> body)
    {
        String toStatus = body.get("toStatus");
        String remark = body.get("remark");
        return toAjax(mkActivityService.changeStatus(activityId, toStatus, remark));
    }

    /** P0-1: 查询状态流转日志 */
    @PreAuthorize("@ss.hasPermi('marketing:activity:query')")
    @GetMapping("/statusLogs/{activityId}")
    public AjaxResult statusLogs(@PathVariable Long activityId)
    {
        return AjaxResult.success(mkActivityService.selectStatusLogs(activityId));
    }

    // ==================== P1-4: 活动复盘 ====================

    /** P1-4: 获取复盘统计数据 */
    @PreAuthorize("@ss.hasPermi('marketing:activity:review')")
    @GetMapping("/review/{activityId}")
    public AjaxResult review(@PathVariable Long activityId)
    {
        return AjaxResult.success(mkActivityService.reviewActivity(activityId));
    }

    /** P1-4: 保存复盘结果 */
    @Log(title = "活动复盘", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:activity:review')")
    @PutMapping("/saveReview")
    public AjaxResult saveReview(@RequestBody MkActivity activity)
    {
        return toAjax(mkActivityService.saveReview(activity));
    }

    // ==================== P2-11: 活动复制 ====================

    /** P2-11: 复制活动 */
    @Log(title = "活动复制", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:activity:copy')")
    @PostMapping("/copy/{activityId}")
    public AjaxResult copy(@PathVariable Long activityId)
    {
        return toAjax(mkActivityService.copyActivity(activityId));
    }

    // ==================== P3-12: 批量操作 ====================

    /** P3-12: 批量更新状态 */
    @Log(title = "批量更新活动状态", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:activity:status')")
    @PutMapping("/batchChangeStatus")
    public AjaxResult batchChangeStatus(@RequestBody Map<String, Object> body)
    {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("activityIds");
        String status = (String) body.get("status");
        Long[] activityIds = ids.stream().map(Long::valueOf).toArray(Long[]::new);
        return toAjax(mkActivityService.batchChangeStatus(activityIds, status));
    }

    /** P3-12: 批量更新负责人 */
    @Log(title = "批量更新活动负责人", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:activity:edit')")
    @PutMapping("/batchUpdateOwner")
    public AjaxResult batchUpdateOwner(@RequestBody Map<String, Object> body)
    {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("activityIds");
        Long userId = Long.valueOf(body.get("userId").toString());
        String userName = (String) body.get("userName");
        Long deptId = body.get("deptId") != null ? Long.valueOf(body.get("deptId").toString()) : null;
        Long[] activityIds = ids.stream().map(Long::valueOf).toArray(Long[]::new);
        return toAjax(mkActivityService.batchUpdateOwner(activityIds, userId, userName, deptId));
    }

    // ==================== P3-15: 报名链接 ====================

    /** P3-15: 获取活动报名链接（生成并持久化安全令牌） */
    @PreAuthorize("@ss.hasPermi('marketing:activity:query')")
    @GetMapping("/registerUrl/{activityId}")
    public AjaxResult registerUrl(@PathVariable Long activityId)
    {
        // 生成并持久化安全令牌
        String token = mkPublicService.generateRegisterToken(activityId);
        String url = "/h5/register/" + activityId + "?token=" + token;
        AjaxResult result = AjaxResult.success("获取成功");
        result.put("url", url);
        result.put("token", token);
        return result;
    }
}

package com.ruoyi.mms.controller;

import java.util.List;
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
import com.ruoyi.mms.domain.MmsAbnormal;
import com.ruoyi.mms.service.IMmsAbnormalService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 异常管理 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/abnormal")
public class MmsAbnormalController extends BaseController
{
    @Autowired
    private IMmsAbnormalService mmsAbnormalService;

    // ========== 标准 CRUD ==========

    /**
     * 查询异常列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('mms:abnormal:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsAbnormal abnormal)
    {
        startPage();
        List<MmsAbnormal> list = mmsAbnormalService.selectAbnormalList(abnormal);
        return getDataTable(list);
    }

    /**
     * 导出 Excel
     */
    @Log(title = "异常记录", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:abnormal:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsAbnormal abnormal)
    {
        List<MmsAbnormal> list = mmsAbnormalService.selectAbnormalList(abnormal);
        ExcelUtil<MmsAbnormal> util = new ExcelUtil<>(MmsAbnormal.class);
        util.exportExcel(response, list, "异常记录数据");
    }

    /**
     * 根据ID获取异常详情
     */
    @PreAuthorize("@ss.hasPermi('mms:abnormal:query')")
    @GetMapping(value = "/{abnormalId}")
    public AjaxResult getInfo(@PathVariable("abnormalId") Long abnormalId)
    {
        return AjaxResult.success(mmsAbnormalService.selectAbnormalById(abnormalId));
    }

    /**
     * 新增异常（安灯呼叫）
     */
    @Log(title = "异常记录", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:abnormal:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MmsAbnormal abnormal)
    {
        return toAjax(mmsAbnormalService.insertAbnormal(abnormal));
    }

    /**
     * 修改异常
     */
    @Log(title = "异常记录", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:abnormal:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MmsAbnormal abnormal)
    {
        return toAjax(mmsAbnormalService.updateAbnormal(abnormal));
    }

    /**
     * 删除异常（软删除）
     */
    @Log(title = "异常记录", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:abnormal:remove')")
    @DeleteMapping("/{abnormalIds}")
    public AjaxResult remove(@PathVariable Long[] abnormalIds)
    {
        return toAjax(mmsAbnormalService.deleteAbnormalByIds(abnormalIds));
    }

    // ========== 业务操作 ==========

    /**
     * 异常响应
     * 状态：0(待响应) → 1(处理中)
     */
    @Log(title = "异常记录-响应", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:abnormal:respond')")
    @PutMapping("/respond/{abnormalId}")
    public AjaxResult respond(@PathVariable("abnormalId") Long abnormalId,
            @RequestParam(required = false) String responseBy)
    {
        return toAjax(mmsAbnormalService.respondAbnormal(abnormalId, responseBy));
    }

    /**
     * 异常处理关闭
     * 状态：1(处理中) → 2(已关闭)
     */
    @Log(title = "异常记录-处理关闭", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:abnormal:resolve')")
    @PutMapping("/resolve/{abnormalId}")
    public AjaxResult resolve(@PathVariable("abnormalId") Long abnormalId,
            @RequestParam(required = false) String handleResult)
    {
        return toAjax(mmsAbnormalService.resolveAbnormal(abnormalId, handleResult));
    }
}

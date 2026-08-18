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
import com.ruoyi.mms.domain.MmsMps;
import com.ruoyi.mms.service.IMmsMpsService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 主生产计划 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/mps")
public class MmsMpsController extends BaseController
{
    @Autowired
    private IMmsMpsService mmsMpsService;

    // ========== 标准 CRUD ==========

    /**
     * 查询计划列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('mms:mps:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsMps mps)
    {
        startPage();
        List<MmsMps> list = mmsMpsService.selectMpsList(mps);
        return getDataTable(list);
    }

    /**
     * 导出 Excel
     */
    @Log(title = "生产计划", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:mps:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsMps mps)
    {
        List<MmsMps> list = mmsMpsService.selectMpsList(mps);
        ExcelUtil<MmsMps> util = new ExcelUtil<>(MmsMps.class);
        util.exportExcel(response, list, "生产计划数据");
    }

    /**
     * 根据ID获取计划详情
     */
    @PreAuthorize("@ss.hasPermi('mms:mps:query')")
    @GetMapping(value = "/{mpsId}")
    public AjaxResult getInfo(@PathVariable("mpsId") Long mpsId)
    {
        return AjaxResult.success(mmsMpsService.selectMpsById(mpsId));
    }

    /**
     * 新增计划
     */
    @Log(title = "生产计划", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:mps:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MmsMps mps)
    {
        return toAjax(mmsMpsService.insertMps(mps));
    }

    /**
     * 修改计划
     */
    @Log(title = "生产计划", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:mps:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MmsMps mps)
    {
        return toAjax(mmsMpsService.updateMps(mps));
    }

    /**
     * 删除计划（软删除）
     */
    @Log(title = "生产计划", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:mps:remove')")
    @DeleteMapping("/{mpsIds}")
    public AjaxResult remove(@PathVariable Long[] mpsIds)
    {
        return toAjax(mmsMpsService.deleteMpsByIds(mpsIds));
    }

    // ========== 业务流程操作 ==========

    /**
     * 计划确认/提交审批
     * 状态：0(草稿) → 1(已确认)
     */
    @Log(title = "生产计划-确认", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:mps:confirm')")
    @PutMapping("/confirm/{mpsId}")
    public AjaxResult confirm(@PathVariable("mpsId") Long mpsId)
    {
        return toAjax(mmsMpsService.confirmMps(mpsId));
    }

    /**
     * 计划审批
     * 状态：1(已确认) → 2(已发布) 或 0(草稿-驳回)
     */
    @Log(title = "生产计划-审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:mps:approve')")
    @PutMapping("/audit/{mpsId}")
    public AjaxResult audit(@PathVariable("mpsId") Long mpsId,
            @RequestParam String status,
            @RequestParam(required = false) String auditOpinion)
    {
        return toAjax(mmsMpsService.auditMps(mpsId, status, auditOpinion));
    }

    /**
     * 计划发布（下达生成工单）
     * 状态：2(已发布) → 3(已下达)，自动生成工单
     */
    @Log(title = "生产计划-发布", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:mps:release')")
    @PutMapping("/release/{mpsId}")
    public AjaxResult release(@PathVariable("mpsId") Long mpsId)
    {
        return AjaxResult.success(mmsMpsService.releaseMps(mpsId));
    }

    /**
     * 计划取消
     * 状态：非3(已下达) → 4(已取消)
     */
    @Log(title = "生产计划-取消", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:mps:cancel')")
    @PutMapping("/cancel/{mpsId}")
    public AjaxResult cancel(@PathVariable("mpsId") Long mpsId,
            @RequestParam(required = false) String cancelReason)
    {
        return toAjax(mmsMpsService.cancelMps(mpsId, cancelReason));
    }
}

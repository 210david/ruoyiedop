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
import com.ruoyi.mms.domain.MmsDispatch;
import com.ruoyi.mms.service.IMmsDispatchService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 派工管理 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/dispatch")
public class MmsDispatchController extends BaseController
{
    @Autowired
    private IMmsDispatchService mmsDispatchService;

    // ========== 标准 CRUD ==========

    /**
     * 查询派工单列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('mms:dispatch:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsDispatch dispatch)
    {
        startPage();
        List<MmsDispatch> list = mmsDispatchService.selectDispatchList(dispatch);
        return getDataTable(list);
    }

    /**
     * 导出 Excel
     */
    @Log(title = "派工管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsDispatch dispatch)
    {
        List<MmsDispatch> list = mmsDispatchService.selectDispatchList(dispatch);
        ExcelUtil<MmsDispatch> util = new ExcelUtil<>(MmsDispatch.class);
        util.exportExcel(response, list, "派工单数据");
    }

    /**
     * 根据ID获取派工单详情
     */
    @PreAuthorize("@ss.hasPermi('mms:dispatch:query')")
    @GetMapping(value = "/{dispatchId}")
    public AjaxResult getInfo(@PathVariable("dispatchId") Long dispatchId)
    {
        return AjaxResult.success(mmsDispatchService.selectDispatchById(dispatchId));
    }

    /**
     * 新增派工单
     */
    @Log(title = "派工管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MmsDispatch dispatch)
    {
        return toAjax(mmsDispatchService.insertDispatch(dispatch));
    }

    /**
     * 修改派工单
     */
    @Log(title = "派工管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MmsDispatch dispatch)
    {
        return toAjax(mmsDispatchService.updateDispatch(dispatch));
    }

    /**
     * 删除派工单（软删除）
     */
    @Log(title = "派工管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:remove')")
    @DeleteMapping("/{dispatchIds}")
    public AjaxResult remove(@PathVariable Long[] dispatchIds)
    {
        return toAjax(mmsDispatchService.deleteDispatchByIds(dispatchIds));
    }

    // ========== 业务流程操作 ==========

    /**
     * 派工开工
     * 状态：0(待开工) → 1(进行中)
     */
    @Log(title = "派工管理-开工", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:start')")
    @PutMapping("/start/{dispatchId}")
    public AjaxResult start(@PathVariable("dispatchId") Long dispatchId)
    {
        return toAjax(mmsDispatchService.startDispatch(dispatchId));
    }

    /**
     * 派工完工
     * 状态：1(进行中) → 2(已完成)
     */
    @Log(title = "派工管理-完工", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:finish')")
    @PutMapping("/finish/{dispatchId}")
    public AjaxResult finish(@PathVariable("dispatchId") Long dispatchId)
    {
        return toAjax(mmsDispatchService.finishDispatch(dispatchId));
    }

    /**
     * 派工取消
     * 状态：0(待开工)/1(进行中) → 3(已取消)
     */
    @Log(title = "派工管理-取消", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:dispatch:cancel')")
    @PutMapping("/cancel/{dispatchId}")
    public AjaxResult cancel(@PathVariable("dispatchId") Long dispatchId)
    {
        return toAjax(mmsDispatchService.cancelDispatch(dispatchId));
    }
}

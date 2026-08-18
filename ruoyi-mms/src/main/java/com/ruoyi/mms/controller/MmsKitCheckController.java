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
import com.ruoyi.mms.domain.MmsKitCheck;
import com.ruoyi.mms.service.IMmsKitCheckService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 齐套检查 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/kit")
public class MmsKitCheckController extends BaseController
{
    @Autowired
    private IMmsKitCheckService mmsKitCheckService;

    // ========== 标准 CRUD ==========

    /**
     * 查询齐套检查列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('mms:kit:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsKitCheck kit)
    {
        startPage();
        List<MmsKitCheck> list = mmsKitCheckService.selectKitCheckList(kit);
        return getDataTable(list);
    }

    /**
     * 导出 Excel
     */
    @Log(title = "齐套检查", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:kit:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsKitCheck kit)
    {
        List<MmsKitCheck> list = mmsKitCheckService.selectKitCheckList(kit);
        ExcelUtil<MmsKitCheck> util = new ExcelUtil<>(MmsKitCheck.class);
        util.exportExcel(response, list, "齐套检查数据");
    }

    /**
     * 根据ID获取齐套检查详情
     */
    @PreAuthorize("@ss.hasPermi('mms:kit:query')")
    @GetMapping(value = "/{kitId}")
    public AjaxResult getInfo(@PathVariable("kitId") Long kitId)
    {
        return AjaxResult.success(mmsKitCheckService.selectKitCheckById(kitId));
    }

    /**
     * 新增齐套检查
     */
    @Log(title = "齐套检查", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:kit:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MmsKitCheck kit)
    {
        return toAjax(mmsKitCheckService.insertKitCheck(kit));
    }

    /**
     * 修改齐套检查
     */
    @Log(title = "齐套检查", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:kit:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MmsKitCheck kit)
    {
        return toAjax(mmsKitCheckService.updateKitCheck(kit));
    }

    /**
     * 删除齐套检查（软删除）
     */
    @Log(title = "齐套检查", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:kit:remove')")
    @DeleteMapping("/{kitIds}")
    public AjaxResult remove(@PathVariable Long[] kitIds)
    {
        return toAjax(mmsKitCheckService.deleteKitCheckByIds(kitIds));
    }

    // ========== 业务操作 ==========

    /**
     * 执行齐套检查
     * 根据BOM展开物料需求，对比库存计算齐套率
     * 状态：0(待检查) → 1(已通过) 或 2(缺料)
     */
    @Log(title = "齐套检查-执行", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:kit:execute')")
    @PutMapping("/execute/{kitId}")
    public AjaxResult execute(@PathVariable("kitId") Long kitId)
    {
        return toAjax(mmsKitCheckService.executeKitCheck(kitId));
    }

    /**
     * 查询齐套检查明细
     */
    @PreAuthorize("@ss.hasPermi('mms:kit:query')")
    @GetMapping("/detail/{kitId}")
    public AjaxResult detail(@PathVariable("kitId") Long kitId)
    {
        return AjaxResult.success(mmsKitCheckService.selectKitCheckDetailByKitId(kitId));
    }
}

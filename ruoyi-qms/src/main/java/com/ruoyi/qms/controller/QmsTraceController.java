package com.ruoyi.qms.controller;

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
import com.ruoyi.qms.domain.QmsLotGenealogy;
import com.ruoyi.qms.domain.QmsTraceSn;
import com.ruoyi.qms.service.IQmsTraceService;
import com.ruoyi.qms.service.IQmsTraceSnService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 质量追溯 Controller
 *
 * 提供：谱系CRUD、正向追溯、反向追溯、完整度看板、断点清单、批量补录
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/qms/trace")
public class QmsTraceController extends BaseController
{
    @Autowired
    private IQmsTraceService qmsTraceService;

    @Autowired
    private IQmsTraceSnService snService;

    // ==================== 谱系管理 CRUD ====================

    @PreAuthorize("@ss.hasPermi('qms:genealogy:list')")
    @GetMapping("/genealogy/list")
    public TableDataInfo genealogyList(QmsLotGenealogy genealogy)
    {
        startPage();
        List<QmsLotGenealogy> list = qmsTraceService.selectGenealogyList(genealogy);
        return getDataTable(list);
    }

    @Log(title = "谱系管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:genealogy:export')")
    @PostMapping("/genealogy/export")
    public void genealogyExport(HttpServletResponse response, QmsLotGenealogy genealogy)
    {
        List<QmsLotGenealogy> list = qmsTraceService.selectGenealogyList(genealogy);
        ExcelUtil<QmsLotGenealogy> util = new ExcelUtil<>(QmsLotGenealogy.class);
        util.exportExcel(response, list, "批次谱系数据");
    }

    @PreAuthorize("@ss.hasPermi('qms:genealogy:query')")
    @GetMapping("/genealogy/{id}")
    public AjaxResult getGenealogy(@PathVariable Long id)
    {
        return AjaxResult.success(qmsTraceService.selectGenealogyById(id));
    }

    @Log(title = "谱系补录", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:genealogy:add')")
    @PostMapping("/genealogy")
    public AjaxResult addGenealogy(@Validated @RequestBody QmsLotGenealogy genealogy)
    {
        return toAjax(qmsTraceService.insertGenealogy(genealogy));
    }

    @Log(title = "谱系修改", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:genealogy:edit')")
    @PutMapping("/genealogy")
    public AjaxResult editGenealogy(@Validated @RequestBody QmsLotGenealogy genealogy)
    {
        return toAjax(qmsTraceService.updateGenealogy(genealogy));
    }

    @Log(title = "谱系删除", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:genealogy:remove')")
    @DeleteMapping("/genealogy/{ids}")
    public AjaxResult removeGenealogy(@PathVariable Long[] ids)
    {
        return toAjax(qmsTraceService.deleteGenealogyByIds(ids));
    }

    @Log(title = "谱系批量补录", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:genealogy:add')")
    @PostMapping("/genealogy/batch")
    public AjaxResult batchAddGenealogy(@RequestBody List<QmsLotGenealogy> list)
    {
        return toAjax(qmsTraceService.batchInsertGenealogy(list));
    }

    // ==================== 追溯引擎 ====================

    /**
     * 正向追溯：原料批次 → 成品批次 → 客户/订单
     */
    @PreAuthorize("@ss.hasPermi('qms:trace:forward')")
    @GetMapping("/forward/{batchNo}")
    public AjaxResult forwardTrace(@PathVariable String batchNo)
    {
        return AjaxResult.success(qmsTraceService.forwardTrace(batchNo));
    }

    /**
     * 反向追溯：成品批次 → 原料/供应商/检验记录/NCR
     */
    @PreAuthorize("@ss.hasPermi('qms:trace:backward')")
    @GetMapping("/backward/{batchNo}")
    public AjaxResult backwardTrace(@PathVariable String batchNo)
    {
        return AjaxResult.success(qmsTraceService.backwardTrace(batchNo));
    }

    /**
     * 降级追溯：当谱系表无数据时，利用已有业务单据中的 batch_no 做辅助关联追溯
     * 返回检验记录、NCR、仓库流转、采购收货、客诉等多维度信息
     */
    @PreAuthorize("@ss.hasPermi('qms:trace:search')")
    @GetMapping("/fallback/{batchNo}")
    public AjaxResult fallbackTrace(@PathVariable String batchNo,
            @RequestParam(value = "direction", defaultValue = "backward") String direction)
    {
        return AjaxResult.success(qmsTraceService.fallbackTrace(batchNo, direction));
    }

    // ==================== 完整度看板 ====================

    @PreAuthorize("@ss.hasPermi('qms:trace:dashboard')")
    @GetMapping("/completeness")
    public AjaxResult completeness()
    {
        return AjaxResult.success(qmsTraceService.traceCompleteness());
    }

    @PreAuthorize("@ss.hasPermi('qms:trace:dashboard')")
    @GetMapping("/breakList")
    public TableDataInfo breakList(QmsLotGenealogy genealogy)
    {
        startPage();
        List<QmsLotGenealogy> list = qmsTraceService.selectBreakList(genealogy);
        return getDataTable(list);
    }

    // ==================== SN级追溯 ====================

    @PreAuthorize("@ss.hasPermi('qms:trace:sn:list')")
    @GetMapping("/sn/list")
    public TableDataInfo snList(QmsTraceSn sn)
    {
        startPage();
        return getDataTable(snService.selectSnList(sn));
    }

    @PreAuthorize("@ss.hasPermi('qms:trace:sn:query')")
    @GetMapping("/sn/{id}")
    public AjaxResult getSn(@PathVariable Long id)
    {
        return AjaxResult.success(snService.selectSnById(id));
    }

    @PreAuthorize("@ss.hasPermi('qms:trace:sn:query')")
    @GetMapping("/sn/code/{snCode}")
    public AjaxResult snByCode(@PathVariable String snCode)
    {
        return AjaxResult.success(snService.selectBySnCode(snCode));
    }

    @PreAuthorize("@ss.hasPermi('qms:trace:sn:query')")
    @GetMapping("/sn/batch/{batchNo}")
    public AjaxResult snByBatch(@PathVariable String batchNo)
    {
        return AjaxResult.success(snService.selectByBatchNo(batchNo));
    }

    @Log(title = "SN追溯", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:trace:sn:add')")
    @PostMapping("/sn")
    public AjaxResult addSn(@RequestBody QmsTraceSn sn)
    {
        return toAjax(snService.insertSn(sn));
    }

    @Log(title = "SN追溯", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:trace:sn:edit')")
    @PutMapping("/sn")
    public AjaxResult editSn(@RequestBody QmsTraceSn sn)
    {
        return toAjax(snService.updateSn(sn));
    }

    @Log(title = "SN追溯", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:trace:sn:remove')")
    @DeleteMapping("/sn/{ids}")
    public AjaxResult removeSn(@PathVariable Long[] ids)
    {
        return toAjax(snService.deleteSnByIds(ids));
    }
}

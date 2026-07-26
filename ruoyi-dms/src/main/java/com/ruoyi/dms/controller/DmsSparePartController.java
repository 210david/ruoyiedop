package com.ruoyi.dms.controller;

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
import com.ruoyi.dms.domain.DmsSparePart;
import com.ruoyi.dms.domain.DmsSparePartStock;
import com.ruoyi.dms.domain.DmsSparePartRecord;
import com.ruoyi.dms.mapper.DmsSparePartStockMapper;
import com.ruoyi.dms.service.IDmsSparePartService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/dms/sparepart")
public class DmsSparePartController extends BaseController
{
    @Autowired
    private IDmsSparePartService dmsSparePartService;

    @Autowired
    private DmsSparePartStockMapper dmsSparePartStockMapper;

    // ==================== 备件主数据 CRUD ====================

    @PreAuthorize("@ss.hasPermi('dms:sparepart:list')")
    @GetMapping("/list")
    public TableDataInfo list(DmsSparePart sparePart)
    {
        startPage();
        List<DmsSparePart> list = dmsSparePartService.selectSparePartList(sparePart);
        return getDataTable(list);
    }

    @Log(title = "备件管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('dms:sparepart:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, DmsSparePart sparePart)
    {
        List<DmsSparePart> list = dmsSparePartService.selectSparePartList(sparePart);
        ExcelUtil<DmsSparePart> util = new ExcelUtil<>(DmsSparePart.class);
        util.exportExcel(response, list, "备件数据");
    }

    @PreAuthorize("@ss.hasPermi('dms:sparepart:query')")
    @GetMapping(value = "/{partId}")
    public AjaxResult getInfo(@PathVariable("partId") Long partId)
    {
        return AjaxResult.success(dmsSparePartService.selectSparePartById(partId));
    }

    @PreAuthorize("@ss.hasPermi('dms:sparepart:add')")
    @GetMapping("/genCode")
    public AjaxResult genPartCode()
    {
        return AjaxResult.success("生成成功", dmsSparePartService.generatePartCode());
    }

    @PreAuthorize("@ss.hasPermi('dms:partin:add')")
    @GetMapping("/genInCode")
    public AjaxResult genInCode()
    {
        return AjaxResult.success("生成成功", dmsSparePartService.generateInCode());
    }

    @PreAuthorize("@ss.hasPermi('dms:partout:add')")
    @GetMapping("/genOutCode")
    public AjaxResult genOutCode()
    {
        return AjaxResult.success("生成成功", dmsSparePartService.generateOutCode());
    }

    @Log(title = "备件管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('dms:sparepart:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DmsSparePart sparePart)
    {
        return toAjax(dmsSparePartService.insertSparePart(sparePart));
    }

    @Log(title = "备件管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:sparepart:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DmsSparePart sparePart)
    {
        return toAjax(dmsSparePartService.updateSparePart(sparePart));
    }

    @Log(title = "备件管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:sparepart:remove')")
    @DeleteMapping("/{partIds}")
    public AjaxResult remove(@PathVariable Long[] partIds)
    {
        return toAjax(dmsSparePartService.deleteSparePartByIds(partIds));
    }

    // ==================== 库存台账（stock表） ====================

    /** 库存台账列表（关联查询 stock 表，带备件主数据） */
    @PreAuthorize("@ss.hasPermi('dms:sparepart:list')")
    @GetMapping("/stock/list")
    public TableDataInfo stockList(DmsSparePartStock stock)
    {
        startPage();
        List<DmsSparePartStock> list = dmsSparePartStockMapper.selectStockList(stock);
        return getDataTable(list);
    }

    /** 删除库存记录（只删库存，不删主数据，支持批量删除） */
    @Log(title = "备件库存台账", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:sparepart:remove')")
    @DeleteMapping("/stock/{stockIds}")
    public AjaxResult removeStock(@PathVariable Long[] stockIds)
    {
        return toAjax(dmsSparePartStockMapper.deleteStockByIds(stockIds));
    }

    // ==================== 出入库操作 ====================

    @Log(title = "备件出入库", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:sparepart:edit')")
    @PostMapping("/stockMove")
    public AjaxResult stockMove(@RequestBody DmsSparePartRecord record)
    {
        return toAjax(dmsSparePartService.stockMove(record));
    }

    @PreAuthorize("@ss.hasAnyPermi('dms:sparepart:query,dms:partin:query,dms:partout:query,dms:partstock:query')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(DmsSparePartRecord record)
    {
        startPage();
        List<DmsSparePartRecord> list = dmsSparePartService.selectRecordList(record);
        return getDataTable(list);
    }

    @Log(title = "备件出入库记录", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:partstock:remove')")
    @DeleteMapping("/record/{recordIds}")
    public AjaxResult removeRecord(@PathVariable Long[] recordIds)
    {
        return toAjax(dmsSparePartService.deleteRecordByIds(recordIds));
    }

    /** 查看出入库记录 */
    @PreAuthorize("@ss.hasAnyPermi('dms:partin:query,dms:partout:query')")
    @GetMapping(value = "/record/{recordId}")
    public AjaxResult getRecord(@PathVariable("recordId") Long recordId)
    {
        return AjaxResult.success(dmsSparePartService.selectRecordById(recordId));
    }

    /** 修改出入库记录 */
    @Log(title = "备件出入库记录", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasAnyPermi('dms:partin:edit,dms:partout:edit')")
    @PutMapping("/record")
    public AjaxResult editRecord(@RequestBody DmsSparePartRecord record)
    {
        return toAjax(dmsSparePartService.updateRecord(record));
    }

    // ==================== 导出 ====================

    @Log(title = "备件入库", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('dms:partin:export')")
    @PostMapping("/partin/export")
    public void partInExport(HttpServletResponse response, DmsSparePartRecord record)
    {
        record.setMoveType("0");
        List<DmsSparePartRecord> list = dmsSparePartService.selectRecordList(record);
        ExcelUtil<DmsSparePartRecord> util = new ExcelUtil<>(DmsSparePartRecord.class);
        util.hideColumn("moveType", "targetType", "receiver");
        util.exportExcel(response, list, "备件入库记录");
    }

    @Log(title = "备件出库", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('dms:partout:export')")
    @PostMapping("/partout/export")
    public void partOutExport(HttpServletResponse response, DmsSparePartRecord record)
    {
        record.setMoveType("not0");
        List<DmsSparePartRecord> list = dmsSparePartService.selectRecordList(record);
        ExcelUtil<DmsSparePartRecord> util = new ExcelUtil<>(DmsSparePartRecord.class);
        util.hideColumn("moveType", "sourceType", "storageLocation");
        util.exportExcel(response, list, "备件出库记录");
    }

    @Log(title = "库存流水报表", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('dms:partstock:export')")
    @PostMapping("/partstock/export")
    public void partStockExport(HttpServletResponse response, DmsSparePartRecord record)
    {
        List<DmsSparePartRecord> list = dmsSparePartService.selectRecordList(record);
        ExcelUtil<DmsSparePartRecord> util = new ExcelUtil<>(DmsSparePartRecord.class);
        util.hideColumn("receiver", "operateDate", "storageLocation");
        util.exportExcel(response, list, "库存流水记录");
    }

    // ==================== 库存预警 ====================

    /** 库存预警列表（查询 stock 表） */
    @PreAuthorize("@ss.hasPermi('dms:partalert:query')")
    @GetMapping("/alert/list")
    public TableDataInfo alertList(DmsSparePartStock stock)
    {
        startPage();
        List<DmsSparePartStock> list = dmsSparePartStockMapper.selectStockAlertList(stock);
        return getDataTable(list);
    }

    /** 库存预警导出 */
    @Log(title = "库存预警", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('dms:partalert:export')")
    @PostMapping("/alert/export")
    public void alertExport(HttpServletResponse response, DmsSparePartStock stock)
    {
        List<DmsSparePartStock> list = dmsSparePartStockMapper.selectStockAlertList(stock);
        ExcelUtil<DmsSparePartStock> util = new ExcelUtil<>(DmsSparePartStock.class);
        util.exportExcel(response, list, "库存预警数据");
    }

    /** 删除库存预警（清除库存上下限设置） */
    @Log(title = "库存预警", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:partalert:remove')")
    @DeleteMapping("/alert/{partId}")
    public AjaxResult removeAlert(@PathVariable Long partId)
    {
        return toAjax(dmsSparePartService.clearSparePartAlert(partId));
    }
}

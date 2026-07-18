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
import com.ruoyi.dms.domain.DmsSparePartRecord;
import com.ruoyi.dms.service.IDmsSparePartService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/dms/sparepart")
public class DmsSparePartController extends BaseController
{
    @Autowired
    private IDmsSparePartService dmsSparePartService;

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

    /** 自动生成备件编号 */
    @PreAuthorize("@ss.hasPermi('dms:sparepart:add')")
    @GetMapping("/genCode")
    public AjaxResult genPartCode()
    {
        return AjaxResult.success("生成成功", dmsSparePartService.generatePartCode());
    }

    /** 自动生成入库单号 */
    @PreAuthorize("@ss.hasPermi('dms:partin:add')")
    @GetMapping("/genInCode")
    public AjaxResult genInCode()
    {
        return AjaxResult.success("生成成功", dmsSparePartService.generateInCode());
    }

    /** 自动生成出库单号 */
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

    /** 出入库操作 */
    @Log(title = "备件出入库", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:sparepart:edit')")
    @PostMapping("/stockMove")
    public AjaxResult stockMove(@RequestBody DmsSparePartRecord record)
    {
        return toAjax(dmsSparePartService.stockMove(record));
    }

    /** 出入库记录列表 */
    @PreAuthorize("@ss.hasPermi('dms:sparepart:query')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(DmsSparePartRecord record)
    {
        startPage();
        List<DmsSparePartRecord> list = dmsSparePartService.selectRecordList(record);
        return getDataTable(list);
    }

    /** 删除出入库记录 */
    @Log(title = "备件出入库记录", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:partstock:remove')")
    @DeleteMapping("/record/{recordIds}")
    public AjaxResult removeRecord(@PathVariable Long[] recordIds)
    {
        return toAjax(dmsSparePartService.deleteRecordByIds(recordIds));
    }

    /** 入库记录导出 */
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

    /** 出库记录导出 */
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

    /** 库存流水报表导出 */
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

    /** 库存预警列表 */
    @PreAuthorize("@ss.hasPermi('dms:partalert:query')")
    @GetMapping("/alert/list")
    public TableDataInfo alertList(DmsSparePart sparePart)
    {
        startPage();
        List<DmsSparePart> list = dmsSparePartService.selectStockAlertList(sparePart);
        return getDataTable(list);
    }

    /** 删除库存预警（清除备件安全库存设置） */
    @Log(title = "库存预警", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:partalert:remove')")
    @DeleteMapping("/alert/{partId}")
    public AjaxResult removeAlert(@PathVariable Long partId)
    {
        return toAjax(dmsSparePartService.clearSparePartAlert(partId));
    }
}

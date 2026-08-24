package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsFinishReceipt;
import com.ruoyi.mms.service.IMmsFinishReceiptService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * FinishReceipt Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/finish")
public class MmsFinishReceiptController extends BaseController
{
    @Autowired
    private IMmsFinishReceiptService mmsFinishReceiptService;

    @PreAuthorize("@ss.hasPermi('mms:finish:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsFinishReceipt finish)
    {
        startPage();
        List<MmsFinishReceipt> list = mmsFinishReceiptService.selectFinishReceiptList(finish);
        return getDataTable(list);
    }

    @Log(title = "FinishReceipt", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:finish:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsFinishReceipt finish)
    {
        List<MmsFinishReceipt> list = mmsFinishReceiptService.selectFinishReceiptList(finish);
        ExcelUtil<MmsFinishReceipt> util = new ExcelUtil<>(MmsFinishReceipt.class);
        util.exportExcel(response, list, "FinishReceipt");
    }

    @PreAuthorize("@ss.hasPermi('mms:finish:query')")
    @GetMapping(value = "/{FinishId}")
    public AjaxResult getInfo(@PathVariable("FinishId") Long FinishId)
    {
        return AjaxResult.success(mmsFinishReceiptService.selectFinishReceiptById(FinishId));
    }

    @Log(title = "FinishReceipt", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:finish:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MmsFinishReceipt finish)
    {
        return toAjax(mmsFinishReceiptService.insertFinishReceipt(finish));
    }

    @Log(title = "FinishReceipt", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:finish:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MmsFinishReceipt finish)
    {
        return toAjax(mmsFinishReceiptService.updateFinishReceipt(finish));
    }

    @Log(title = "FinishReceipt", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:finish:remove')")
    @DeleteMapping("/{FinishIds}")
    public AjaxResult remove(@PathVariable Long[] FinishIds)
    {
        return toAjax(mmsFinishReceiptService.deleteFinishReceiptByIds(FinishIds));
    }

    /**
     * 入库确认
     */
    @Log(title = "完工入库", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:finish:confirm')")
    @PutMapping("/confirm/{finishId}")
    public AjaxResult confirm(@PathVariable("finishId") Long finishId, @RequestBody MmsFinishReceipt finish)
    {
        finish.setFinishId(finishId);
        return toAjax(mmsFinishReceiptService.confirmFinishReceipt(finish));
    }
}

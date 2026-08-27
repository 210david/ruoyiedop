package com.ruoyi.hr.controller;

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
import com.ruoyi.hr.domain.HrTransfer;
import com.ruoyi.hr.service.IHrTransferService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/transfer")
public class HrTransferController extends BaseController
{
    @Autowired
    private IHrTransferService hrTransferService;

    @PreAuthorize("@ss.hasPermi('hr:transfer:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrTransfer hrTransfer)
    {
        startPage();
        List<HrTransfer> list = hrTransferService.selectHrTransferList(hrTransfer);
        return getDataTable(list);
    }

    @Log(title = "调动", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:transfer:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrTransfer hrTransfer)
    {
        List<HrTransfer> list = hrTransferService.selectHrTransferList(hrTransfer);
        ExcelUtil<HrTransfer> util = new ExcelUtil<>(HrTransfer.class);
        util.exportExcel(response, list, "调动");
    }

    @PreAuthorize("@ss.hasPermi('hr:transfer:query')")
    @GetMapping(value = "/{transferId}")
    public AjaxResult getInfo(@PathVariable("transferId") Long transferId)
    {
        return AjaxResult.success(hrTransferService.selectHrTransferById(transferId));
    }

    @PreAuthorize("@ss.hasPermi('hr:transfer:add')")
    @Log(title = "调动", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrTransfer hrTransfer)
    {
        return toAjax(hrTransferService.insertHrTransfer(hrTransfer));
    }

    @PreAuthorize("@ss.hasPermi('hr:transfer:edit')")
    @Log(title = "调动", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrTransfer hrTransfer)
    {
        return toAjax(hrTransferService.updateHrTransfer(hrTransfer));
    }

    @PreAuthorize("@ss.hasPermi('hr:transfer:remove')")
    @Log(title = "调动", businessType = BusinessType.DELETE)
    @DeleteMapping("/{transferIds}")
    public AjaxResult remove(@PathVariable Long[] transferIds)
    {
        return toAjax(hrTransferService.deleteHrTransferByIds(transferIds));
    }
}

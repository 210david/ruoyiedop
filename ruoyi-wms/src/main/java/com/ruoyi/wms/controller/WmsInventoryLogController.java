package com.ruoyi.wms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.wms.domain.WmsInventoryLog;
import com.ruoyi.wms.service.IWmsInventoryService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/wms/log")
public class WmsInventoryLogController extends BaseController
{
    @Autowired
    private IWmsInventoryService wmsInventoryService;

    @PreAuthorize("@ss.hasPermi('wms:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsInventoryLog log)
    {
        startPage();
        List<WmsInventoryLog> list = wmsInventoryService.selectInventoryLogList(log);
        return getDataTable(list);
    }

    @Log(title = "库存流水", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('wms:log:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsInventoryLog log)
    {
        List<WmsInventoryLog> list = wmsInventoryService.selectInventoryLogList(log);
        ExcelUtil<WmsInventoryLog> util = new ExcelUtil<>(WmsInventoryLog.class);
        util.exportExcel(response, list, "库存流水数据");
    }
}

package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsOeeSnapshot;
import com.ruoyi.mms.service.IMmsOeeSnapshotService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/mms/oee")
public class MmsOeeSnapshotController extends BaseController
{
    @Autowired
    private IMmsOeeSnapshotService mmsOeeSnapshotService;

    @PreAuthorize("@ss.hasPermi('mms:oee:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsOeeSnapshot snapshot)
    {
        startPage();
        List<MmsOeeSnapshot> list = mmsOeeSnapshotService.selectOeeSnapshotList(snapshot);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mms:oee:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsOeeSnapshot snapshot)
    {
        List<MmsOeeSnapshot> list = mmsOeeSnapshotService.selectOeeSnapshotList(snapshot);
        ExcelUtil<MmsOeeSnapshot> util = new ExcelUtil<>(MmsOeeSnapshot.class);
        util.exportExcel(response, list, "OEE分析数据");
    }

    @PreAuthorize("@ss.hasPermi('mms:oee:query')")
    @GetMapping(value = "/{snapshotId}")
    public AjaxResult getInfo(@PathVariable("snapshotId") Long snapshotId)
    {
        return AjaxResult.success(mmsOeeSnapshotService.selectOeeSnapshotById(snapshotId));
    }
}

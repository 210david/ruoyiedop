package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
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
        // 使用PageHelper分页但禁用自动COUNT（复杂派生表SQL的COUNT查询性能极差）
        com.ruoyi.common.core.page.PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        if (pageNum == null) pageNum = 1;
        if (pageSize == null) pageSize = 10;
        PageHelper.startPage(pageNum, pageSize, false);
        List<MmsOeeSnapshot> list = mmsOeeSnapshotService.selectOeeSnapshotList(snapshot);
        // 手动查询总数（简单COUNT，不走派生表）
        long total = mmsOeeSnapshotService.selectOeeSnapshotCount(snapshot);
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(200);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(total);
        return rspData;
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

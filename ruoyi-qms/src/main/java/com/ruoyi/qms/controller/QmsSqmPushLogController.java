package com.ruoyi.qms.controller;

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
import com.ruoyi.qms.domain.QmsSqmPushLog;
import com.ruoyi.qms.service.ISqmScorePushService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * SQM→PMS 推送日志 Controller
 *
 * 对应 PRD FR-SQM-06
 */
@RestController
@RequestMapping("/qms/sqmPushLog")
public class QmsSqmPushLogController extends BaseController {

    @Autowired
    private ISqmScorePushService pushService;

    @PreAuthorize("@ss.hasPermi('qms:sqmPushLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsSqmPushLog log) {
        startPage();
        return getDataTable(pushService.selectPushLogList(log));
    }

    @Log(title = "推送日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:sqmPushLog:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsSqmPushLog log) {
        List<QmsSqmPushLog> list = pushService.selectPushLogList(log);
        ExcelUtil<QmsSqmPushLog> util = new ExcelUtil<>(QmsSqmPushLog.class);
        util.exportExcel(response, list, "SQM推送日志");
    }

    @PreAuthorize("@ss.hasPermi('qms:sqmPushLog:query')")
    @GetMapping("/{logId}")
    public AjaxResult getInfo(@PathVariable Long logId) {
        return AjaxResult.success(pushService.selectPushLogById(logId));
    }

    @Log(title = "推送日志", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:sqmPushLog:remove')")
    @DeleteMapping("/{logIds}")
    public AjaxResult remove(@PathVariable Long[] logIds) {
        return toAjax(pushService.deletePushLogByIds(logIds));
    }
}

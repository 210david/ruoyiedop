package com.ruoyi.hr.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.hr.domain.HrLeaveBalanceLog;
import com.ruoyi.hr.service.IHrLeaveBalanceLogService;

/**
 * 假期余额扣减明细
 */
@RestController
@RequestMapping("/hr/leaveBalanceLog")
public class HrLeaveBalanceLogController extends BaseController
{
    @Autowired
    private IHrLeaveBalanceLogService hrLeaveBalanceLogService;

    /**
     * 查询扣减明细列表
     */
    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:query')")
    @GetMapping("/list")
    public TableDataInfo list(HrLeaveBalanceLog hrLeaveBalanceLog)
    {
        startPage();
        List<HrLeaveBalanceLog> list = hrLeaveBalanceLogService.selectHrLeaveBalanceLogList(hrLeaveBalanceLog);
        return getDataTable(list);
    }
}

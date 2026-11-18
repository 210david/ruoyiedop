package com.ruoyi.oa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.oa.service.OaResourceCalendarService;

/**
 * 综合办公资源使用日历（印章/车辆/司机）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/oa/resource")
public class OaResourceCalendarController extends BaseController
{
    @Autowired
    private OaResourceCalendarService resourceCalendarService;

    /**
     * 资源使用日历数据
     *
     * @param type      资源类型：vehicle / driver / seal
     * @param startDate 开始日期 yyyy-MM-dd
     * @param endDate   结束日期 yyyy-MM-dd
     */
    @PreAuthorize("@ss.hasPermi('oa:vehicle:list') or @ss.hasPermi('oa:driver:list') or @ss.hasPermi('oa:seal:list')")
    @GetMapping("/calendar")
    public AjaxResult calendar(@RequestParam String type, @RequestParam String startDate, @RequestParam String endDate)
    {
        return success(resourceCalendarService.selectCalendar(type, startDate, endDate));
    }
}

package com.ruoyi.integration.service;

import java.util.Date;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 企业微信集成同步服务
 *
 * @author ruoyi
 */
public interface IWecomSyncService
{
    /**
     * 同步企业微信通讯录并自动匹配员工档案
     *
     * @return 同步结果（新增/更新/自动匹配/未匹配 统计）
     */
    public AjaxResult syncContacts();

    /**
     * 同步指定日期范围的企业微信打卡数据到考勤模块
     *
     * @param beginDate 开始日期（含）
     * @param endDate   结束日期（含）
     * @return 同步结果
     */
    public AjaxResult syncAttendance(Date beginDate, Date endDate);
}

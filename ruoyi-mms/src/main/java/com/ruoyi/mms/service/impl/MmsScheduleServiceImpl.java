package com.ruoyi.mms.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsSchedule;
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.mapper.MmsScheduleMapper;
import com.ruoyi.mms.mapper.MmsWorkOrderMapper;
import com.ruoyi.mms.service.IMmsScheduleService;
import com.ruoyi.system.utils.MessageHelper;

/**
 * 排产计划 Service实现
 *
 * 排产状态机（简化后）：
 * 0(已下达) → 1(已取消)
 *
 * @author ruoyi
 */
@Service
public class MmsScheduleServiceImpl implements IMmsScheduleService
{
    @Autowired
    private MmsScheduleMapper scheduleMapper;

    @Autowired
    private MmsWorkOrderMapper workOrderMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private MessageHelper messageHelper;

    // ========== 标准 CRUD ==========

    @Override
    public List<MmsSchedule> selectScheduleList(MmsSchedule schedule)
    {
        return scheduleMapper.selectScheduleList(schedule);
    }

    @Override
    public MmsSchedule selectScheduleById(Long scheduleId)
    {
        return scheduleMapper.selectScheduleById(scheduleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSchedule(MmsSchedule schedule)
    {
        // 自动生成排产编号
        if (StringUtils.isEmpty(schedule.getScheduleNo()))
        {
            schedule.setScheduleNo(mkNumberRuleService.generateNumber("mms_schedule"));
        }
        // 默认状态为待确认
        if (StringUtils.isEmpty(schedule.getStatus()))
        {
            schedule.setStatus("0");
        }
        // 默认排产策略为正排
        if (StringUtils.isEmpty(schedule.getStrategy()))
        {
            schedule.setStrategy("1");
        }
        if (schedule.getOpSeq() == null)
        {
            schedule.setOpSeq(1);
        }
        if (schedule.getSetupMinutes() == null)
        {
            schedule.setSetupMinutes(0);
        }
        schedule.setDelFlag("0");
        schedule.setCreateBy(SecurityUtils.getUsername());
        schedule.setCreateTime(DateUtils.getNowDate());
        int rows = scheduleMapper.insertSchedule(schedule);
        // 排产创建后，标记"工单已下达，请安排生产"消息为已处理
        if (schedule.getWorkOrderId() != null)
        {
            messageHelper.markHandled("mms", schedule.getWorkOrderId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSchedule(MmsSchedule schedule)
    {
        // 校验：已取消状态不允许修改
        MmsSchedule existing = scheduleMapper.selectScheduleById(schedule.getScheduleId());
        if (existing != null && "1".equals(existing.getStatus()))
        {
            throw new ServiceException("排产[" + existing.getScheduleNo() + "]当前状态为" + statusName(existing.getStatus()) + "，不允许修改");
        }
        schedule.setUpdateBy(SecurityUtils.getUsername());
        return scheduleMapper.updateSchedule(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteScheduleByIds(Long[] scheduleIds)
    {
        for (Long id : scheduleIds)
        {
            scheduleEntityCheck(id);
        }
        return scheduleMapper.deleteScheduleByIds(scheduleIds);
    }

    // ========== 业务操作 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmSchedule(Long scheduleId)
    {
        // 已废弃：流程简化后排产直接下达，此方法保留兼容
        MmsSchedule schedule = getAndCheckSchedule(scheduleId);
        schedule.setUpdateBy(SecurityUtils.getUsername());
        return scheduleMapper.updateSchedule(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseSchedule(Long scheduleId)
    {
        // 已废弃：流程简化后排产直接下达，此方法保留兼容
        MmsSchedule schedule = getAndCheckSchedule(scheduleId);
        schedule.setStatus("0"); // 已下达
        schedule.setUpdateBy(SecurityUtils.getUsername());
        int rows = scheduleMapper.updateSchedule(schedule);

        // 排产只同步产能单元到工单，不覆盖工单的计划时间
        // 工单计划时间是用户在工单管理页面填写的粗粒度计划，排产时间是产能单元级别的细粒度排程，两者是不同维度
        if (schedule.getWorkOrderId() != null)
        {
            MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(schedule.getWorkOrderId());
            if (wo != null)
            {
                wo.setResourceId(schedule.getResourceId());
                wo.setResourceName(schedule.getResourceName());
                wo.setUpdateBy(SecurityUtils.getUsername());
                workOrderMapper.updateWorkOrder(wo);
            }
        }

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelSchedule(Long scheduleId, String cancelReason)
    {
        MmsSchedule schedule = getAndCheckSchedule(scheduleId);
        if ("1".equals(schedule.getStatus()))
        {
            throw new ServiceException("排产[" + schedule.getScheduleNo() + "]已是取消状态");
        }
        schedule.setStatus("1");
        schedule.setRemark(cancelReason);
        schedule.setUpdateBy(SecurityUtils.getUsername());
        int rows = scheduleMapper.updateSchedule(schedule);

        // 取消排产只改排产记录状态，不修改工单状态
        // 工单状态由工单管理页面统一管理，排产取消后工单仍然保持原状态
        // 工单如果已下达，取消排产只是取消了产能分配，工单本身仍然有效
        return rows;
    }

    // ========== 私有辅助方法 ==========

    private MmsSchedule getAndCheckSchedule(Long scheduleId)
    {
        MmsSchedule schedule = scheduleMapper.selectScheduleById(scheduleId);
        if (schedule == null)
        {
            throw new ServiceException("排产计划不存在或已删除");
        }
        return schedule;
    }

    private void scheduleEntityCheck(Long scheduleId)
    {
        MmsSchedule schedule = scheduleMapper.selectScheduleById(scheduleId);
        if (schedule != null && !"1".equals(schedule.getStatus()))
        {
            throw new ServiceException("排产[" + schedule.getScheduleNo() + "]非已取消状态，不允许删除");
        }
    }

    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "已下达";
            case "1": return "已取消";
            default: return "未知(" + status + ")";
        }
    }

    // ========== 甘特图排产工作台 ==========

    @Override
    public List<MmsSchedule> selectScheduleByDateRange(String startDate, String endDate)
    {
        return scheduleMapper.selectScheduleByDateRange(startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int dragUpdateSchedule(MmsSchedule schedule)
    {
        MmsSchedule existing = scheduleMapper.selectScheduleById(schedule.getScheduleId());
        if (existing == null)
        {
            throw new ServiceException("排产计划不存在或已删除");
        }
        // 已取消的排产不允许拖拽
        if ("1".equals(existing.getStatus()))
        {
            throw new ServiceException("排产[" + existing.getScheduleNo() + "]已取消，不允许拖拽调整");
        }
        // 冲突检测
        if (schedule.getResourceId() != null && schedule.getPlanStart() != null && schedule.getPlanEnd() != null)
        {
            if (checkConflict(schedule.getResourceId(), schedule.getPlanStart(), schedule.getPlanEnd(), schedule.getScheduleId()))
            {
                throw new ServiceException("与该产能单元已有排产任务时间冲突");
            }
        }
        // 只更新拖拽相关字段
        existing.setResourceId(schedule.getResourceId());
        existing.setResourceName(schedule.getResourceName());
        existing.setPlanStart(schedule.getPlanStart());
        existing.setPlanEnd(schedule.getPlanEnd());
        existing.setUpdateBy(SecurityUtils.getUsername());
        return scheduleMapper.updateSchedule(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.Map<String, Object> batchSchedule(List<MmsSchedule> scheduleList)
    {
        int count = 0;
        List<String> conflictMsgs = new java.util.ArrayList<>();
        for (MmsSchedule schedule : scheduleList)
        {
            // 冲突检测：提示但不阻止保存
            if (schedule.getResourceId() != null && schedule.getPlanStart() != null && schedule.getPlanEnd() != null)
            {
                if (checkConflict(schedule.getResourceId(), schedule.getPlanStart(), schedule.getPlanEnd(), schedule.getScheduleId()))
                {
                    conflictMsgs.add("工单[" + schedule.getWorkOrderNo() + "]排产与已有任务时间冲突");
                }
            }
            if (schedule.getScheduleId() != null)
            {
                // 已存在的排产，更新
                schedule.setUpdateBy(SecurityUtils.getUsername());
                scheduleMapper.updateSchedule(schedule);
            }
            else
            {
                // 新排产，插入
                if (StringUtils.isEmpty(schedule.getScheduleNo()))
                {
                    schedule.setScheduleNo(mkNumberRuleService.generateNumber("mms_schedule"));
                }
                if (StringUtils.isEmpty(schedule.getStatus()))
                {
                    schedule.setStatus("0"); // 默认已下达
                }
                if (StringUtils.isEmpty(schedule.getStrategy()))
                {
                    schedule.setStrategy("1");
                }
                if (schedule.getOpSeq() == null)
                {
                    schedule.setOpSeq(1);
                }
                if (schedule.getSetupMinutes() == null)
                {
                    schedule.setSetupMinutes(0);
                }
                schedule.setDelFlag("0");
                schedule.setCreateBy(SecurityUtils.getUsername());
                schedule.setCreateTime(DateUtils.getNowDate());
                scheduleMapper.insertSchedule(schedule);
            }
            // 如果排产状态为已下达(0)，只同步产能单元到工单（不改工单状态和计划时间）
            // 工单计划时间是用户在工单管理页面填写的粗粒度计划，排产时间是产能单元级别的细粒度排程，两者是不同维度
            // 工单状态只由工单管理的下达操作推进，排产不越界修改
            if ("0".equals(schedule.getStatus()) && schedule.getWorkOrderId() != null)
            {
                MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(schedule.getWorkOrderId());
                if (wo != null)
                {
                    wo.setResourceId(schedule.getResourceId());
                    wo.setResourceName(schedule.getResourceName());
                    wo.setUpdateBy(SecurityUtils.getUsername());
                    workOrderMapper.updateWorkOrder(wo);
                }
                // 批量排产后，标记"工单已下达，请安排生产"消息为已处理
                messageHelper.markHandled("mms", schedule.getWorkOrderId());
            }
            count++;
        }
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("count", count);
        result.put("conflictMsgs", conflictMsgs);
        result.put("hasConflict", !conflictMsgs.isEmpty());
        return result;
    }

    @Override
    public boolean checkConflict(Long resourceId, Date planStart, Date planEnd, Long excludeScheduleId)
    {
        if (resourceId == null || planStart == null || planEnd == null)
        {
            return false;
        }
        int count = scheduleMapper.countConflict(resourceId, planStart, planEnd, excludeScheduleId);
        return count > 0;
    }

    @Override
    public List<Long> selectScheduledWorkOrderIds()
    {
        return scheduleMapper.selectScheduledWorkOrderIds();
    }
}

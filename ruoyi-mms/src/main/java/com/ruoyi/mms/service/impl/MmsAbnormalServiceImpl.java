package com.ruoyi.mms.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsAbnormal;
import com.ruoyi.mms.domain.MmsDowntime;
import com.ruoyi.mms.mapper.MmsAbnormalMapper;
import com.ruoyi.mms.mapper.MmsDowntimeMapper;
import com.ruoyi.mms.service.IMmsAbnormalService;

/**
 * 异常管理 Service实现
 *
 * 异常状态机：
 * 0(待响应) → 1(处理中) → 2(已关闭)
 *
 * @author ruoyi
 */
@Service
public class MmsAbnormalServiceImpl implements IMmsAbnormalService
{
    @Autowired
    private MmsAbnormalMapper abnormalMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private MmsDowntimeMapper downtimeMapper;

    @Override
    public List<MmsAbnormal> selectAbnormalList(MmsAbnormal abnormal)
    {
        return abnormalMapper.selectAbnormalList(abnormal);
    }

    @Override
    public MmsAbnormal selectAbnormalById(Long abnormalId)
    {
        return abnormalMapper.selectAbnormalById(abnormalId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAbnormal(MmsAbnormal abnormal)
    {
        // 自动生成异常单号
        if (StringUtils.isEmpty(abnormal.getAbnormalNo()))
        {
            abnormal.setAbnormalNo(mkNumberRuleService.generateNumber("mms_abnormal"));
        }
        // 默认状态为待响应
        if (StringUtils.isEmpty(abnormal.getStatus()))
        {
            abnormal.setStatus("0");
        }
        abnormal.setDelFlag("0");
        abnormal.setCreateBy(SecurityUtils.getUsername());
        abnormal.setCreateTime(DateUtils.getNowDate());
        abnormal.setReportBy(SecurityUtils.getUsername());
        abnormal.setReportTime(new Date());
        return abnormalMapper.insertAbnormal(abnormal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAbnormal(MmsAbnormal abnormal)
    {
        // 已关闭的异常不允许修改
        MmsAbnormal existing = abnormalMapper.selectAbnormalById(abnormal.getAbnormalId());
        if (existing != null && "2".equals(existing.getStatus()))
        {
            throw new ServiceException("异常[" + existing.getAbnormalNo() + "]已关闭，不允许修改");
        }
        return abnormalMapper.updateAbnormal(abnormal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAbnormalByIds(Long[] abnormalIds)
    {
        // 已关闭的异常不允许删除
        for (Long id : abnormalIds)
        {
            MmsAbnormal abnormal = abnormalMapper.selectAbnormalById(id);
            if (abnormal != null && "2".equals(abnormal.getStatus()))
            {
                throw new ServiceException("异常[" + abnormal.getAbnormalNo() + "]已关闭，不允许删除");
            }
        }
        return abnormalMapper.deleteAbnormalByIds(abnormalIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int respondAbnormal(Long abnormalId, MmsAbnormal body)
    {
        MmsAbnormal abnormal = getAndCheckAbnormal(abnormalId);
        // 状态校验：只有待响应(0)可响应
        if (!"0".equals(abnormal.getStatus()))
        {
            throw new ServiceException("异常[" + abnormal.getAbnormalNo() + "]当前状态为" + statusName(abnormal.getStatus()) + "，只有待响应状态可响应");
        }
        abnormal.setStatus("1");
        abnormal.setResponseBy(body != null && StringUtils.isNotEmpty(body.getResponseBy()) ? body.getResponseBy() : SecurityUtils.getUsername());
        abnormal.setResponseTime(body != null && body.getResponseTime() != null ? body.getResponseTime() : new Date());
        // 响应措施、影响范围、预计恢复时间、响应备注
        if (body != null)
        {
            abnormal.setResponseMeasure(body.getResponseMeasure());
            abnormal.setImpactScope(body.getImpactScope());
            abnormal.setEstimatedRestoreTime(body.getEstimatedRestoreTime());
            abnormal.setResponseRemark(body.getResponseRemark());
        }
        abnormal.setUpdateBy(SecurityUtils.getUsername());
        abnormal.setUpdateTime(DateUtils.getNowDate());
        return abnormalMapper.updateAbnormal(abnormal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resolveAbnormal(Long abnormalId, MmsAbnormal body)
    {
        MmsAbnormal abnormal = getAndCheckAbnormal(abnormalId);
        // 状态校验：只有处理中(1)可关闭
        if (!"1".equals(abnormal.getStatus()))
        {
            throw new ServiceException("异常[" + abnormal.getAbnormalNo() + "]当前状态为" + statusName(abnormal.getStatus()) + "，只有处理中状态可关闭");
        }
        abnormal.setStatus("2");
        // 处理结果、处理方式、根本原因、预防措施、停机时长、是否追纠
        if (body != null)
        {
            abnormal.setHandleResult(body.getHandleResult());
            abnormal.setHandleMethod(body.getHandleMethod());
            abnormal.setRootCause(body.getRootCause());
            abnormal.setPreventiveMeasure(body.getPreventiveMeasure());
            abnormal.setDowntimeMinutes(body.getDowntimeMinutes());
            abnormal.setNeedPursuit(body.getNeedPursuit() != null ? body.getNeedPursuit() : "0");
        }
        // 处理人默认取响应人
        abnormal.setHandleBy(body != null && StringUtils.isNotEmpty(body.getHandleBy()) ? body.getHandleBy() : abnormal.getResponseBy());
        abnormal.setHandleTime(body != null && body.getHandleTime() != null ? body.getHandleTime() : new Date());
        abnormal.setCloseTime(new Date());
        abnormal.setUpdateBy(SecurityUtils.getUsername());
        abnormal.setUpdateTime(DateUtils.getNowDate());
        int rows = abnormalMapper.updateAbnormal(abnormal);
        // 联动关闭停机记录
        closeLinkedDowntime(abnormalId);
        return rows;
    }

    // ========== 异常↔停机联动 ==========

    /** 异常类型到停机类型的映射 */
    private String mapAbnormalTypeToDowntimeType(String abnormalType)
    {
        if (abnormalType == null) return "9";
        switch (abnormalType)
        {
            case "0": return "1"; // 设备异常 → 故障停机
            case "1": return "3"; // 物料异常 → 物料停机
            case "2": return "9"; // 质量异常 → 其他停机
            case "3": return "9"; // 安全异常 → 其他停机
            default: return "9";  // 其他 → 其他停机
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long linkDowntime(Long abnormalId)
    {
        MmsAbnormal abnormal = getAndCheckAbnormal(abnormalId);

        // 检查是否已关联停机记录
        MmsDowntime query = new MmsDowntime();
        query.setAbnormalId(abnormalId);
        List<MmsDowntime> existing = downtimeMapper.selectDowntimeList(query);
        if (existing != null && !existing.isEmpty())
        {
            throw new ServiceException("异常[" + abnormal.getAbnormalNo() + "]已关联停机记录[" + existing.get(0).getDowntimeNo() + "]，不可重复关联");
        }

        // 创建停机记录
        MmsDowntime downtime = new MmsDowntime();
        downtime.setAbnormalId(abnormalId);
        downtime.setAbnormalNo(abnormal.getAbnormalNo());
        downtime.setWorkOrderId(abnormal.getWorkOrderId());
        downtime.setResourceId(abnormal.getResourceId());
        downtime.setResourceName(abnormal.getResourceName());
        downtime.setDowntimeNo(mkNumberRuleService.generateNumber("mms_downtime"));
        downtime.setStartTime(abnormal.getReportTime() != null ? abnormal.getReportTime() : new Date());
        downtime.setDtType(mapAbnormalTypeToDowntimeType(abnormal.getAbnormalType()));
        downtime.setReason("[联动] " + (abnormal.getDescription() != null ? abnormal.getDescription() : ""));
        downtime.setStatus("0"); // 停机中
        downtime.setDelFlag("0");
        downtime.setCreateBy(SecurityUtils.getUsername());
        downtime.setCreateTime(DateUtils.getNowDate());
        downtimeMapper.insertDowntime(downtime);

        return downtime.getDowntimeId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeLinkedDowntime(Long abnormalId)
    {
        MmsDowntime query = new MmsDowntime();
        query.setAbnormalId(abnormalId);
        List<MmsDowntime> linked = downtimeMapper.selectDowntimeList(query);
        if (linked == null || linked.isEmpty()) return;

        for (MmsDowntime dt : linked)
        {
            if ("0".equals(dt.getStatus())) // 只关闭停机中的
            {
                dt.setStatus("1"); // 已恢复
                dt.setEndTime(new Date());
                if (dt.getStartTime() != null)
                {
                    long diff = dt.getEndTime().getTime() - dt.getStartTime().getTime();
                    dt.setMinutes((int) (diff / (1000 * 60)));
                }
                dt.setUpdateBy(SecurityUtils.getUsername());
                downtimeMapper.updateDowntime(dt);
            }
        }
    }

    @Override
    public Map<String, Object> getDowntimeTodayStats()
    {
        Map<String, Object> stats = new HashMap<>();
        // 查询今日停机记录（params.beginTime/endTime 为字符串，mapper 中用 date() 比较）
        String today = DateUtils.dateTimeNow("yyyy-MM-dd");
        MmsDowntime query = new MmsDowntime();
        query.getParams().put("beginTime", today);
        query.getParams().put("endTime", today);
        List<MmsDowntime> todayList = downtimeMapper.selectDowntimeList(query);

        int totalMinutes = 0;
        int count = 0;
        int activeCount = 0;
        if (todayList != null)
        {
            for (MmsDowntime dt : todayList)
            {
                count++;
                if ("0".equals(dt.getStatus())) activeCount++;
                if (dt.getMinutes() != null) totalMinutes += dt.getMinutes();
            }
        }
        stats.put("totalMinutes", totalMinutes);
        stats.put("count", count);
        stats.put("activeCount", activeCount);
        return stats;
    }

    // ========== 私有辅助方法 ==========

    private MmsAbnormal getAndCheckAbnormal(Long abnormalId)
    {
        MmsAbnormal abnormal = abnormalMapper.selectAbnormalById(abnormalId);
        if (abnormal == null)
        {
            throw new ServiceException("异常记录不存在或已删除");
        }
        return abnormal;
    }

    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "待响应";
            case "1": return "处理中";
            case "2": return "已关闭";
            default: return "未知(" + status + ")";
        }
    }

}

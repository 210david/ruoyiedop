package com.ruoyi.mms.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mms.domain.MmsAbnormal;

/**
 * 异常管理 Service接口
 *
 * 异常状态机：
 * 0(待响应) → 1(处理中) → 2(已关闭)
 *
 * @author ruoyi
 */
public interface IMmsAbnormalService
{
    /** 标准CRUD */
    public List<MmsAbnormal> selectAbnormalList(MmsAbnormal abnormal);
    public MmsAbnormal selectAbnormalById(Long abnormalId);
    public int insertAbnormal(MmsAbnormal abnormal);
    public int updateAbnormal(MmsAbnormal abnormal);
    public int deleteAbnormalByIds(Long[] abnormalIds);

    /** 异常响应：0(待响应) → 1(处理中) */
    public int respondAbnormal(Long abnormalId, com.ruoyi.mms.domain.MmsAbnormal body);

    /** 异常处理关闭：1(处理中) → 2(已关闭) */
    public int resolveAbnormal(Long abnormalId, com.ruoyi.mms.domain.MmsAbnormal body);

    /**
     * 联动生成停机记录
     * 根据异常单信息自动创建一条停机记录，建立关联关系
     * @param abnormalId 异常单ID
     * @return 生成的停机记录ID
     */
    public Long linkDowntime(Long abnormalId);

    /**
     * 异常关闭时联动关闭停机记录
     * 将关联的停机记录状态设为已恢复，填写结束时间
     * @param abnormalId 异常单ID
     */
    public void closeLinkedDowntime(Long abnormalId);

    /**
     * 看板：今日停机统计
     * @return Map containing totalMinutes, count, activeCount
     */
    public Map<String, Object> getDowntimeTodayStats();
}

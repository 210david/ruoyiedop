package com.ruoyi.mms.service;

import java.util.List;
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
    public int respondAbnormal(Long abnormalId, String responseBy);

    /** 异常处理关闭：1(处理中) → 2(已关闭) */
    public int resolveAbnormal(Long abnormalId, String handleResult);
}

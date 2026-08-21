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
import com.ruoyi.mms.domain.MmsAbnormal;
import com.ruoyi.mms.mapper.MmsAbnormalMapper;
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
    public int respondAbnormal(Long abnormalId, String responseBy)
    {
        MmsAbnormal abnormal = getAndCheckAbnormal(abnormalId);
        // 状态校验：只有待响应(0)可响应
        if (!"0".equals(abnormal.getStatus()))
        {
            throw new ServiceException("异常[" + abnormal.getAbnormalNo() + "]当前状态为" + statusName(abnormal.getStatus()) + "，只有待响应状态可响应");
        }
        abnormal.setStatus("1");
        abnormal.setResponseBy(StringUtils.isNotEmpty(responseBy) ? responseBy : SecurityUtils.getUsername());
        abnormal.setResponseTime(new Date());
        return abnormalMapper.updateAbnormal(abnormal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resolveAbnormal(Long abnormalId, String handleResult)
    {
        MmsAbnormal abnormal = getAndCheckAbnormal(abnormalId);
        // 状态校验：只有处理中(1)可关闭
        if (!"1".equals(abnormal.getStatus()))
        {
            throw new ServiceException("异常[" + abnormal.getAbnormalNo() + "]当前状态为" + statusName(abnormal.getStatus()) + "，只有处理中状态可关闭");
        }
        abnormal.setStatus("2");
        abnormal.setHandleResult(handleResult);
        abnormal.setCloseTime(new Date());
        return abnormalMapper.updateAbnormal(abnormal);
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

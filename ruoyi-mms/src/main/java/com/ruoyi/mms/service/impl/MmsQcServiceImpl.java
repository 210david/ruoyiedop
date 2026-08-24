package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsQc;
import com.ruoyi.mms.mapper.MmsQcMapper;
import com.ruoyi.mms.service.IMmsQcService;

/**
 * 过程质检 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsQcServiceImpl implements IMmsQcService
{
    @Autowired
    private MmsQcMapper qcMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsQc> selectQcList(MmsQc qc)
    {
        return qcMapper.selectQcList(qc);
    }

    @Override
    public MmsQc selectQcById(Long QcId)
    {
        return qcMapper.selectQcById(QcId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertQc(MmsQc qc)
    {
        // 自动生成质检单号
        if (StringUtils.isEmpty(qc.getQcNo()))
        {
            qc.setQcNo(mkNumberRuleService.generateNumber("mms_qc"));
        }
        // 新增时检验数量/不良数/报废数量默认值处理（数据库字段NOT NULL）
        if (qc.getInspectQty() == null)
        {
            qc.setInspectQty(0);
        }
        if (qc.getDefectQty() == null)
        {
            qc.setDefectQty(0);
        }
        if (qc.getScrapQty() == null)
        {
            qc.setScrapQty(0);
        }
        qc.setDelFlag("0");
        qc.setCreateBy(SecurityUtils.getUsername());
        qc.setCreateTime(DateUtils.getNowDate());
        return qcMapper.insertQc(qc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateQc(MmsQc qc)
    {
        return qcMapper.updateQc(qc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteQcByIds(Long[] QcIds)
    {
        return qcMapper.deleteQcByIds(QcIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int inspectQc(MmsQc qc)
    {
        MmsQc existing = qcMapper.selectQcById(qc.getQcId());
        if (existing == null)
        {
            throw new com.ruoyi.common.exception.ServiceException("质检记录不存在");
        }
        // 已有检验结果的不允许重复质检
        if (StringUtils.isNotEmpty(existing.getQcResult()))
        {
            throw new com.ruoyi.common.exception.ServiceException("该质检单已检验，不可重复质检");
        }
        // 更新检验相关字段
        existing.setInspectQty(qc.getInspectQty());
        existing.setDefectQty(qc.getDefectQty());
        existing.setScrapQty(qc.getScrapQty());
        existing.setDefectType(qc.getDefectType());
        existing.setQcResult(qc.getQcResult());
        existing.setQcBy(qc.getQcBy());
        existing.setQcTime(qc.getQcTime() != null ? qc.getQcTime() : DateUtils.getNowDate());
        existing.setRemark(qc.getRemark());
        existing.setUpdateBy(SecurityUtils.getUsername());
        existing.setUpdateTime(DateUtils.getNowDate());
        return qcMapper.updateQc(existing);
    }
}

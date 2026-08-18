package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        qc.setDelFlag("0");
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
}

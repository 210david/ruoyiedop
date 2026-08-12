package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.qms.domain.QmsGauge;
import com.ruoyi.qms.mapper.QmsGaugeMapper;
import com.ruoyi.qms.service.IQmsGaugeService;

/**
 * 量检具台账 Service实现
 *
 * @author ruoyi
 */
@Service
public class QmsGaugeServiceImpl implements IQmsGaugeService
{
    @Autowired
    private QmsGaugeMapper qmsGaugeMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<QmsGauge> selectGaugeList(QmsGauge gauge)
    {
        return qmsGaugeMapper.selectGaugeList(gauge);
    }

    @Override
    public QmsGauge selectGaugeById(Long gaugeId)
    {
        return qmsGaugeMapper.selectGaugeById(gaugeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertGauge(QmsGauge gauge)
    {
        // 编号为空时自动生成
        if (StringUtils.isEmpty(gauge.getGaugeNo()))
        {
            gauge.setGaugeNo(mkNumberRuleService.generateNumber("qms_gauge"));
        }
        else
        {
            QmsGauge existing = qmsGaugeMapper.selectGaugeByNo(gauge.getGaugeNo());
            if (existing != null)
            {
                throw new ServiceException("量检具编号已存在：" + gauge.getGaugeNo());
            }
        }
        gauge.setDelFlag("0");
        if (gauge.getStatus() == null)
        {
            gauge.setStatus("0");
        }
        if (gauge.getGaugeStatus() == null)
        {
            gauge.setGaugeStatus("0");
        }
        return qmsGaugeMapper.insertGauge(gauge);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateGauge(QmsGauge gauge)
    {
        return qmsGaugeMapper.updateGauge(gauge);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteGaugeByIds(Long[] gaugeIds)
    {
        return qmsGaugeMapper.deleteGaugeByIds(gaugeIds);
    }
}

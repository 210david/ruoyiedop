package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsDemand;
import com.ruoyi.mms.mapper.MmsDemandMapper;
import com.ruoyi.mms.service.IMmsDemandService;

/**
 * 生产需求 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsDemandServiceImpl implements IMmsDemandService
{
    @Autowired
    private MmsDemandMapper demandMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsDemand> selectDemandList(MmsDemand demand)
    {
        return demandMapper.selectDemandList(demand);
    }

    @Override
    public MmsDemand selectDemandById(Long DemandId)
    {
        return demandMapper.selectDemandById(DemandId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDemand(MmsDemand demand)
    {
        // 自动生成需求计划编号
        if (StringUtils.isEmpty(demand.getDemandNo()))
        {
            demand.setDemandNo(mkNumberRuleService.generateNumber("mms_demand"));
        }
        demand.setDelFlag("0");
        return demandMapper.insertDemand(demand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDemand(MmsDemand demand)
    {
        return demandMapper.updateDemand(demand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDemandByIds(Long[] DemandIds)
    {
        return demandMapper.deleteDemandByIds(DemandIds);
    }
}

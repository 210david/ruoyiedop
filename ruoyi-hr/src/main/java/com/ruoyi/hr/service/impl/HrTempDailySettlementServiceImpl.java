package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrTempDailySettlement;
import com.ruoyi.hr.mapper.HrTempDailySettlementMapper;
import com.ruoyi.hr.service.IHrTempDailySettlementService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class HrTempDailySettlementServiceImpl implements IHrTempDailySettlementService
{
    @Autowired
    private HrTempDailySettlementMapper hrTempDailySettlementMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrTempDailySettlement selectHrTempDailySettlementById(Long settlementId) { return hrTempDailySettlementMapper.selectHrTempDailySettlementById(settlementId); }

    @Override
    public List<HrTempDailySettlement> selectHrTempDailySettlementList(HrTempDailySettlement hrTempDailySettlement) { return hrTempDailySettlementMapper.selectHrTempDailySettlementList(hrTempDailySettlement); }

    @Override
    public int insertHrTempDailySettlement(HrTempDailySettlement hrTempDailySettlement)
    {
        if (StringUtils.isEmpty(hrTempDailySettlement.getSettlementNo()))
        {
            hrTempDailySettlement.setSettlementNo(mkNumberRuleService.generateNumber("hr_temp_settlement"));
        }
        return hrTempDailySettlementMapper.insertHrTempDailySettlement(hrTempDailySettlement);
    }

    @Override
    public int updateHrTempDailySettlement(HrTempDailySettlement hrTempDailySettlement) { return hrTempDailySettlementMapper.updateHrTempDailySettlement(hrTempDailySettlement); }

    @Override
    public int deleteHrTempDailySettlementByIds(Long[] settlementIds) { return hrTempDailySettlementMapper.deleteHrTempDailySettlementByIds(settlementIds); }

    @Override
    public int deleteHrTempDailySettlementById(Long settlementId) { return hrTempDailySettlementMapper.deleteHrTempDailySettlementById(settlementId); }
}

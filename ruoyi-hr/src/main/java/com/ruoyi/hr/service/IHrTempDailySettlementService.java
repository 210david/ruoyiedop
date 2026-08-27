package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrTempDailySettlement;

public interface IHrTempDailySettlementService {
    public HrTempDailySettlement selectHrTempDailySettlementById(Long settlementId);
    public List<HrTempDailySettlement> selectHrTempDailySettlementList(HrTempDailySettlement hrTempDailySettlement);
    public int insertHrTempDailySettlement(HrTempDailySettlement hrTempDailySettlement);
    public int updateHrTempDailySettlement(HrTempDailySettlement hrTempDailySettlement);
    public int deleteHrTempDailySettlementByIds(Long[] settlementIds);
    public int deleteHrTempDailySettlementById(Long settlementId);
}

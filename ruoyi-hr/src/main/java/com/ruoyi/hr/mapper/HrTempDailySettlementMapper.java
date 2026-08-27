package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrTempDailySettlement;

public interface HrTempDailySettlementMapper {
    public HrTempDailySettlement selectHrTempDailySettlementById(Long settlementId);
    public List<HrTempDailySettlement> selectHrTempDailySettlementList(HrTempDailySettlement hrTempDailySettlement);
    public int insertHrTempDailySettlement(HrTempDailySettlement hrTempDailySettlement);
    public int updateHrTempDailySettlement(HrTempDailySettlement hrTempDailySettlement);
    public int deleteHrTempDailySettlementByIds(Long[] settlementIds);
    public int deleteHrTempDailySettlementById(Long settlementId);
}

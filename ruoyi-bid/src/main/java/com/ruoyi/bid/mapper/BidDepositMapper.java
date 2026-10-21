package com.ruoyi.bid.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.bid.domain.BidDeposit;

/**
 * 保证金台账 Mapper 接口
 */
public interface BidDepositMapper
{
    public List<BidDeposit> selectDepositList(BidDeposit deposit);

    public BidDeposit selectDepositById(Long depositId);

    public List<BidDeposit> selectDepositByProjectId(Long projectId);

    public List<BidDeposit> selectOverdueDeposits();

    public int insertDeposit(BidDeposit deposit);

    public int updateDeposit(BidDeposit deposit);

    public int deleteDepositByIds(Long[] depositIds);

    public Map<String, Object> selectOccupancySummary();
}

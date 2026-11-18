package com.ruoyi.flowable.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.flowable.domain.FlowLedger;
import com.ruoyi.flowable.domain.FlowLedgerGrant;

/**
 * 流程台账 Mapper 接口
 *
 * @author ruoyi
 */
public interface FlowLedgerMapper
{
    List<FlowLedger> selectFlowLedgerList(FlowLedger query);

    FlowLedger selectFlowLedgerById(Long ledgerId);

    FlowLedger selectFlowLedgerByKey(String ledgerKey);

    /** 统计台账标识占用情况（含已逻辑删除数据，避免唯一索引冲突） */
    int countByLedgerKey(String ledgerKey);

    List<FlowLedger> selectActiveLedgerList();

    int insertFlowLedger(FlowLedger ledger);

    int updateFlowLedger(FlowLedger ledger);

    int deleteFlowLedgerByIds(Long[] ledgerIds);

    /** 回写台账对应的前台菜单ID */
    int updateLedgerMenuId(@Param("ledgerId") Long ledgerId, @Param("menuId") Long menuId);

    List<FlowLedgerGrant> selectGrantList(FlowLedgerGrant query);

    int insertGrant(FlowLedgerGrant grant);

    int updateGrant(FlowLedgerGrant grant);

    int deleteGrantByIds(@Param("grantIds") Long[] grantIds);

    int deleteGrantByLedgerId(Long ledgerId);
}

package com.ruoyi.flowable.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.flowable.domain.FlowLedger;
import com.ruoyi.flowable.domain.FlowLedgerGrant;

/**
 * 流程台账 Service
 *
 * @author ruoyi
 */
public interface IFlowLedgerService
{
    /** 后台：台账定义列表 */
    List<FlowLedger> selectFlowLedgerList(FlowLedger query);

    /** 后台：台账定义详情 */
    FlowLedger selectFlowLedgerById(Long ledgerId);

    int insertFlowLedger(FlowLedger ledger);

    int updateFlowLedger(FlowLedger ledger);

    int deleteFlowLedgerByIds(Long[] ledgerIds);

    /** 后台：授权列表 */
    List<FlowLedgerGrant> selectGrantList(Long ledgerId);

    int insertGrant(FlowLedgerGrant grant);

    int updateGrant(FlowLedgerGrant grant);

    int deleteGrantByIds(Long[] grantIds);

    /** 后台：表单可配置字段候选（系统字段 + 主表字段） */
    List<Map<String, Object>> selectLedgerFields(Long formId);

    /** 前台：当前用户可见的台账 */
    List<FlowLedger> selectMyLedgers();

    /** 前台：台账展示配置（列、默认排序、导出权限） */
    Map<String, Object> selectPortalConfig(Long ledgerId);

    /** 前台：台账分页数据 */
    Map<String, Object> selectLedgerData(Long ledgerId, Map<String, String> params);

    /** 前台：台账导出（xlsx） */
    byte[] exportLedgerData(Long ledgerId, Map<String, String> params);

    /**
     * 流程发起时登记实例快照（冻结发起部门，供台账数据权限使用）
     */
    void registerInstanceExt(String instanceId, Long formId, String formKey, String processKey,
            String businessKey, String title, String startUserId, Long startDeptId);
}

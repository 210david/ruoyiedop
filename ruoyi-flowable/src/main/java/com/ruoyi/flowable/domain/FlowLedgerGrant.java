package com.ruoyi.flowable.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程台账授权 flow_ledger_grant
 *
 * grantType 表示「授权给谁」（全员/公司/部门/角色/用户），
 * scopeType 表示「能看到哪些数据」（全部/本人/本部门/本部门及下级/本公司/本公司及下级/自定义部门/自定义人员）。
 *
 * @author ruoyi
 */
public class FlowLedgerGrant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 授权ID */
    private Long grantId;

    /** 台账ID */
    private Long ledgerId;

    /** 授权对象类型（all/company/dept/role/user） */
    private String grantType;

    /** 授权对象ID（company/dept/role/user） */
    private Long targetId;

    /** 数据范围类型 */
    private String scopeType;

    /** 自定义范围JSON {deptIds:[],includeChild:true,userIds:[]} */
    private String scopeJson;

    /** 是否允许导出（0否 1是） */
    private String canExport;

    /** 授权对象名称（查询回显，不落库） */
    private String grantName;

    public Long getGrantId() { return grantId; }
    public void setGrantId(Long grantId) { this.grantId = grantId; }
    public Long getLedgerId() { return ledgerId; }
    public void setLedgerId(Long ledgerId) { this.ledgerId = ledgerId; }
    public String getGrantType() { return grantType; }
    public void setGrantType(String grantType) { this.grantType = grantType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public String getScopeJson() { return scopeJson; }
    public void setScopeJson(String scopeJson) { this.scopeJson = scopeJson; }
    public String getCanExport() { return canExport; }
    public void setCanExport(String canExport) { this.canExport = canExport; }
    public String getGrantName() { return grantName; }
    public void setGrantName(String grantName) { this.grantName = grantName; }
}

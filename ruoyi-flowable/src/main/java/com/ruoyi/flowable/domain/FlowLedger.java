package com.ruoyi.flowable.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程台账定义 flow_ledger
 *
 * 一个台账绑定一个流程表单，列与可见数据范围均由配置驱动（虚拟台账，实时查询）。
 *
 * @author ruoyi
 */
public class FlowLedger extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 台账ID */
    private Long ledgerId;

    /** 台账标识 */
    private String ledgerKey;

    /** 台账名称 */
    private String ledgerName;

    /** 台账说明 */
    private String ledgerDesc;

    /** 流程分类（字典 flowable_category） */
    private String category;

    /** 绑定表单ID */
    private Long formId;

    /** 表单Key */
    private String formKey;

    /** 表单名称（列表回显，不落库） */
    private String formName;

    /** 流程定义Key */
    private String processKey;

    /** 表单数据表名 */
    private String tableName;

    /** 列配置JSON */
    private String columnsJson;

    /** 查询配置JSON {defaultSort, defaultSortOrder} */
    private String queryJson;

    /** 是否允许导出（0否 1是） */
    private String exportFlag;

    /** 状态（0启用 1停用） */
    private String status;

    /** 显示顺序 */
    private Integer orderNum;

    /** 前台菜单ID（一个台账对应一个二级菜单） */
    private Long menuId;

    /** 删除标志 */
    private String delFlag;

    public Long getLedgerId() { return ledgerId; }
    public void setLedgerId(Long ledgerId) { this.ledgerId = ledgerId; }
    public String getLedgerKey() { return ledgerKey; }
    public void setLedgerKey(String ledgerKey) { this.ledgerKey = ledgerKey; }
    public String getLedgerName() { return ledgerName; }
    public void setLedgerName(String ledgerName) { this.ledgerName = ledgerName; }
    public String getLedgerDesc() { return ledgerDesc; }
    public void setLedgerDesc(String ledgerDesc) { this.ledgerDesc = ledgerDesc; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }
    public String getFormKey() { return formKey; }
    public void setFormKey(String formKey) { this.formKey = formKey; }
    public String getFormName() { return formName; }
    public void setFormName(String formName) { this.formName = formName; }
    public String getProcessKey() { return processKey; }
    public void setProcessKey(String processKey) { this.processKey = processKey; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getColumnsJson() { return columnsJson; }
    public void setColumnsJson(String columnsJson) { this.columnsJson = columnsJson; }
    public String getQueryJson() { return queryJson; }
    public void setQueryJson(String queryJson) { this.queryJson = queryJson; }
    public String getExportFlag() { return exportFlag; }
    public void setExportFlag(String exportFlag) { this.exportFlag = exportFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}

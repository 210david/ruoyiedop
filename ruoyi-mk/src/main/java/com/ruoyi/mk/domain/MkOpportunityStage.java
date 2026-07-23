package com.ruoyi.mk.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 销售阶段配置 mk_opportunity_stage
 *
 * @author ruoyi
 */
public class MkOpportunityStage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 阶段ID */
    private Long stageId;

    /** 阶段编码 */
    @Excel(name = "阶段编码")
    private String stageCode;

    /** 阶段名称 */
    @Excel(name = "阶段名称")
    private String stageName;

    /** 排序号 */
    @Excel(name = "排序")
    private Integer sort;

    /** 赢率 */
    @Excel(name = "赢率(%)")
    private Integer winRate;

    /** 最大停留天数 */
    @Excel(name = "最大停留天数")
    private Integer maxDays;

    /** 阶段推进必填字段 */
    private String requiredFields;

    /** 状态 */
    @Excel(name = "状态", dictType = "sys_normal_disable")
    private String status;

    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }
    public String getStageCode() { return stageCode; }
    public void setStageCode(String stageCode) { this.stageCode = stageCode; }
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getWinRate() { return winRate; }
    public void setWinRate(Integer winRate) { this.winRate = winRate; }
    public Integer getMaxDays() { return maxDays; }
    public void setMaxDays(Integer maxDays) { this.maxDays = maxDays; }
    public String getRequiredFields() { return requiredFields; }
    public void setRequiredFields(String requiredFields) { this.requiredFields = requiredFields; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

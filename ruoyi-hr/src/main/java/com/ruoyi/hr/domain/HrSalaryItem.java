package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;

public class HrSalaryItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long itemId;
    @NotBlank(message = "编码不能为空")
    @Excel(name = "编码")
    private String itemCode;
    @NotBlank(message = "名称不能为空")
    @Excel(name = "名称")
    private String itemName;
    @Excel(name = "类别")
    private String itemCategory;
    @Excel(name = "计算规则")
    private String calcRule;
    @Excel(name = "加项")
    private Integer isAdd;
    @Excel(name = "行业标签")
    private String industryTag;
    @Excel(name = "排序号")
    private Integer orderNum;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemCategory() { return itemCategory; }
    public void setItemCategory(String itemCategory) { this.itemCategory = itemCategory; }
    public String getCalcRule() { return calcRule; }
    public void setCalcRule(String calcRule) { this.calcRule = calcRule; }
    public Integer getIsAdd() { return isAdd; }
    public void setIsAdd(Integer isAdd) { this.isAdd = isAdd; }
    public String getIndustryTag() { return industryTag; }
    public void setIndustryTag(String industryTag) { this.industryTag = industryTag; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}

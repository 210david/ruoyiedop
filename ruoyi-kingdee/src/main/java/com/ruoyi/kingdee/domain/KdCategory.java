package com.ruoyi.kingdee.domain;

/** 金蝶商品类别（用于库存分类树多级展示） */
public class KdCategory
{
    private String id;
    private String number;
    private String name;
    private String parentId;
    private Integer level;
    private Integer sort;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}

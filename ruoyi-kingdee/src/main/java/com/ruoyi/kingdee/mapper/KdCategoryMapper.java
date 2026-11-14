package com.ruoyi.kingdee.mapper;

import java.util.List;
import com.ruoyi.kingdee.domain.KdCategory;

/** 金蝶商品类别 Mapper */
public interface KdCategoryMapper
{
    int deleteAll();

    int batchInsert(List<KdCategory> list);

    List<KdCategory> selectAllOrdered();
}

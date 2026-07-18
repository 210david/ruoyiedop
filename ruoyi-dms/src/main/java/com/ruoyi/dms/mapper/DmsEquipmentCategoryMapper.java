package com.ruoyi.dms.mapper;

import java.util.List;
import com.ruoyi.dms.domain.DmsEquipmentCategory;

public interface DmsEquipmentCategoryMapper
{
    public List<DmsEquipmentCategory> selectCategoryList(DmsEquipmentCategory category);
    public DmsEquipmentCategory selectCategoryById(Long categoryId);
    public int countChildByParentId(Long parentId);
    public int insertCategory(DmsEquipmentCategory category);
    public int updateCategory(DmsEquipmentCategory category);
    public int deleteCategoryByIds(Long[] categoryIds);
}

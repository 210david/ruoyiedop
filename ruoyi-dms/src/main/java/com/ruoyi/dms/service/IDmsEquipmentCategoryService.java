package com.ruoyi.dms.service;

import java.util.List;
import com.ruoyi.dms.domain.DmsEquipmentCategory;

public interface IDmsEquipmentCategoryService
{
    public List<DmsEquipmentCategory> selectCategoryList(DmsEquipmentCategory category);
    public DmsEquipmentCategory selectCategoryById(Long categoryId);
    public int insertCategory(DmsEquipmentCategory category);
    public int updateCategory(DmsEquipmentCategory category);
    public int deleteCategoryByIds(Long[] categoryIds);
}

package com.ruoyi.dms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.dms.domain.DmsEquipmentCategory;
import com.ruoyi.dms.mapper.DmsEquipmentCategoryMapper;
import com.ruoyi.dms.service.IDmsEquipmentCategoryService;

@Service
public class DmsEquipmentCategoryServiceImpl implements IDmsEquipmentCategoryService
{
    @Autowired
    private DmsEquipmentCategoryMapper dmsEquipmentCategoryMapper;

    @Override
    public List<DmsEquipmentCategory> selectCategoryList(DmsEquipmentCategory category)
    {
        return dmsEquipmentCategoryMapper.selectCategoryList(category);
    }

    @Override
    public DmsEquipmentCategory selectCategoryById(Long categoryId)
    {
        return dmsEquipmentCategoryMapper.selectCategoryById(categoryId);
    }

    @Override
    public int insertCategory(DmsEquipmentCategory category)
    {
        category.setDelFlag("0");
        buildAncestors(category);
        return dmsEquipmentCategoryMapper.insertCategory(category);
    }

    @Override
    public int updateCategory(DmsEquipmentCategory category)
    {
        DmsEquipmentCategory old = dmsEquipmentCategoryMapper.selectCategoryById(category.getCategoryId());
        if (old != null && category.getParentId() != null
                && !category.getParentId().equals(old.getParentId()))
        {
            buildAncestors(category);
        }
        return dmsEquipmentCategoryMapper.updateCategory(category);
    }

    @Override
    public int deleteCategoryByIds(Long[] categoryIds)
    {
        for (Long id : categoryIds)
        {
            int childCount = dmsEquipmentCategoryMapper.countChildByParentId(id);
            if (childCount > 0)
            {
                DmsEquipmentCategory cat = dmsEquipmentCategoryMapper.selectCategoryById(id);
                String name = cat != null ? cat.getCategoryName() : String.valueOf(id);
                throw new ServiceException("[" + name + "] 存在子分类，不允许删除");
            }
        }
        return dmsEquipmentCategoryMapper.deleteCategoryByIds(categoryIds);
    }

    private void buildAncestors(DmsEquipmentCategory category)
    {
        Long parentId = category.getParentId();
        if (parentId == null || parentId == 0L)
        {
            category.setAncestors("0");
        }
        else
        {
            DmsEquipmentCategory parent = dmsEquipmentCategoryMapper.selectCategoryById(parentId);
            if (parent != null)
            {
                category.setAncestors(parent.getAncestors() + "," + parentId);
            }
            else
            {
                category.setAncestors("0");
            }
        }
    }
}

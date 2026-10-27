package com.ruoyi.pdm.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.pdm.domain.PdmProductCategory;
import com.ruoyi.pdm.mapper.PdmProductCategoryMapper;
import com.ruoyi.pdm.mapper.PdmProductMapper;
import com.ruoyi.pdm.service.IPdmProductCategoryService;

/**
 * 产品分类 Service 实现
 *
 * @author ruoyi
 */
@Service
public class PdmProductCategoryServiceImpl implements IPdmProductCategoryService
{
    @Autowired
    private PdmProductCategoryMapper categoryMapper;

    @Autowired
    private PdmProductMapper productMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<PdmProductCategory> selectCategoryList(PdmProductCategory category)
    {
        return categoryMapper.selectCategoryList(category);
    }

    @Override
    public List<PdmProductCategory> buildCategoryTree(List<PdmProductCategory> categories)
    {
        List<PdmProductCategory> tree = new ArrayList<>();
        for (PdmProductCategory node : categories)
        {
            if (node.getParentId() == null || node.getParentId() == 0L)
            {
                buildChildren(node, categories);
                tree.add(node);
            }
        }
        return tree;
    }

    private void buildChildren(PdmProductCategory parent, List<PdmProductCategory> categories)
    {
        for (PdmProductCategory node : categories)
        {
            if (parent.getCategoryId().equals(node.getParentId()))
            {
                buildChildren(node, categories);
                parent.getChildren().add(node);
            }
        }
    }

    @Override
    public PdmProductCategory selectCategoryById(Long categoryId)
    {
        return categoryMapper.selectCategoryById(categoryId);
    }

    @Override
    public int insertCategory(PdmProductCategory category)
    {
        checkNameUnique(category);
        // 祖级路径
        if (category.getParentId() == null || category.getParentId() == 0L)
        {
            category.setParentId(0L);
            category.setAncestors("0");
        }
        else
        {
            PdmProductCategory parent = categoryMapper.selectCategoryById(category.getParentId());
            if (parent == null)
            {
                throw new ServiceException("上级分类不存在或已停用");
            }
            category.setAncestors(parent.getAncestors() + "," + parent.getCategoryId());
        }
        // 分类编码自动生成
        category.setCategoryCode(mkNumberRuleService.generateNumber("pdm_category"));
        category.setStatus(StringUtils.isBlank(category.getStatus()) ? "0" : category.getStatus());
        // 记录创建人与创建时间
        category.setCreateBy(SecurityUtils.getUsername());
        category.setCreateTime(DateUtils.getNowDate());
        return categoryMapper.insertCategory(category);
    }

    @Override
    public int updateCategory(PdmProductCategory category)
    {
        checkNameUnique(category);
        PdmProductCategory exist = categoryMapper.selectCategoryById(category.getCategoryId());
        if (exist == null)
        {
            throw new ServiceException("分类不存在或已删除");
        }
        if (category.getCategoryId().equals(category.getParentId()))
        {
            throw new ServiceException("上级分类不能选择自己");
        }
        return categoryMapper.updateCategory(category);
    }

    @Override
    public int deleteCategoryByIds(Long[] categoryIds)
    {
        for (Long categoryId : categoryIds)
        {
            if (categoryMapper.hasChildByCategoryId(categoryId) > 0)
            {
                throw new ServiceException("存在下级分类，不允许删除");
            }
            if (productMapper.countProductByCategoryId(categoryId) > 0)
            {
                throw new ServiceException("分类下存在产品档案，不允许删除");
            }
        }
        return categoryMapper.deleteCategoryByIds(categoryIds);
    }

    private void checkNameUnique(PdmProductCategory category)
    {
        PdmProductCategory query = new PdmProductCategory();
        query.setCategoryName(category.getCategoryName());
        query.setParentId(category.getParentId() == null ? 0L : category.getParentId());
        PdmProductCategory exist = categoryMapper.checkCategoryNameUnique(query);
        if (exist != null && !exist.getCategoryId().equals(category.getCategoryId()))
        {
            throw new ServiceException("同级下已存在同名分类：" + category.getCategoryName());
        }
    }
}

package com.ruoyi.pdm.service;

import java.util.List;
import com.ruoyi.pdm.domain.PdmProductCategory;

/**
 * 产品分类 Service 接口
 *
 * @author ruoyi
 */
public interface IPdmProductCategoryService
{
    /** 查询产品分类列表（平铺） */
    public List<PdmProductCategory> selectCategoryList(PdmProductCategory category);

    /** 查询产品分类树 */
    public List<PdmProductCategory> buildCategoryTree(List<PdmProductCategory> categories);

    /** 根据ID查询 */
    public PdmProductCategory selectCategoryById(Long categoryId);

    /** 新增（分类编码自动生成） */
    public int insertCategory(PdmProductCategory category);

    /** 修改 */
    public int updateCategory(PdmProductCategory category);

    /** 批量删除（校验子分类与产品引用） */
    public int deleteCategoryByIds(Long[] categoryIds);
}

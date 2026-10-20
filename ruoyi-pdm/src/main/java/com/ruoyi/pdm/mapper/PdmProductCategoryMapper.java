package com.ruoyi.pdm.mapper;

import java.util.List;
import com.ruoyi.pdm.domain.PdmProductCategory;

/**
 * 产品分类 Mapper 接口
 *
 * @author ruoyi
 */
public interface PdmProductCategoryMapper
{
    /** 查询产品分类列表 */
    public List<PdmProductCategory> selectCategoryList(PdmProductCategory category);

    /** 根据ID查询 */
    public PdmProductCategory selectCategoryById(Long categoryId);

    /** 校验分类名称是否唯一 */
    public PdmProductCategory checkCategoryNameUnique(PdmProductCategory category);

    /** 是否存在子分类 */
    public int hasChildByCategoryId(Long categoryId);

    /** 新增 */
    public int insertCategory(PdmProductCategory category);

    /** 修改 */
    public int updateCategory(PdmProductCategory category);

    /** 批量删除（软删除） */
    public int deleteCategoryByIds(Long[] categoryIds);
}

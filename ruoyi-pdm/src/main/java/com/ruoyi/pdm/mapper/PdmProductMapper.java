package com.ruoyi.pdm.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.pdm.domain.PdmProduct;

/**
 * 产品档案 Mapper 接口
 *
 * @author ruoyi
 */
public interface PdmProductMapper
{
    /** 查询产品档案列表 */
    public List<PdmProduct> selectProductList(PdmProduct product);

    /** 根据ID查询 */
    public PdmProduct selectProductById(Long productId);

    /** 根据物料ID查询 */
    public PdmProduct selectProductByMaterialId(Long materialId);

    /** 校验物料是否已被其他产品档案挂接 */
    public PdmProduct checkMaterialUsed(@Param("materialId") Long materialId, @Param("productId") Long productId);

    /** 按产品编码查询（含已删除记录，配合数据库唯一键校验） */
    public PdmProduct selectProductByCode(String productCode);

    /** 查询物料类型（wms_material.material_type，'2'=成品） */
    public String selectMaterialType(Long materialId);

    /** 统计被引用的分类下产品数 */
    public int countProductByCategoryId(Long categoryId);

    /** 新增 */
    public int insertProduct(PdmProduct product);

    /** 修改 */
    public int updateProduct(PdmProduct product);

    /** 批量删除（软删除） */
    public int deleteProductByIds(Long[] productIds);
}

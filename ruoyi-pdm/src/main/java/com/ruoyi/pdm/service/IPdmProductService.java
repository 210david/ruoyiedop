package com.ruoyi.pdm.service;

import java.util.List;
import com.ruoyi.pdm.domain.PdmProduct;

/**
 * 产品档案 Service 接口
 *
 * @author ruoyi
 */
public interface IPdmProductService
{
    /** 查询产品档案列表 */
    public List<PdmProduct> selectProductList(PdmProduct product);

    /** 根据ID查询（含版本记录聚合） */
    public PdmProduct selectProductById(Long productId);

    /** 新增（产品编码=物料编码，物料唯一挂接校验） */
    public int insertProduct(PdmProduct product);

    /** 修改（产品编码启用后不可修改；已定型状态禁止手工变更） */
    public int updateProduct(PdmProduct product);

    /** 批量删除（仅限"设计中"且无下游引用） */
    public int deleteProductByIds(Long[] productIds);

    /**
     * 产品停产（已定型 → 已停产；生命周期终止动作，不走变更单）
     */
    public int discontinueProduct(Long productId, String remark);

    /**
     * 产品复产（已停产 → 已定型）
     */
    public int resumeProduct(Long productId);
}

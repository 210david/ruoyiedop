package com.ruoyi.pdm.service.impl;

import com.ruoyi.common.annotation.DataScope;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pdm.domain.PdmProduct;
import com.ruoyi.pdm.domain.PdmProductCategory;
import com.ruoyi.pdm.mapper.PdmProductMapper;
import com.ruoyi.pdm.mapper.PdmProductCategoryMapper;
import com.ruoyi.pdm.service.IPdmProductService;

/**
 * 产品档案 Service 实现
 *
 * 业务规则（设计方案 7.1）：
 * 1. 产品编码=物料编码（D2 决策），启用后不允许修改；
 * 2. 产品状态"已定型"必须经由设计方案定稿流程触发，不允许手工直接改状态；
 * 3. 已定型产品只允许停用（停产），不允许删除；删除仅限"设计中"产品。
 *
 * @author ruoyi
 */
@Service
public class PdmProductServiceImpl implements IPdmProductService
{
    @Autowired
    private PdmProductMapper productMapper;

    @Autowired
    private PdmProductCategoryMapper categoryMapper;

    @Override
    @DataScope(deptAlias = "pdm_product")
    public List<PdmProduct> selectProductList(PdmProduct product)
    {
        return productMapper.selectProductList(product);
    }

    @Override
    public PdmProduct selectProductById(Long productId)
    {
        return productMapper.selectProductById(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertProduct(PdmProduct product)
    {
        checkMaterial(product, null);
        // 产品编码=物料编码（唯一主数据源）
        product.setProductCode(StringUtils.trimToEmpty(product.getProductCode()));
        if (StringUtils.isEmpty(product.getProductCode()))
        {
            throw new ServiceException("请选择产品对应的物料（产品编码取自物料编码）");
        }
        // 已定型不可手工设置
        if ("1".equals(product.getProductStatus()))
        {
            throw new ServiceException("产品状态\"已定型\"必须经由设计方案定稿流程触发，不允许手工设置");
        }
        product.setProductStatus(StringUtils.isBlank(product.getProductStatus()) ? "0" : product.getProductStatus());
        // 按分类ID回填分类名称，保证列表/统计展示
        if (product.getCategoryId() != null && StringUtils.isBlank(product.getCategoryName()))
        {
            PdmProductCategory category = categoryMapper.selectCategoryById(product.getCategoryId());
            if (category != null)
            {
                product.setCategoryName(category.getCategoryName());
            }
        }
        // 记录创建人与创建时间
        product.setCreateBy(SecurityUtils.getUsername());
        product.setCreateTime(DateUtils.getNowDate());
        return productMapper.insertProduct(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProduct(PdmProduct product)
    {
        PdmProduct exist = productMapper.selectProductById(product.getProductId());
        if (exist == null)
        {
            throw new ServiceException("产品档案不存在或已删除");
        }
        // 已停产产品为终态保留追溯，不允许修改
        if ("2".equals(exist.getProductStatus()))
        {
            throw new ServiceException("产品【" + exist.getMaterialName() + "】已停产，不允许修改，如需调整请先复产");
        }
        // 产品编码启用后不允许修改（物料更换需重新校验唯一挂接）
        if (product.getMaterialId() != null && !product.getMaterialId().equals(exist.getMaterialId()))
        {
            checkMaterial(product, product.getProductId());
            product.setProductCode(exist.getProductCode());
        }
        else
        {
            product.setProductCode(null);
        }
        // 已定型状态只允许通过定稿流程进入/离开，不允许手工变更
        if (product.getProductStatus() != null && !product.getProductStatus().equals(exist.getProductStatus()))
        {
            if ("1".equals(product.getProductStatus()))
            {
                throw new ServiceException("产品状态\"已定型\"必须经由设计方案定稿流程触发，不允许手工设置");
            }
            if ("1".equals(exist.getProductStatus()) && "0".equals(product.getProductStatus()))
            {
                throw new ServiceException("已定型产品不允许退回\"设计中\"，如需调整请走技术变更流程");
            }
        }
        return productMapper.updateProduct(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProductByIds(Long[] productIds)
    {
        for (Long productId : productIds)
        {
            PdmProduct exist = productMapper.selectProductById(productId);
            if (exist == null)
            {
                continue;
            }
            if (!"0".equals(exist.getProductStatus()))
            {
                throw new ServiceException("产品【" + exist.getMaterialName() + "】不是\"设计中\"状态，已定型/已停产产品只允许停用，不允许删除");
            }
        }
        // TODO 第三批：校验无BOM下发记录、无订单引用后方可删除
        return productMapper.deleteProductByIds(productIds);
    }

    /** 产品停产（已定型 → 已停产；生命周期终止动作，不走变更单） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int discontinueProduct(Long productId, String remark)
    {
        PdmProduct exist = productMapper.selectProductById(productId);
        if (exist == null)
        {
            throw new ServiceException("产品档案不存在或已删除");
        }
        if (!"1".equals(exist.getProductStatus()))
        {
            throw new ServiceException("仅「已定型」产品允许停产；\"设计中\"产品可直接删除");
        }
        PdmProduct up = new PdmProduct();
        up.setProductId(productId);
        up.setProductStatus("2");
        up.setRemark(remark);
        up.setUpdateBy(SecurityUtils.getUsername());
        return productMapper.updateProduct(up);
    }

    /** 产品复产（已停产 → 已定型，用于产品复产） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resumeProduct(Long productId)
    {
        PdmProduct exist = productMapper.selectProductById(productId);
        if (exist == null)
        {
            throw new ServiceException("产品档案不存在或已删除");
        }
        if (!"2".equals(exist.getProductStatus()))
        {
            throw new ServiceException("仅「已停产」产品允许复产");
        }
        PdmProduct up = new PdmProduct();
        up.setProductId(productId);
        up.setProductStatus("1");
        up.setUpdateBy(SecurityUtils.getUsername());
        return productMapper.updateProduct(up);
    }

    /** 校验物料唯一挂接 */
    private void checkMaterial(PdmProduct product, Long excludeProductId)
    {
        if (product.getMaterialId() == null)
        {
            throw new ServiceException("请选择产品对应的物料");
        }
        // 产品档案仅允许挂接成品类型物料（material_type='2'）
        String materialType = productMapper.selectMaterialType(product.getMaterialId());
        if (materialType == null)
        {
            throw new ServiceException("所选物料不存在或已删除，请重新选择");
        }
        if (!"2".equals(materialType))
        {
            throw new ServiceException("产品档案仅可挂接物料类型为「成品」的物料，请重新选择");
        }
        PdmProduct used = productMapper.checkMaterialUsed(product.getMaterialId(), excludeProductId);
        if (used != null)
        {
            throw new ServiceException("物料【" + used.getProductCode() + "】已挂接产品档案：" + used.getMaterialName());
        }
        // 产品编码唯一性校验需覆盖已删除记录（数据库唯一键含全部记录，避免直接撞唯一键报500）
        if (StringUtils.isNotBlank(product.getProductCode()))
        {
            PdmProduct codeDup = productMapper.selectProductByCode(product.getProductCode());
            if (codeDup != null && (excludeProductId == null || !excludeProductId.equals(codeDup.getProductId())))
            {
                throw new ServiceException("产品编码【" + codeDup.getProductCode() + "】已被其他产品档案占用（含已删除记录），请更换物料");
            }
        }
    }
}

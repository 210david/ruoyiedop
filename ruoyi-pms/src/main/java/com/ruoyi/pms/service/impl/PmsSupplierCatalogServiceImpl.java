package com.ruoyi.pms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pms.domain.PmsSupplierCatalog;
import com.ruoyi.pms.mapper.PmsSupplierCatalogMapper;
import com.ruoyi.pms.service.IPmsSupplierCatalogService;
import com.ruoyi.wms.domain.WmsMaterial;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.mapper.WmsMaterialMapper;
import com.ruoyi.wms.mapper.WmsSupplierMapper;

/**
 * 供应商供货清单 Service实现
 *
 * @author ruoyi
 */
@Service
public class PmsSupplierCatalogServiceImpl implements IPmsSupplierCatalogService
{
    @Autowired
    private PmsSupplierCatalogMapper pmsSupplierCatalogMapper;

    @Autowired
    private WmsSupplierMapper wmsSupplierMapper;

    @Autowired
    private WmsMaterialMapper wmsMaterialMapper;

    @Override
    public List<PmsSupplierCatalog> selectCatalogList(PmsSupplierCatalog catalog)
    {
        return pmsSupplierCatalogMapper.selectCatalogList(catalog);
    }

    @Override
    public PmsSupplierCatalog selectCatalogById(Long catalogId)
    {
        return pmsSupplierCatalogMapper.selectCatalogById(catalogId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCatalog(PmsSupplierCatalog catalog)
    {
        catalog.setDelFlag("0");
        if (catalog.getStatus() == null)
        {
            catalog.setStatus("1");
        }
        return pmsSupplierCatalogMapper.insertCatalog(catalog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCatalog(PmsSupplierCatalog catalog)
    {
        return pmsSupplierCatalogMapper.updateCatalog(catalog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCatalogByIds(Long[] catalogIds)
    {
        return pmsSupplierCatalogMapper.deleteCatalogByIds(catalogIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importCatalog(List<PmsSupplierCatalog> catalogList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(catalogList) || catalogList.size() == 0)
        {
            throw new RuntimeException("导入供货清单数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (PmsSupplierCatalog row : catalogList)
        {
            try
            {
                // 1. 验证供应商名称不为空
                if (StringUtils.isEmpty(row.getSupplierName()))
                {
                    throw new RuntimeException("供应商名称不能为空");
                }
                // 2. 验证物料编码不为空
                if (StringUtils.isEmpty(row.getMaterialCode()))
                {
                    throw new RuntimeException("物料编码不能为空");
                }
                // 3. 按供应商名称精确匹配（selectSupplierList 用 LIKE，这里筛选精确匹配）
                WmsSupplier supplierQuery = new WmsSupplier();
                supplierQuery.setSupplierName(row.getSupplierName());
                List<WmsSupplier> supplierCandidates = wmsSupplierMapper.selectSupplierList(supplierQuery);
                WmsSupplier matchedSupplier = null;
                for (WmsSupplier s : supplierCandidates)
                {
                    if (s.getSupplierName() != null && s.getSupplierName().equals(row.getSupplierName()))
                    {
                        matchedSupplier = s;
                        break;
                    }
                }
                if (matchedSupplier == null)
                {
                    throw new RuntimeException("未找到供应商：" + row.getSupplierName());
                }
                row.setSupplierId(matchedSupplier.getSupplierId());

                // 4. 按物料编码精确匹配
                WmsMaterial matchedMaterial = wmsMaterialMapper.selectMaterialByCode(row.getMaterialCode());
                if (matchedMaterial == null)
                {
                    throw new RuntimeException("未找到物料编码：" + row.getMaterialCode());
                }
                row.setMaterialId(matchedMaterial.getMaterialId());
                // 用物料主数据覆盖物料名称、规格、单位（保证数据一致）
                row.setMaterialName(matchedMaterial.getMaterialName());
                row.setSpecModel(matchedMaterial.getSpecModel());
                row.setUnit(matchedMaterial.getUnit());

                // 5. 检查是否已存在（按供应商+物料组合）
                PmsSupplierCatalog existing = pmsSupplierCatalogMapper.selectCatalogBySupplierAndMaterial(
                        row.getSupplierId(), row.getMaterialId());
                if (existing != null)
                {
                    if (isUpdateSupport)
                    {
                        row.setCatalogId(existing.getCatalogId());
                        row.setUpdateBy(operName);
                        pmsSupplierCatalogMapper.updateCatalog(row);
                        successNum++;
                        successMsg.append("<br/>" + successNum + "、供应商 " + row.getSupplierName()
                                + " - 物料 " + row.getMaterialCode() + " 更新成功");
                    }
                    else
                    {
                        failureNum++;
                        failureMsg.append("<br/>" + failureNum + "、供应商 " + row.getSupplierName()
                                + " - 物料 " + row.getMaterialCode() + " 已存在");
                    }
                }
                else
                {
                    row.setDelFlag("0");
                    if (row.getStatus() == null || row.getStatus().isEmpty())
                    {
                        row.setStatus("1");
                    }
                    if (row.getCurrency() == null || row.getCurrency().isEmpty())
                    {
                        row.setCurrency("CNY");
                    }
                    row.setCreateBy(operName);
                    pmsSupplierCatalogMapper.insertCatalog(row);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、供应商 " + row.getSupplierName()
                            + " - 物料 " + row.getMaterialCode() + " 导入成功");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String label = row.getSupplierName() + " - " + row.getMaterialCode();
                failureMsg.append("<br/>" + failureNum + "、供应商 " + label + " 导入失败：" + e.getMessage());
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new RuntimeException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }
}

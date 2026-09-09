package com.ruoyi.pms.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
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
    public AjaxResult importCatalog(List<PmsSupplierCatalog> catalogList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(catalogList) || catalogList.size() == 0)
        {
            return AjaxResult.error("导入供货清单数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();
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
                // 字典类字段和数字字段校验
                validateDictFields(row);
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
                        successList.add(buildSuccessItem(row, "更新成功"));
                    }
                    else
                    {
                        failureNum++;
                        failureList.add(buildFailureItem(row, "供货清单已存在，未勾选更新选项"));
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
                    successList.add(buildSuccessItem(row, "导入成功"));
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String errMsg = e.getMessage();
                if (errMsg != null && errMsg.length() > 200) { errMsg = errMsg.substring(0, 200) + "..."; }
                failureList.add(buildFailureItem(row, errMsg != null ? errMsg : "未知错误"));
            }
        }
        // 构建返回结果（结构化明细）
        AjaxResult ajax = AjaxResult.success();
        ajax.put("successNum", successNum);
        ajax.put("failureNum", failureNum);
        ajax.put("successList", successList);
        ajax.put("failureList", failureList);
        if (failureNum > 0 && successNum == 0)
        {
            ajax.put("code", 500);
            ajax.put("msg", "导入失败，共 " + failureNum + " 条数据异常");
        }
        else if (failureNum > 0)
        {
            ajax.put("msg", "部分导入成功：成功 " + successNum + " 条，失败 " + failureNum + " 条");
        }
        else
        {
            ajax.put("msg", "导入成功，共 " + successNum + " 条");
        }
        return ajax;
    }

    /**
     * 校验字典类字段和数字字段
     */
    private void validateDictFields(PmsSupplierCatalog row)
    {
        // 状态（readConverterExp: 1=正常供货,2=暂停供货,3=停止供货）
        validateConverterField("状态", row.getStatus(), "1=正常供货,2=暂停供货,3=停止供货");
        // 价格校验
        if (row.getUnitPrice() != null && row.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) < 0)
        {
            throw new RuntimeException("单价不能为负数");
        }
        // 交货周期校验
        if (row.getDeliveryCycle() != null && row.getDeliveryCycle() < 0)
        {
            throw new RuntimeException("交货周期不能为负数");
        }
    }

    private void validateConverterField(String fieldLabel, String value, String converterExp)
    {
        if (StringUtils.isNotEmpty(value))
        {
            String label = ExcelUtil.convertByExp(value, converterExp, ",");
            if (StringUtils.isEmpty(label))
            {
                throw new RuntimeException(fieldLabel + "值不合法，请填写文字描述");
            }
        }
    }

    /**
     * 构建成功明细项
     */
    private Map<String, Object> buildSuccessItem(PmsSupplierCatalog row, String result)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("supplierName", row.getSupplierName() != null ? row.getSupplierName() : "");
        item.put("materialCode", row.getMaterialCode() != null ? row.getMaterialCode() : "");
        item.put("materialName", row.getMaterialName() != null ? row.getMaterialName() : "");
        item.put("specModel", row.getSpecModel() != null ? row.getSpecModel() : "");
        item.put("unit", row.getUnit() != null ? row.getUnit() : "");
        item.put("unitPrice", row.getUnitPrice() != null ? row.getUnitPrice().toString() : "");
        item.put("taxRate", row.getTaxRate() != null ? row.getTaxRate().toString() : "");
        item.put("minOrderQty", row.getMinOrderQty() != null ? row.getMinOrderQty().toString() : "");
        item.put("deliveryCycle", row.getDeliveryCycle() != null ? row.getDeliveryCycle().toString() : "");
        item.put("currency", row.getCurrency() != null ? row.getCurrency() : "");
        item.put("priceEffectiveDate", row.getPriceEffectiveDate() != null ? row.getPriceEffectiveDate().toString() : "");
        item.put("priceExpireDate", row.getPriceExpireDate() != null ? row.getPriceExpireDate().toString() : "");
        item.put("status", dictText(row.getStatus(), "1=正常供货,2=暂停供货,3=停止供货"));
        item.put("result", result);
        return item;
    }

    /**
     * 构建失败明细项（列与导入模板对齐）
     */
    private Map<String, Object> buildFailureItem(PmsSupplierCatalog row, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("supplierName", row.getSupplierName() != null ? row.getSupplierName() : "");
        item.put("materialCode", row.getMaterialCode() != null ? row.getMaterialCode() : "");
        item.put("unitPrice", row.getUnitPrice() != null ? row.getUnitPrice().toString() : "");
        item.put("taxRate", row.getTaxRate() != null ? row.getTaxRate().toString() : "");
        item.put("minOrderQty", row.getMinOrderQty() != null ? row.getMinOrderQty().toString() : "");
        item.put("deliveryCycle", row.getDeliveryCycle() != null ? row.getDeliveryCycle().toString() : "");
        item.put("priceEffectiveDate", row.getPriceEffectiveDate() != null ? row.getPriceEffectiveDate().toString() : "");
        item.put("priceExpireDate", row.getPriceExpireDate() != null ? row.getPriceExpireDate().toString() : "");
        item.put("status", dictText(row.getStatus(), "1=正常供货,2=暂停供货,3=停止供货"));
        item.put("errorReason", errorReason);
        return item;
    }

    /**
     * 将编码转换为文字标签（用于明细展示）
     */
    private String dictText(String code, String converterExp)
    {
        if (StringUtils.isEmpty(code)) { return ""; }
        String label = ExcelUtil.convertByExp(code, converterExp, ",");
        return StringUtils.isNotEmpty(label) ? label : code;
    }
}

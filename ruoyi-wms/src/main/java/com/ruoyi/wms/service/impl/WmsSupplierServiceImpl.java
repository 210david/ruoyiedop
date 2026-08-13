package com.ruoyi.wms.service.impl;
import java.util.HashMap;
import java.util.Map;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.mapper.WmsSupplierMapper;
import com.ruoyi.wms.service.IWmsSupplierService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class WmsSupplierServiceImpl implements IWmsSupplierService
{
    @Autowired
    private WmsSupplierMapper wmsSupplierMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<WmsSupplier> selectSupplierList(WmsSupplier supplier)
    {
        return wmsSupplierMapper.selectSupplierList(supplier);
    }

    @Override
    public WmsSupplier selectSupplierById(Long supplierId)
    {
        return wmsSupplierMapper.selectSupplierById(supplierId);
    }

    @Override
    public int insertSupplier(WmsSupplier supplier)
    {
        supplier.setDelFlag("0");
        if (StringUtils.isEmpty(supplier.getSupplierCode()))
        {
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(supplier.getSupplierType())) { params.put("supplierType", supplier.getSupplierType()); }
        supplier.setSupplierCode(mkNumberRuleService.generateNumber("wms_supplier", params));
        }
        return wmsSupplierMapper.insertSupplier(supplier);
    }

    @Override
    public int updateSupplier(WmsSupplier supplier)
    {
        return wmsSupplierMapper.updateSupplier(supplier);
    }

    @Override
    public int deleteSupplierByIds(Long[] supplierIds)
    {
        return wmsSupplierMapper.deleteSupplierByIds(supplierIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult importSupplier(List<WmsSupplier> supplierList, Boolean isUpdateSupport, String updateKey, String operName)
    {
        if (StringUtils.isNull(supplierList) || supplierList.size() == 0)
        {
            return AjaxResult.error("导入供应商数据不能为空！");
        }
        // 默认按供应商名称匹配
        if (StringUtils.isEmpty(updateKey))
        {
            updateKey = "supplierName";
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (WmsSupplier row : supplierList)
        {
            try
            {
                // 1. 基础校验：供应商名称不能为空
                if (StringUtils.isEmpty(row.getSupplierName()))
                {
                    throw new RuntimeException("供应商名称不能为空");
                }
                // 2. 根据匹配字段校验并查找已存在的供应商
                String matchValue = getMatchValue(row, updateKey);
                if (StringUtils.isEmpty(matchValue))
                {
                    String fieldLabel = getMatchFieldLabel(updateKey);
                    throw new RuntimeException("匹配字段「" + fieldLabel + "」不能为空");
                }
                WmsSupplier matchedSupplier = findSupplierByMatchKey(updateKey, matchValue);

                if (matchedSupplier != null)
                {
                    if (isUpdateSupport)
                    {
                        row.setSupplierId(matchedSupplier.getSupplierId());
                        // 保留原编码不被覆盖
                        if (StringUtils.isEmpty(row.getSupplierCode()))
                        {
                            row.setSupplierCode(matchedSupplier.getSupplierCode());
                        }
                        row.setUpdateBy(operName);
                        wmsSupplierMapper.updateSupplier(row);
                        successNum++;
                        successMsg.append("<br/>" + successNum + "、供应商 " + row.getSupplierName() + "（" + getMatchFieldLabel(updateKey) + ": " + matchValue + "）更新成功");
                    }
                    else
                    {
                        failureNum++;
                        failureMsg.append("<br/>" + failureNum + "、供应商 " + row.getSupplierName() + " 已存在（" + getMatchFieldLabel(updateKey) + ": " + matchValue + "）");
                    }
                }
                else
                {
                    // 新增：自动生成编码，设置默认值
                    Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(row.getSupplierType())) { params.put("supplierType", row.getSupplierType()); }
        row.setSupplierCode(mkNumberRuleService.generateNumber("wms_supplier", params));
                    row.setDelFlag("0");
                    if (StringUtils.isEmpty(row.getStatus()))
                    {
                        row.setStatus("0");
                    }
                    if (StringUtils.isEmpty(row.getQualificationStatus()))
                    {
                        row.setQualificationStatus("0");
                    }
                    row.setCreateBy(operName);
                    wmsSupplierMapper.insertSupplier(row);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、供应商 " + row.getSupplierName() + " 导入成功");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                failureMsg.append("<br/>" + failureNum + "、供应商 " + (StringUtils.isNotEmpty(row.getSupplierName()) ? row.getSupplierName() : "(名称为空)") + " 导入失败：" + e.getMessage());
            }
        }

        // 构建返回结果（不抛异常，返回成功和失败的明细）
        StringBuilder resultMsg = new StringBuilder();
        if (successNum > 0)
        {
            resultMsg.append(successMsg.toString());
        }
        if (failureNum > 0)
        {
            resultMsg.append(failureMsg.toString());
        }

        AjaxResult ajax = AjaxResult.success(resultMsg.toString());
        ajax.put("successNum", successNum);
        ajax.put("failureNum", failureNum);
        if (failureNum > 0 && successNum == 0)
        {
            // 全部失败时标记 code
            ajax.put("code", 500);
        }
        return ajax;
    }

    /**
     * 获取匹配字段的值
     */
    private String getMatchValue(WmsSupplier row, String updateKey)
    {
        switch (updateKey)
        {
            case "unifiedCreditCode":
                return row.getUnifiedCreditCode();
            case "supplierCode":
                return row.getSupplierCode();
            case "supplierName":
            default:
                return row.getSupplierName();
        }
    }

    /**
     * 获取匹配字段的中文标签
     */
    private String getMatchFieldLabel(String updateKey)
    {
        switch (updateKey)
        {
            case "unifiedCreditCode":
                return "统一社会信用代码";
            case "supplierCode":
                return "供应商编码";
            case "supplierName":
            default:
                return "供应商名称";
        }
    }

    /**
     * 根据匹配字段查找已存在的供应商
     */
    private WmsSupplier findSupplierByMatchKey(String updateKey, String matchValue)
    {
        WmsSupplier query = new WmsSupplier();
        switch (updateKey)
        {
            case "unifiedCreditCode":
                query.setUnifiedCreditCode(matchValue);
                break;
            case "supplierCode":
                query.setSupplierCode(matchValue);
                break;
            case "supplierName":
            default:
                query.setSupplierName(matchValue);
                break;
        }
        List<WmsSupplier> candidates = wmsSupplierMapper.selectSupplierList(query);
        // 精确匹配（selectSupplierList 用 LIKE，这里筛选精确匹配）
        for (WmsSupplier s : candidates)
        {
            String val = getMatchValue(s, updateKey);
            if (val != null && val.equals(matchValue))
            {
                return s;
            }
        }
        return null;
    }
}

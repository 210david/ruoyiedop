package com.ruoyi.wms.service.impl;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
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
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();
        for (WmsSupplier row : supplierList)
        {
            try
            {
                // 1. 基础校验：供应商名称不能为空
                if (StringUtils.isEmpty(row.getSupplierName()))
                {
                    throw new RuntimeException("供应商名称不能为空");
                }
                // 2. 字典类字段合法性校验
                validateDictFields(row);
                // 3. 根据匹配字段校验并查找已存在的供应商
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
                        successList.add(buildSuccessItem(row, "更新成功"));
                    }
                    else
                    {
                        failureNum++;
                        failureList.add(buildFailureItem(row, "供应商已存在（" + getMatchFieldLabel(updateKey) + ": " + matchValue + "），未勾选更新选项"));
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
     * 构建成功明细项（列与导入模板对齐，成功时已生成编码）
     */
    private Map<String, Object> buildSuccessItem(WmsSupplier row, String result)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("supplierCode", row.getSupplierCode() != null ? row.getSupplierCode() : "");
        item.put("supplierName", row.getSupplierName() != null ? row.getSupplierName() : "");
        item.put("supplierShortName", row.getSupplierShortName() != null ? row.getSupplierShortName() : "");
        item.put("supplierType", dictText(row.getSupplierType(), "0=厂商,1=经销商,2=代理商,3=服务商"));
        item.put("supplierLevel", row.getSupplierLevel() != null ? row.getSupplierLevel() : "");
        item.put("unifiedCreditCode", row.getUnifiedCreditCode() != null ? row.getUnifiedCreditCode() : "");
        item.put("contactPerson", row.getContactPerson() != null ? row.getContactPerson() : "");
        item.put("contactPhone", row.getContactPhone() != null ? row.getContactPhone() : "");
        item.put("email", row.getEmail() != null ? row.getEmail() : "");
        item.put("address", row.getAddress() != null ? row.getAddress() : "");
        item.put("legalPerson", row.getLegalPerson() != null ? row.getLegalPerson() : "");
        item.put("registeredCapital", row.getRegisteredCapital() != null ? row.getRegisteredCapital().toString() : "");
        item.put("enterpriseNature", dictText(row.getEnterpriseNature(), "0=国有企业,1=民营企业,2=外资企业,3=合资企业,4=个体工商户"));
        item.put("establishedDate", row.getEstablishedDate() != null ? row.getEstablishedDate().toString() : "");
        item.put("bankName", row.getBankName() != null ? row.getBankName() : "");
        item.put("bankAccount", row.getBankAccount() != null ? row.getBankAccount() : "");
        item.put("taxNumber", row.getTaxNumber() != null ? row.getTaxNumber() : "");
        item.put("invoiceTitle", row.getInvoiceTitle() != null ? row.getInvoiceTitle() : "");
        item.put("paymentMethod", dictText(row.getPaymentMethod(), "0=现结,1=月结,2=预付"));
        item.put("paymentDays", row.getPaymentDays() != null ? row.getPaymentDays().toString() : "");
        item.put("settlementType", dictText(row.getSettlementType(), "0=银行转账,1=承兑汇票,2=现金"));
        item.put("currency", row.getCurrency() != null ? row.getCurrency() : "");
        item.put("deliveryCycle", row.getDeliveryCycle() != null ? row.getDeliveryCycle().toString() : "");
        item.put("minOrderAmount", row.getMinOrderAmount() != null ? row.getMinOrderAmount().toString() : "");
        item.put("cooperationDate", row.getCooperationDate() != null ? row.getCooperationDate().toString() : "");
        item.put("qualificationStatus", dictText(row.getQualificationStatus(), "0=未审核,1=已审核,2=审核不通过"));
        item.put("status", dictText(row.getStatus(), "0=正常,1=停用"));
        item.put("result", result);
        return item;
    }

    /**
     * 构建失败明细项（列与导入模板对齐，去掉自动生成的供应商编码）
     */
    private Map<String, Object> buildFailureItem(WmsSupplier row, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("supplierName", row.getSupplierName() != null ? row.getSupplierName() : "");
        item.put("supplierShortName", row.getSupplierShortName() != null ? row.getSupplierShortName() : "");
        item.put("supplierType", dictText(row.getSupplierType(), "0=厂商,1=经销商,2=代理商,3=服务商"));
        item.put("supplierLevel", row.getSupplierLevel() != null ? row.getSupplierLevel() : "");
        item.put("unifiedCreditCode", row.getUnifiedCreditCode() != null ? row.getUnifiedCreditCode() : "");
        item.put("contactPerson", row.getContactPerson() != null ? row.getContactPerson() : "");
        item.put("contactPhone", row.getContactPhone() != null ? row.getContactPhone() : "");
        item.put("email", row.getEmail() != null ? row.getEmail() : "");
        item.put("address", row.getAddress() != null ? row.getAddress() : "");
        item.put("legalPerson", row.getLegalPerson() != null ? row.getLegalPerson() : "");
        item.put("registeredCapital", row.getRegisteredCapital() != null ? row.getRegisteredCapital().toString() : "");
        item.put("enterpriseNature", dictText(row.getEnterpriseNature(), "0=国有企业,1=民营企业,2=外资企业,3=合资企业,4=个体工商户"));
        item.put("establishedDate", row.getEstablishedDate() != null ? row.getEstablishedDate().toString() : "");
        item.put("bankName", row.getBankName() != null ? row.getBankName() : "");
        item.put("bankAccount", row.getBankAccount() != null ? row.getBankAccount() : "");
        item.put("taxNumber", row.getTaxNumber() != null ? row.getTaxNumber() : "");
        item.put("invoiceTitle", row.getInvoiceTitle() != null ? row.getInvoiceTitle() : "");
        item.put("paymentMethod", dictText(row.getPaymentMethod(), "0=现结,1=月结,2=预付"));
        item.put("paymentDays", row.getPaymentDays() != null ? row.getPaymentDays().toString() : "");
        item.put("settlementType", dictText(row.getSettlementType(), "0=银行转账,1=承兑汇票,2=现金"));
        item.put("currency", row.getCurrency() != null ? row.getCurrency() : "");
        item.put("deliveryCycle", row.getDeliveryCycle() != null ? row.getDeliveryCycle().toString() : "");
        item.put("minOrderAmount", row.getMinOrderAmount() != null ? row.getMinOrderAmount().toString() : "");
        item.put("cooperationDate", row.getCooperationDate() != null ? row.getCooperationDate().toString() : "");
        item.put("qualificationStatus", dictText(row.getQualificationStatus(), "0=未审核,1=已审核,2=审核不通过"));
        item.put("status", dictText(row.getStatus(), "0=正常,1=停用"));
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

    /**
     * 校验字典类字段：供应商类型、企业性质、付款方式、结算方式、资质状态、状态
     * 用户在Excel中填写的是文字描述，ExcelUtil已将合法文字转为编码。
     * 如果用户输入了不合法的文字或数字，ExcelUtil会保留原始值，此处检测并报错。
     */
    private void validateDictFields(WmsSupplier row)
    {
        // 供应商类型（readConverterExp: 0=厂商,1=经销商,2=代理商,3=服务商）
        validateConverterField("供应商类型", row.getSupplierType(), "0=厂商,1=经销商,2=代理商,3=服务商");
        // 企业性质（readConverterExp: 0=国有企业,1=民营企业,2=外资企业,3=合资企业,4=个体工商户）
        validateConverterField("企业性质", row.getEnterpriseNature(), "0=国有企业,1=民营企业,2=外资企业,3=合资企业,4=个体工商户");
        // 付款方式（readConverterExp: 0=现结,1=月结,2=预付）
        validateConverterField("付款方式", row.getPaymentMethod(), "0=现结,1=月结,2=预付");
        // 结算方式（readConverterExp: 0=银行转账,1=承兑汇票,2=现金）
        validateConverterField("结算方式", row.getSettlementType(), "0=银行转账,1=承兑汇票,2=现金");
        // 资质状态（readConverterExp: 0=未审核,1=已审核,2=审核不通过）
        validateConverterField("资质状态", row.getQualificationStatus(), "0=未审核,1=已审核,2=审核不通过");
        // 状态（readConverterExp: 0=正常,1=停用）
        validateConverterField("状态", row.getStatus(), "0=正常,1=停用");
        // 账期天数校验
        if (row.getPaymentDays() != null && row.getPaymentDays() < 0)
        {
            throw new RuntimeException("账期天数必须为非负整数");
        }
        // 交货周期校验
        if (row.getDeliveryCycle() != null && row.getDeliveryCycle() < 0)
        {
            throw new RuntimeException("交货周期必须为非负整数");
        }
        // 注册资本校验
        if (row.getRegisteredCapital() != null && row.getRegisteredCapital().compareTo(java.math.BigDecimal.ZERO) < 0)
        {
            throw new RuntimeException("注册资本不能为负数");
        }
        // 最小订单金额校验
        if (row.getMinOrderAmount() != null && row.getMinOrderAmount().compareTo(java.math.BigDecimal.ZERO) < 0)
        {
            throw new RuntimeException("最小订单金额不能为负数");
        }
    }

    /**
     * 校验readConverterExp字段：如果值不为空但不是合法编码，说明用户输入了不合法的文字或数字
     */
    private void validateConverterField(String fieldLabel, String value, String converterExp)
    {
        if (StringUtils.isNotEmpty(value))
        {
            // 尝试将编码转为文字，如果成功说明是合法编码
            String label = ExcelUtil.convertByExp(value, converterExp, ",");
            if (StringUtils.isEmpty(label))
            {
                // 不是合法编码，说明用户输入了不合法的文字或数字
                throw new RuntimeException(fieldLabel + "值不合法，请填写文字描述（参考导入说明中的合法值）");
            }
        }
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

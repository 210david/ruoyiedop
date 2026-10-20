package com.ruoyi.dms.service.impl;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.dms.domain.DmsEquipment;
import com.ruoyi.dms.mapper.DmsEquipmentMapper;
import com.ruoyi.dms.service.IDmsEquipmentService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class DmsEquipmentServiceImpl implements IDmsEquipmentService
{
    @Autowired
    private DmsEquipmentMapper dmsEquipmentMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<DmsEquipment> selectEquipmentList(DmsEquipment equipment)
    {
        return dmsEquipmentMapper.selectEquipmentList(equipment);
    }

    @Override
    public DmsEquipment selectEquipmentById(Long equipmentId)
    {
        return dmsEquipmentMapper.selectEquipmentById(equipmentId);
    }

    @Override
    public int insertEquipment(DmsEquipment equipment)
    {
        equipment.setDelFlag("0");
        if (StringUtils.isEmpty(equipment.getEquipmentCode()))
        {
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(equipment.getEquipmentStatus())) { params.put("equipmentStatus", equipment.getEquipmentStatus()); }
        equipment.setEquipmentCode(mkNumberRuleService.generateNumber("dms_equipment", params));
        }
        // 记录创建人与创建时间
        equipment.setCreateBy(SecurityUtils.getUsername());
        equipment.setCreateTime(DateUtils.getNowDate());
        return dmsEquipmentMapper.insertEquipment(equipment);
    }

    @Override
    public int updateEquipment(DmsEquipment equipment)
    {
        return dmsEquipmentMapper.updateEquipment(equipment);
    }

    @Override
    public int deleteEquipmentByIds(Long[] equipmentIds)
    {
        return dmsEquipmentMapper.deleteEquipmentByIds(equipmentIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult importEquipment(List<DmsEquipment> equipmentList, Boolean isUpdateSupport, String updateKey, String operName)
    {
        if (StringUtils.isNull(equipmentList) || equipmentList.size() == 0)
        {
            return AjaxResult.error("导入设备数据不能为空！");
        }
        // 默认按设备名称匹配
        if (StringUtils.isEmpty(updateKey))
        {
            updateKey = "equipmentName";
        }
        int successNum = 0;
        int failureNum = 0;
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();
        for (DmsEquipment row : equipmentList)
        {
            try
            {
                // 1. 基础校验：设备名称不能为空
                if (StringUtils.isEmpty(row.getEquipmentName()))
                {
                    throw new RuntimeException("设备名称不能为空");
                }
                // 2. 字典类字段合法性校验
                validateDictFields(row);
                // 3. 根据匹配字段校验并查找已存在的设备
                String matchValue = getMatchValue(row, updateKey);
                if (StringUtils.isEmpty(matchValue))
                {
                    String fieldLabel = getMatchFieldLabel(updateKey);
                    throw new RuntimeException("匹配字段「" + fieldLabel + "」不能为空");
                }
                DmsEquipment matchedEquipment = findEquipmentByMatchKey(updateKey, matchValue);

                if (matchedEquipment != null)
                {
                    if (isUpdateSupport)
                    {
                        row.setEquipmentId(matchedEquipment.getEquipmentId());
                        // 保留原编码不被覆盖
                        if (StringUtils.isEmpty(row.getEquipmentCode()))
                        {
                            row.setEquipmentCode(matchedEquipment.getEquipmentCode());
                        }
                        row.setUpdateBy(operName);
                        dmsEquipmentMapper.updateEquipment(row);
                        successNum++;
                        Map<String, Object> item = buildSuccessItem(row, "更新成功", updateKey, matchValue);
                        successList.add(item);
                    }
                    else
                    {
                        failureNum++;
                        Map<String, Object> item = buildFailureItem(row, "设备已存在（" + getMatchFieldLabel(updateKey) + ": " + matchValue + "），未勾选更新选项", updateKey, matchValue);
                        failureList.add(item);
                    }
                }
                else
                {
                    // 新增：自动生成编码，设置默认值
                    Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(row.getEquipmentStatus())) { params.put("equipmentStatus", row.getEquipmentStatus()); }
        row.setEquipmentCode(mkNumberRuleService.generateNumber("dms_equipment", params));
                    row.setDelFlag("0");
                    if (StringUtils.isEmpty(row.getStatus()))
                    {
                        row.setStatus("0");
                    }
                    if (StringUtils.isEmpty(row.getEquipmentStatus()))
                    {
                        row.setEquipmentStatus("0");
                    }
                    row.setCreateBy(operName);
                    dmsEquipmentMapper.insertEquipment(row);
                    successNum++;
                    Map<String, Object> item = buildSuccessItem(row, "导入成功", updateKey, matchValue);
                    successList.add(item);
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String errMsg = e.getMessage();
                if (errMsg != null && errMsg.length() > 200)
                {
                    errMsg = errMsg.substring(0, 200) + "...";
                }
                Map<String, Object> item = buildFailureItem(row, errMsg != null ? errMsg : "未知错误", updateKey, null);
                failureList.add(item);
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
     * 校验字典类字段：设备状态、设备等级、状态
     * 用户在Excel中填写的是文字描述，ExcelUtil已将合法文字转为编码。
     * 如果用户输入了不合法的文字或数字，ExcelUtil会保留原始值，此处检测并报错。
     */
    private void validateDictFields(DmsEquipment row)
    {
        // 设备状态（dictType: dms_equipment_status）
        validateDictTypeField("设备状态", row.getEquipmentStatus(), "dms_equipment_status");
        // 设备等级（dictType: dms_equipment_level）
        validateDictTypeField("设备等级", row.getEquipmentLevel(), "dms_equipment_level");
        // 状态（dictType: sys_normal_disable）
        validateDictTypeField("状态", row.getStatus(), "sys_normal_disable");
        // 原值校验
        if (row.getOriginalValue() != null && row.getOriginalValue().compareTo(java.math.BigDecimal.ZERO) < 0)
        {
            throw new RuntimeException("原值不能为负数");
        }
    }

    /**
     * 校验dictType字段：如果值不为空但不是合法编码，说明用户输入了不合法的文字或数字
     */
    private void validateDictTypeField(String fieldLabel, String value, String dictType)
    {
        if (StringUtils.isNotEmpty(value))
        {
            // 尝试将编码转为文字，如果成功说明是合法编码
            String label = DictUtils.getDictLabel(dictType, value);
            if (StringUtils.isEmpty(label))
            {
                // 不是合法编码，说明用户输入了不合法的文字或数字
                throw new RuntimeException(fieldLabel + "值不合法，请填写文字描述（参考导入说明中的合法值）");
            }
        }
    }

    /**
     * 构建成功明细项
     */
    private Map<String, Object> buildSuccessItem(DmsEquipment row, String result, String updateKey, String matchValue)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("equipmentCode", row.getEquipmentCode() != null ? row.getEquipmentCode() : "");
        item.put("assetCode", row.getAssetCode() != null ? row.getAssetCode() : "");
        item.put("equipmentName", row.getEquipmentName() != null ? row.getEquipmentName() : "");
        item.put("categoryName", row.getCategoryName() != null ? row.getCategoryName() : "");
        item.put("model", row.getModel() != null ? row.getModel() : "");
        item.put("serialNumber", row.getSerialNumber() != null ? row.getSerialNumber() : "");
        item.put("manufacturer", row.getManufacturer() != null ? row.getManufacturer() : "");
        item.put("supplier", row.getSupplier() != null ? row.getSupplier() : "");
        item.put("purchaseDate", row.getPurchaseDate() != null ? row.getPurchaseDate().toString() : "");
        item.put("installDate", row.getInstallDate() != null ? row.getInstallDate().toString() : "");
        item.put("retireDate", row.getRetireDate() != null ? row.getRetireDate().toString() : "");
        item.put("originalValue", row.getOriginalValue() != null ? row.getOriginalValue().toString() : "");
        item.put("deptName", row.getDeptName() != null ? row.getDeptName() : "");
        item.put("installLocation", row.getInstallLocation() != null ? row.getInstallLocation() : "");
        item.put("equipmentStatus", dictText(row.getEquipmentStatus(), "dms_equipment_status"));
        item.put("equipmentLevel", dictText(row.getEquipmentLevel(), "dms_equipment_level"));
        item.put("warrantyDate", row.getWarrantyDate() != null ? row.getWarrantyDate().toString() : "");
        item.put("responsibleName", row.getResponsibleName() != null ? row.getResponsibleName() : "");
        item.put("status", dictText(row.getStatus(), "sys_normal_disable"));
        item.put("result", result);
        return item;
    }

    /**
     * 构建失败明细项（去掉自动生成的设备编号，列与导入模板对齐）
     */
    private Map<String, Object> buildFailureItem(DmsEquipment row, String errorReason, String updateKey, String matchValue)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("assetCode", row.getAssetCode() != null ? row.getAssetCode() : "");
        item.put("equipmentName", row.getEquipmentName() != null ? row.getEquipmentName() : "");
        item.put("categoryName", row.getCategoryName() != null ? row.getCategoryName() : "");
        item.put("model", row.getModel() != null ? row.getModel() : "");
        item.put("serialNumber", row.getSerialNumber() != null ? row.getSerialNumber() : "");
        item.put("manufacturer", row.getManufacturer() != null ? row.getManufacturer() : "");
        item.put("supplier", row.getSupplier() != null ? row.getSupplier() : "");
        item.put("purchaseDate", row.getPurchaseDate() != null ? row.getPurchaseDate().toString() : "");
        item.put("installDate", row.getInstallDate() != null ? row.getInstallDate().toString() : "");
        item.put("retireDate", row.getRetireDate() != null ? row.getRetireDate().toString() : "");
        item.put("originalValue", row.getOriginalValue() != null ? row.getOriginalValue().toString() : "");
        item.put("deptName", row.getDeptName() != null ? row.getDeptName() : "");
        item.put("installLocation", row.getInstallLocation() != null ? row.getInstallLocation() : "");
        item.put("equipmentStatus", dictText(row.getEquipmentStatus(), "dms_equipment_status"));
        item.put("equipmentLevel", dictText(row.getEquipmentLevel(), "dms_equipment_level"));
        item.put("warrantyDate", row.getWarrantyDate() != null ? row.getWarrantyDate().toString() : "");
        item.put("responsibleName", row.getResponsibleName() != null ? row.getResponsibleName() : "");
        item.put("status", dictText(row.getStatus(), "sys_normal_disable"));
        item.put("errorReason", errorReason);
        return item;
    }

    /**
     * 将编码转换为文字标签（用于明细展示）
     */
    private String dictText(String code, String dictType)
    {
        if (StringUtils.isEmpty(code)) { return ""; }
        String label = DictUtils.getDictLabel(dictType, code, "");
        return StringUtils.isNotEmpty(label) ? label : code;
    }

    /**
     * 获取匹配字段的值
     */
    private String getMatchValue(DmsEquipment row, String updateKey)
    {
        switch (updateKey)
        {
            case "equipmentCode":
                return row.getEquipmentCode();
            case "assetCode":
                return row.getAssetCode();
            case "serialNumber":
                return row.getSerialNumber();
            case "equipmentName":
            default:
                return row.getEquipmentName();
        }
    }

    /**
     * 获取匹配字段的中文标签
     */
    private String getMatchFieldLabel(String updateKey)
    {
        switch (updateKey)
        {
            case "equipmentCode":
                return "设备编号";
            case "assetCode":
                return "资产编号";
            case "serialNumber":
                return "序列号";
            case "equipmentName":
            default:
                return "设备名称";
        }
    }

    /**
     * 根据匹配字段查找已存在的设备
     */
    private DmsEquipment findEquipmentByMatchKey(String updateKey, String matchValue)
    {
        DmsEquipment query = new DmsEquipment();
        switch (updateKey)
        {
            case "equipmentCode":
                query.setEquipmentCode(matchValue);
                break;
            case "assetCode":
                query.setAssetCode(matchValue);
                break;
            case "serialNumber":
                query.setSerialNumber(matchValue);
                break;
            case "equipmentName":
            default:
                query.setEquipmentName(matchValue);
                break;
        }
        List<DmsEquipment> candidates = dmsEquipmentMapper.selectEquipmentList(query);
        // 精确匹配（selectEquipmentList 用 LIKE，这里筛选精确匹配）
        for (DmsEquipment e : candidates)
        {
            String val = getMatchValue(e, updateKey);
            if (val != null && val.equals(matchValue))
            {
                return e;
            }
        }
        return null;
    }
}

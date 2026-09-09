package com.ruoyi.wms.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsMaterial;
import com.ruoyi.wms.mapper.WmsMaterialMapper;
import com.ruoyi.wms.service.IWmsMaterialService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class WmsMaterialServiceImpl implements IWmsMaterialService
{
    @Autowired
    private WmsMaterialMapper wmsMaterialMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<WmsMaterial> selectMaterialList(WmsMaterial material)
    {
        return wmsMaterialMapper.selectMaterialList(material);
    }

    @Override
    public WmsMaterial selectMaterialById(Long materialId)
    {
        return wmsMaterialMapper.selectMaterialById(materialId);
    }

    @Override
    public WmsMaterial selectMaterialByCode(String materialCode)
    {
        return wmsMaterialMapper.selectMaterialByCode(materialCode);
    }

    @Override
    public int insertMaterial(WmsMaterial material)
    {
        // 校验安全库存上限不能小于下限
        validateSafetyStock(material);
        // 校验效期管理为"是"时保质期天数和临期预警必填
        validateExpiryManage(material);
        if (StringUtils.isEmpty(material.getMaterialCode()))
        {
            material.setMaterialCode(generateMaterialCode(material.getMaterialType()));
        }
        if (!checkMaterialCodeUnique(material))
        {
            throw new ServiceException("新增物料'" + material.getMaterialName() + "'失败，物料编码已存在");
        }
        material.setDelFlag("0");
        return wmsMaterialMapper.insertMaterial(material);
    }

    /**
     * 生成物料编码（支持动态前缀）
     * 通过传入 materialType 参数，由编号规则服务根据配置的动态前缀映射决定前缀
     */
    private String generateMaterialCode(String materialType)
    {
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(materialType))
        {
            params.put("materialType", materialType);
        }
        return mkNumberRuleService.generateNumber("wms_material", params);
    }

    @Override
    public int updateMaterial(WmsMaterial material)
    {
        // 校验安全库存上限不能小于下限
        validateSafetyStock(material);
        // 校验效期管理为"是"时保质期天数和临期预警必填
        validateExpiryManage(material);
        if (!checkMaterialCodeUnique(material))
        {
            throw new ServiceException("修改物料'" + material.getMaterialName() + "'失败，物料编码已存在");
        }
        return wmsMaterialMapper.updateMaterial(material);
    }

    @Override
    public int deleteMaterialByIds(Long[] materialIds)
    {
        return wmsMaterialMapper.deleteMaterialByIds(materialIds);
    }

    @Override
    public boolean checkMaterialCodeUnique(WmsMaterial material)
    {
        Long materialId = material.getMaterialId() == null ? -1L : material.getMaterialId();
        WmsMaterial info = wmsMaterialMapper.selectMaterialByCode(material.getMaterialCode());
        if (info != null && info.getMaterialId().longValue() != materialId.longValue())
        {
            return false;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult importMaterial(List<WmsMaterial> materialList, Boolean isUpdateSupport, String updateKey, String operName)
    {
        if (StringUtils.isNull(materialList) || materialList.size() == 0)
        {
            return AjaxResult.error("导入物料数据不能为空！");
        }
        // 默认按物料名称匹配
        if (StringUtils.isEmpty(updateKey))
        {
            updateKey = "materialName";
        }
        int successNum = 0;
        int failureNum = 0;
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();
        for (WmsMaterial row : materialList)
        {
            try
            {
                // 1. 基础校验：物料名称不能为空
                if (StringUtils.isEmpty(row.getMaterialName()))
                {
                    throw new RuntimeException("物料名称不能为空");
                }
                // 2. 字典类字段合法性校验
                validateDictFields(row);
                // 3. 效期管理校验
                validateExpiryManage(row);
                // 4. 安全库存校验
                validateSafetyStock(row);
                // 5. 根据匹配字段校验并查找已存在的物料
                String matchValue = getMatchValue(row, updateKey);
                if (StringUtils.isEmpty(matchValue))
                {
                    String fieldLabel = getMatchFieldLabel(updateKey);
                    throw new RuntimeException("匹配字段「" + fieldLabel + "」不能为空");
                }
                WmsMaterial matchedMaterial = findMaterialByMatchKey(updateKey, matchValue);

                if (matchedMaterial != null)
                {
                    if (isUpdateSupport)
                    {
                        row.setMaterialId(matchedMaterial.getMaterialId());
                        // 保留原编码不被覆盖
                        if (StringUtils.isEmpty(row.getMaterialCode()))
                        {
                            row.setMaterialCode(matchedMaterial.getMaterialCode());
                        }
                        row.setUpdateBy(operName);
                        wmsMaterialMapper.updateMaterial(row);
                        successNum++;
                        successList.add(buildSuccessItem(row, "更新成功"));
                    }
                    else
                    {
                        failureNum++;
                        failureList.add(buildFailureItem(row, "物料已存在（" + getMatchFieldLabel(updateKey) + ": " + matchValue + "），未勾选更新选项"));
                    }
                }
                else
                {
                    // 新增：自动生成编码，设置默认值
                    row.setMaterialCode(generateMaterialCode(row.getMaterialType()));
                    row.setDelFlag("0");
                    if (StringUtils.isEmpty(row.getStatus()))
                    {
                        row.setStatus("0");
                    }
                    if (StringUtils.isEmpty(row.getIsBatchManage()))
                    {
                        row.setIsBatchManage("0");
                    }
                    if (StringUtils.isEmpty(row.getIsExpiryManage()))
                    {
                        row.setIsExpiryManage("0");
                    }
                    row.setCreateBy(operName);
                    wmsMaterialMapper.insertMaterial(row);
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
     * 构建成功明细项
     */
    private Map<String, Object> buildSuccessItem(WmsMaterial row, String result)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("materialCode", row.getMaterialCode() != null ? row.getMaterialCode() : "");
        item.put("materialName", row.getMaterialName() != null ? row.getMaterialName() : "");
        item.put("specModel", row.getSpecModel() != null ? row.getSpecModel() : "");
        item.put("unit", dictText(row.getUnit(), UNIT_REVERSE));
        item.put("materialType", dictText(row.getMaterialType(), MATERIAL_TYPE_REVERSE));
        item.put("isBatchManage", dictText(row.getIsBatchManage(), YES_NO_REVERSE));
        item.put("isExpiryManage", dictText(row.getIsExpiryManage(), YES_NO_REVERSE));
        item.put("shelfLifeDays", row.getShelfLifeDays() != null ? row.getShelfLifeDays() : "");
        item.put("safetyStockMin", row.getSafetyStockMin() != null ? row.getSafetyStockMin() : "");
        item.put("safetyStockMax", row.getSafetyStockMax() != null ? row.getSafetyStockMax() : "");
        item.put("status", dictText(row.getStatus(), STATUS_REVERSE));
        item.put("result", result);
        return item;
    }

    /**
     * 构建失败明细项
     */
    private Map<String, Object> buildFailureItem(WmsMaterial row, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("materialName", row.getMaterialName() != null ? row.getMaterialName() : "");
        item.put("specModel", row.getSpecModel() != null ? row.getSpecModel() : "");
        item.put("unit", dictText(row.getUnit(), UNIT_REVERSE));
        item.put("materialType", dictText(row.getMaterialType(), MATERIAL_TYPE_REVERSE));
        item.put("isBatchManage", dictText(row.getIsBatchManage(), YES_NO_REVERSE));
        item.put("isExpiryManage", dictText(row.getIsExpiryManage(), YES_NO_REVERSE));
        item.put("shelfLifeDays", row.getShelfLifeDays() != null ? row.getShelfLifeDays() : "");
        item.put("safetyStockMin", row.getSafetyStockMin() != null ? row.getSafetyStockMin() : "");
        item.put("safetyStockMax", row.getSafetyStockMax() != null ? row.getSafetyStockMax() : "");
        item.put("status", dictText(row.getStatus(), STATUS_REVERSE));
        item.put("errorReason", errorReason);
        return item;
    }

    /**
     * 获取匹配字段的值
     */
    private String getMatchValue(WmsMaterial row, String updateKey)
    {
        switch (updateKey)
        {
            case "materialCode":
                return row.getMaterialCode();
            case "materialName":
            default:
                return row.getMaterialName();
        }
    }

    /**
     * 获取匹配字段的中文标签
     */
    private String getMatchFieldLabel(String updateKey)
    {
        switch (updateKey)
        {
            case "materialCode":
                return "物料编码";
            case "materialName":
            default:
                return "物料名称";
        }
    }

    /**
     * 根据匹配字段查找已存在的物料
     */
    private WmsMaterial findMaterialByMatchKey(String updateKey, String matchValue)
    {
        WmsMaterial query = new WmsMaterial();
        switch (updateKey)
        {
            case "materialCode":
                // 物料编码有唯一索引，直接精确查询
                return wmsMaterialMapper.selectMaterialByCode(matchValue);
            case "materialName":
            default:
                query.setMaterialName(matchValue);
                break;
        }
        List<WmsMaterial> candidates = wmsMaterialMapper.selectMaterialList(query);
        // 精确匹配（selectMaterialList 用 LIKE，这里筛选精确匹配）
        for (WmsMaterial m : candidates)
        {
            String val = getMatchValue(m, updateKey);
            if (val != null && val.equals(matchValue))
            {
                return m;
            }
        }
        return null;
    }

    /** 物料类型 文字→数字 映射表 */
    private static final Map<String, String> MATERIAL_TYPE_MAP = new HashMap<>();
    /** 物料类型 数字→文字 映射表（用于导入结果展示） */
    private static final Map<String, String> MATERIAL_TYPE_REVERSE = new HashMap<>();
    static {
        MATERIAL_TYPE_MAP.put("原材料", "0");
        MATERIAL_TYPE_MAP.put("半成品", "1");
        MATERIAL_TYPE_MAP.put("成品", "2");
        MATERIAL_TYPE_MAP.put("辅料", "3");
        MATERIAL_TYPE_MAP.put("服务", "4");
        MATERIAL_TYPE_MAP.put("工程", "5");
        MATERIAL_TYPE_REVERSE.put("0", "原材料");
        MATERIAL_TYPE_REVERSE.put("1", "半成品");
        MATERIAL_TYPE_REVERSE.put("2", "成品");
        MATERIAL_TYPE_REVERSE.put("3", "辅料");
        MATERIAL_TYPE_REVERSE.put("4", "服务");
        MATERIAL_TYPE_REVERSE.put("5", "工程");
    }
    /** 计量单位 文字→数字 映射表 */
    private static final Map<String, String> UNIT_MAP = new HashMap<>();
    /** 计量单位 数字→文字 映射表（用于导入结果展示） */
    private static final Map<String, String> UNIT_REVERSE = new HashMap<>();
    static {
        UNIT_MAP.put("个", "0");
        UNIT_MAP.put("件", "1");
        UNIT_MAP.put("箱", "2");
        UNIT_MAP.put("kg", "3");
        UNIT_MAP.put("公斤", "3");
        UNIT_MAP.put("吨", "4");
        UNIT_MAP.put("米", "5");
        UNIT_REVERSE.put("0", "个");
        UNIT_REVERSE.put("1", "件");
        UNIT_REVERSE.put("2", "箱");
        UNIT_REVERSE.put("3", "kg");
        UNIT_REVERSE.put("4", "吨");
        UNIT_REVERSE.put("5", "米");
    }
    /** 是/否 文字→数字 映射表 */
    private static final Map<String, String> YES_NO_MAP = new HashMap<>();
    /** 是/否 数字→文字 映射表（用于导入结果展示） */
    private static final Map<String, String> YES_NO_REVERSE = new HashMap<>();
    static {
        YES_NO_MAP.put("是", "1");
        YES_NO_MAP.put("否", "0");
        YES_NO_REVERSE.put("0", "否");
        YES_NO_REVERSE.put("1", "是");
    }
    /** 状态 文字→数字 映射表 */
    private static final Map<String, String> STATUS_MAP = new HashMap<>();
    /** 状态 数字→文字 映射表（用于导入结果展示） */
    private static final Map<String, String> STATUS_REVERSE = new HashMap<>();
    static {
        STATUS_MAP.put("正常", "0");
        STATUS_MAP.put("停用", "1");
        STATUS_REVERSE.put("0", "正常");
        STATUS_REVERSE.put("1", "停用");
    }

    /**
     * 校验并转换字典类字段（导入用）
     * 用户在Excel中填写的是文字描述（如“原材料”、“是”），
     * 这里将其转换为数据库存储的数字编码。
     * 如果填写了不合法的值（包括数字），则报错。
     * 注意：domain 字段已去掉 readConverterExp，ExcelUtil 不会自动转换，
     * 用户填写的原始值直接传入此方法。
     */
    private void validateDictFields(WmsMaterial row)
    {
        // 物料类型校验并转换
        String materialType = row.getMaterialType();
        if (StringUtils.isNotEmpty(materialType))
        {
            String code = MATERIAL_TYPE_MAP.get(materialType.trim());
            if (code == null)
            {
                throw new RuntimeException("物料类型值不合法，请填写文字：原材料、半成品、成品、辅料、服务、工程（或留空）");
            }
            row.setMaterialType(code);
        }
        // 计量单位校验并转换
        String unit = row.getUnit();
        if (StringUtils.isNotEmpty(unit))
        {
            String code = UNIT_MAP.get(unit.trim());
            if (code == null)
            {
                throw new RuntimeException("计量单位值不合法，请填写文字：个、件、箱、kg、吨、米（或留空）");
            }
            row.setUnit(code);
        }
        // 效期管理校验并转换
        String isExpiryManage = row.getIsExpiryManage();
        if (StringUtils.isNotEmpty(isExpiryManage))
        {
            String code = YES_NO_MAP.get(isExpiryManage.trim());
            if (code == null)
            {
                throw new RuntimeException("效期管理值不合法，请填写文字：是 或 否（或留空）");
            }
            row.setIsExpiryManage(code);
        }
        // 批次管理校验并转换
        String isBatchManage = row.getIsBatchManage();
        if (StringUtils.isNotEmpty(isBatchManage))
        {
            String code = YES_NO_MAP.get(isBatchManage.trim());
            if (code == null)
            {
                throw new RuntimeException("批次管理值不合法，请填写文字：是 或 否（或留空）");
            }
            row.setIsBatchManage(code);
        }
        // 状态校验并转换
        String status = row.getStatus();
        if (StringUtils.isNotEmpty(status))
        {
            String code = STATUS_MAP.get(status.trim());
            if (code == null)
            {
                throw new RuntimeException("状态值不合法，请填写文字：正常 或 停用（或留空）");
            }
            row.setStatus(code);
        }
        // 保质期天数校验
        Integer shelfLifeDays = row.getShelfLifeDays();
        if (shelfLifeDays != null && shelfLifeDays < 0)
        {
            throw new RuntimeException("保质期天数必须为非负整数");
        }
        // 临期预警天数校验
        Integer expiryAlertDays = row.getExpiryAlertDays();
        if (expiryAlertDays != null && expiryAlertDays < 0)
        {
            throw new RuntimeException("临期预警天数必须为非负整数");
        }
    }

    /**
     * 将数字编码转为文字描述（用于导入结果展示）
     * @param code 数字编码
     * @param codeToText 数字→文字映射表
     * @return 文字描述，如果为空或未找到则返回原始值
     */
    private String dictText(String code, Map<String, String> codeToText)
    {
        if (StringUtils.isEmpty(code))
        {
            return "";
        }
        String text = codeToText.get(code);
        return text != null ? text : code;
    }

    /**
     * 校验效期管理为"是"时保质期天数和临期预警必填，且临期预警不能大于保质期天数
     */
    private void validateExpiryManage(WmsMaterial material)
    {
        if ("1".equals(material.getIsExpiryManage()))
        {
            if (material.getShelfLifeDays() == null)
            {
                throw new ServiceException("效期管理为「是」时，保质期天数不能为空");
            }
            if (material.getExpiryAlertDays() == null)
            {
                throw new ServiceException("效期管理为「是」时，临期预警天数不能为空");
            }
            if (material.getExpiryAlertDays() > material.getShelfLifeDays())
            {
                throw new ServiceException("临期预警天数不能大于保质期天数");
            }
        }
    }

    /**
     * 校验安全库存上限不能小于安全库存下限
     */
    private void validateSafetyStock(WmsMaterial material)
    {
        BigDecimal min = material.getSafetyStockMin();
        BigDecimal max = material.getSafetyStockMax();
        if (min != null && max != null && max.compareTo(min) < 0)
        {
            throw new ServiceException("安全库存上限不能小于安全库存下限");
        }
    }
}

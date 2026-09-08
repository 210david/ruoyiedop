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
                // 2. 根据匹配字段校验并查找已存在的物料
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
        item.put("unit", row.getUnit() != null ? row.getUnit() : "");
        item.put("materialType", row.getMaterialType() != null ? row.getMaterialType() : "");
        item.put("result", result);
        return item;
    }

    /**
     * 构建失败明细项
     */
    private Map<String, Object> buildFailureItem(WmsMaterial row, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("materialCode", row.getMaterialCode() != null ? row.getMaterialCode() : "");
        item.put("materialName", row.getMaterialName() != null ? row.getMaterialName() : "");
        item.put("specModel", row.getSpecModel() != null ? row.getSpecModel() : "");
        item.put("unit", row.getUnit() != null ? row.getUnit() : "");
        item.put("materialType", row.getMaterialType() != null ? row.getMaterialType() : "");
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
}

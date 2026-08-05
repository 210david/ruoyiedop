package com.ruoyi.dms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
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
            equipment.setEquipmentCode(mkNumberRuleService.generateNumber("dms_equipment"));
        }
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
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (DmsEquipment row : equipmentList)
        {
            try
            {
                // 1. 基础校验：设备名称不能为空
                if (StringUtils.isEmpty(row.getEquipmentName()))
                {
                    throw new RuntimeException("设备名称不能为空");
                }
                // 2. 根据匹配字段校验并查找已存在的设备
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
                        successMsg.append("<br/>" + successNum + "、设备 " + row.getEquipmentName() + "（" + getMatchFieldLabel(updateKey) + ": " + matchValue + "）更新成功");
                    }
                    else
                    {
                        failureNum++;
                        failureMsg.append("<br/>" + failureNum + "、设备 " + row.getEquipmentName() + " 已存在（" + getMatchFieldLabel(updateKey) + ": " + matchValue + "）");
                    }
                }
                else
                {
                    // 新增：自动生成编码，设置默认值
                    row.setEquipmentCode(mkNumberRuleService.generateNumber("dms_equipment"));
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
                    successMsg.append("<br/>" + successNum + "、设备 " + row.getEquipmentName() + " 导入成功");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                failureMsg.append("<br/>" + failureNum + "、设备 " + (StringUtils.isNotEmpty(row.getEquipmentName()) ? row.getEquipmentName() : "(名称为空)") + " 导入失败：" + e.getMessage());
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

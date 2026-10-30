package com.ruoyi.wms.service.impl;

import java.util.HashMap;
import java.util.Map;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.mapper.WmsWarehouseMapper;
import com.ruoyi.wms.service.IWmsWarehouseService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class WmsWarehouseServiceImpl implements IWmsWarehouseService
{
    @Autowired
    private WmsWarehouseMapper wmsWarehouseMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<WmsWarehouse> selectWarehouseList(WmsWarehouse warehouse)
    {
        return wmsWarehouseMapper.selectWarehouseList(warehouse);
    }

    @Override
    public WmsWarehouse selectWarehouseById(Long warehouseId)
    {
        return wmsWarehouseMapper.selectWarehouseById(warehouseId);
    }

    @Override
    public int insertWarehouse(WmsWarehouse warehouse)
    {
        warehouse.setDelFlag("0");
        if ("1".equals(warehouse.getNodeType()))
        {
            // 仓库级别：根据编号规则「是否启用自动编码」开关决定生成方式
            if (isAutoCodeEnabled())
            {
                // 自动编码开启：按规则生成，忽略手动输入
                Map<String, String> params = new HashMap<>();
            if (StringUtils.isNotEmpty(warehouse.getAreaType())) { params.put("areaType", warehouse.getAreaType()); }
            if (StringUtils.isNotEmpty(warehouse.getLocationType())) { params.put("locationType", warehouse.getLocationType()); }
            warehouse.setWarehouseCode(mkNumberRuleService.generateNumber("wms_warehouse", params));
            }
            else
            {
                // 自动编码关闭：编码必填，手动输入，校验不能重复
                if (StringUtils.isEmpty(warehouse.getWarehouseCode()))
                {
                    throw new ServiceException("仓库编码不能为空：自动编码已关闭，请手动填写仓库编码");
                }
                checkCodeUnique(warehouse.getWarehouseCode(), null);
            }
        }
        else if (StringUtils.isEmpty(warehouse.getWarehouseCode()))
        {
            // 仓区/仓位使用层级编码（父编码+后缀）
            warehouse.setWarehouseCode(generateNextCode(warehouse));
        }
        // 维护 ancestors
        buildAncestors(warehouse);
        // 仓位默认状态
        if ("3".equals(warehouse.getNodeType()) && warehouse.getLocationStatus() == null)
        {
            warehouse.setLocationStatus("0");
        }
        try
        {
            return wmsWarehouseMapper.insertWarehouse(warehouse);
        }
        catch (DuplicateKeyException e)
        {
            // 唯一性冲突给出明确业务提示（如删除后重新新增生成的编码与软删除记录撞车）
            throw new ServiceException("保存失败：编码[" + warehouse.getWarehouseCode() + "]已被占用，请刷新页面后重新新增");
        }
    }

    @Override
    public int updateWarehouse(WmsWarehouse warehouse)
    {
        // 如果修改了父节点，需要更新 ancestors
        WmsWarehouse old = wmsWarehouseMapper.selectWarehouseById(warehouse.getWarehouseId());
        if (old != null)
        {
            if (isAutoCodeEnabled())
            {
                // 自动编码开启：编码只读，忽略前端提交的编码变更，保留原编码
                warehouse.setWarehouseCode(old.getWarehouseCode());
            }
            else
            {
                // 自动编码关闭：所有节点类型（仓库/仓区/仓位）允许手动修改编码，校验不能重复（排除自身）
                if (StringUtils.isEmpty(warehouse.getWarehouseCode()))
                {
                    throw new ServiceException("仓库编码不能为空：自动编码已关闭，请手动填写编码");
                }
                if (!warehouse.getWarehouseCode().equals(old.getWarehouseCode()))
                {
                    checkCodeUnique(warehouse.getWarehouseCode(), warehouse.getWarehouseId());
                }
            }
        }
        if (old != null && warehouse.getParentId() != null
                && !warehouse.getParentId().equals(old.getParentId()))
        {
            buildAncestors(warehouse);
        }
        int rows = wmsWarehouseMapper.updateWarehouse(warehouse);
        // 编码变更后级联更新子节点编码前缀，保持层级编码一致（如仓区 WH001A01 改为 WH001B01，其下仓位 WH001A01L01 → WH001B01L01）
        if (old != null && !old.getWarehouseCode().equals(warehouse.getWarehouseCode()))
        {
            cascadeChildCodes(warehouse.getWarehouseId(), old.getWarehouseCode(), warehouse.getWarehouseCode());
        }
        return rows;
    }

    /** 级联更新子节点编码：子节点编码以旧父编码为前缀的部分替换为新父编码，递归处理 */
    private void cascadeChildCodes(Long parentId, String oldCode, String newCode)
    {
        List<WmsWarehouse> children = wmsWarehouseMapper.selectChildByParentId(parentId);
        for (WmsWarehouse child : children)
        {
            String childCode = child.getWarehouseCode();
            if (StringUtils.isNotEmpty(childCode) && childCode.startsWith(oldCode))
            {
                String updated = newCode + childCode.substring(oldCode.length());
                WmsWarehouse update = new WmsWarehouse();
                update.setWarehouseId(child.getWarehouseId());
                update.setWarehouseCode(updated);
                wmsWarehouseMapper.updateWarehouse(update);
                cascadeChildCodes(child.getWarehouseId(), childCode, updated);
            }
        }
    }

    @Override
    public int deleteWarehouseByIds(Long[] warehouseIds)
    {
        for (Long id : warehouseIds)
        {
            int childCount = wmsWarehouseMapper.countChildByParentId(id);
            if (childCount > 0)
            {
                WmsWarehouse wh = wmsWarehouseMapper.selectWarehouseById(id);
                String name = wh != null ? wh.getWarehouseName() : String.valueOf(id);
                throw new ServiceException("[" + name + "] 存在子节点，不允许删除");
            }
        }
        return wmsWarehouseMapper.deleteWarehouseByIds(warehouseIds);
    }

    @Override
    public String generateNextCode(WmsWarehouse warehouse)
    {
        String nodeType = warehouse.getNodeType();
        Long parentId = warehouse.getParentId();
        String maxCode = wmsWarehouseMapper.selectMaxCodeByParent(parentId, nodeType);

        if ("1".equals(nodeType))
        {
            // 仓库: WH + 3位序号
            int seq = 1;
            if (maxCode != null && maxCode.startsWith("WH"))
            {
                try { seq = Integer.parseInt(maxCode.substring(2)) + 1; } catch (Exception e) { seq = 1; }
            }
            return String.format("WH%03d", seq);
        }
        else if ("2".equals(nodeType))
        {
            // 仓区: 父编码 + A + 2位序号
            WmsWarehouse parent = wmsWarehouseMapper.selectWarehouseById(parentId);
            String prefix = parent != null ? parent.getWarehouseCode() : "WH000";
            int seq = 1;
            if (maxCode != null && maxCode.startsWith(prefix + "A"))
            {
                try { seq = Integer.parseInt(maxCode.substring(prefix.length() + 1)) + 1; } catch (Exception e) { seq = 1; }
            }
            return prefix + String.format("A%02d", seq);
        }
        else if ("3".equals(nodeType))
        {
            // 仓位: 父编码 + L + 2位序号
            WmsWarehouse parent = wmsWarehouseMapper.selectWarehouseById(parentId);
            String prefix = parent != null ? parent.getWarehouseCode() : "WH000A00";
            int seq = 1;
            if (maxCode != null && maxCode.startsWith(prefix + "L"))
            {
                try { seq = Integer.parseInt(maxCode.substring(prefix.length() + 1)) + 1; } catch (Exception e) { seq = 1; }
            }
            return prefix + String.format("L%02d", seq);
        }
        return null;
    }

    /**
     * 构建ancestors字段
     */
    private void buildAncestors(WmsWarehouse warehouse)
    {
        Long parentId = warehouse.getParentId();
        if (parentId == null || parentId == 0L)
        {
            warehouse.setAncestors("0");
        }
        else
        {
            WmsWarehouse parent = wmsWarehouseMapper.selectWarehouseById(parentId);
            if (parent != null)
            {
                warehouse.setAncestors(parent.getAncestors() + "," + parentId);
            }
            else
            {
                warehouse.setAncestors("0");
            }
        }
    }

    /** 仓库编码规则「是否启用自动编码」开关（规则缺失时默认开启，保持向后兼容） */
    @Override
    public boolean isAutoCodeEnabled()
    {
        com.ruoyi.mk.domain.MkNumberRule rule = mkNumberRuleService.selectRuleByCode("wms_warehouse");
        return rule == null || !"0".equals(rule.getAutoEnabled());
    }

    /** 仓库编码唯一性校验（excludeId 为编辑时排除自身，可为 null） */
    private void checkCodeUnique(String warehouseCode, Long excludeId)
    {
        Long count = wmsWarehouseMapper.checkWarehouseCodeUnique(warehouseCode, excludeId);
        if (count != null && count > 0)
        {
            throw new ServiceException("仓库编码“" + warehouseCode + "”已存在，请更换编码");
        }
    }
}

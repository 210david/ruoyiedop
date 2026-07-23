package com.ruoyi.wms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
        // 自动生成编码（所有节点类型）
        if (StringUtils.isEmpty(warehouse.getWarehouseCode()))
        {
            if ("1".equals(warehouse.getNodeType()))
            {
                // 仓库级别使用编号规则
                warehouse.setWarehouseCode(mkNumberRuleService.generateNumber("wms_warehouse"));
            }
            else
            {
                // 仓区/仓位使用层级编码（父编码+后缀）
                warehouse.setWarehouseCode(generateNextCode(warehouse));
            }
        }
        // 维护 ancestors
        buildAncestors(warehouse);
        // 仓位默认状态
        if ("3".equals(warehouse.getNodeType()) && warehouse.getLocationStatus() == null)
        {
            warehouse.setLocationStatus("0");
        }
        return wmsWarehouseMapper.insertWarehouse(warehouse);
    }

    @Override
    public int updateWarehouse(WmsWarehouse warehouse)
    {
        // 如果修改了父节点，需要更新 ancestors
        WmsWarehouse old = wmsWarehouseMapper.selectWarehouseById(warehouse.getWarehouseId());
        if (old != null && warehouse.getParentId() != null
                && !warehouse.getParentId().equals(old.getParentId()))
        {
            buildAncestors(warehouse);
        }
        return wmsWarehouseMapper.updateWarehouse(warehouse);
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
}

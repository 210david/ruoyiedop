package com.ruoyi.wms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsInventory;
import com.ruoyi.wms.domain.WmsMoveOrder;
import com.ruoyi.wms.mapper.WmsInventoryMapper;
import com.ruoyi.wms.mapper.WmsMoveOrderMapper;
import com.ruoyi.wms.service.IWmsMoveOrderService;
import com.ruoyi.wms.service.IWmsInventoryService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class WmsMoveOrderServiceImpl implements IWmsMoveOrderService
{
    @Autowired
    private WmsMoveOrderMapper wmsMoveOrderMapper;

    @Autowired
    private IWmsInventoryService wmsInventoryService;

    @Autowired
    private WmsInventoryMapper wmsInventoryMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<WmsMoveOrder> selectMoveOrderList(WmsMoveOrder move)
    {
        return wmsMoveOrderMapper.selectMoveOrderList(move);
    }

    @Override
    public WmsMoveOrder selectMoveOrderById(Long moveId)
    {
        return wmsMoveOrderMapper.selectMoveOrderById(moveId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMoveOrder(WmsMoveOrder move)
    {
        if (StringUtils.isEmpty(move.getMoveNo()))
        {
            move.setMoveNo(mkNumberRuleService.generateNumber("wms_move"));
        }
        // 校验移库数量
        if (move.getMoveQty() == null || move.getMoveQty().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("移库数量必须大于0");
        }
        // 校验源库位和目标库位不能相同
        if (move.getFromLocationId() != null && move.getFromLocationId().equals(move.getToLocationId()))
        {
            throw new ServiceException("目标库位不能与源库位相同");
        }
        // 校验源库位库存是否充足
        WmsInventory queryInv = new WmsInventory();
        queryInv.setWarehouseId(move.getWarehouseId());
        queryInv.setMaterialId(move.getMaterialId());
        queryInv.setLocationId(move.getFromLocationId());
        if (move.getBatchNo() != null && !move.getBatchNo().isEmpty())
        {
            queryInv.setBatchNo(move.getBatchNo());
        }
        List<WmsInventory> invList = wmsInventoryMapper.selectInventoryList(queryInv);
        if (invList == null || invList.isEmpty())
        {
            throw new ServiceException("源库位不存在该物料库存");
        }
        WmsInventory inv = invList.get(0);
        if (inv.getQty().compareTo(move.getMoveQty()) < 0)
        {
            throw new ServiceException("源库位库存不足，可用数量：" + inv.getQty() + "，移库数量：" + move.getMoveQty());
        }
        move.setDelFlag("0");
        if (move.getStatus() == null)
        {
            move.setStatus("0");
        }
        return wmsMoveOrderMapper.insertMoveOrder(move);
    }

    @Override
    public int updateMoveOrder(WmsMoveOrder move)
    {
        return wmsMoveOrderMapper.updateMoveOrder(move);
    }

    @Override
    public int deleteMoveOrderByIds(Long[] moveIds)
    {
        return wmsMoveOrderMapper.deleteMoveOrderByIds(moveIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveMove(Long moveId)
    {
        WmsMoveOrder move = wmsMoveOrderMapper.selectMoveOrderById(moveId);
        if (move == null || !"0".equals(move.getStatus()))
        {
            throw new ServiceException("移库单状态不正确，无法审批");
        }
        move.setStatus("1");
        move.setApproveBy(SecurityUtils.getUsername());
        move.setApproveTime(new Date());
        return wmsMoveOrderMapper.updateMoveOrder(move);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeMove(Long moveId)
    {
        WmsMoveOrder move = wmsMoveOrderMapper.selectMoveOrderById(moveId);
        if (move == null || !"1".equals(move.getStatus()))
        {
            throw new ServiceException("移库单状态不正确，无法执行");
        }
        String username = SecurityUtils.getUsername();
        // reduce from source location
        wmsInventoryService.reduceInventory(move.getMaterialId(), move.getWarehouseId(),
                move.getFromLocationId(), move.getBatchNo(), move.getMoveQty(), "3", "move_out", move.getMoveNo(), username);
        // add to target location
        wmsInventoryService.addInventory(move.getMaterialId(), move.getWarehouseId(),
                move.getToLocationId(), move.getBatchNo(), move.getMoveQty(), "2", "move_in", move.getMoveNo(), username, null, null);
        // update status
        move.setStatus("2");
        move.setCompleteTime(new Date());
        return wmsMoveOrderMapper.updateMoveOrder(move);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int voidMove(Long moveId)
    {
        WmsMoveOrder move = wmsMoveOrderMapper.selectMoveOrderById(moveId);
        if (move == null)
        {
            throw new ServiceException("移库单不存在");
        }
        // 只有待审批(0)和已批准(1)状态可以作废，已完成(2)不可作废
        if (!"0".equals(move.getStatus()) && !"1".equals(move.getStatus()))
        {
            throw new ServiceException("当前状态无法作废，只有待审批或已批准的移库单可以作废");
        }
        move.setStatus("3");
        return wmsMoveOrderMapper.updateMoveOrder(move);
    }
}

package com.ruoyi.wms.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.wms.domain.WmsWave;
import com.ruoyi.wms.domain.WmsOutboundOrder;
import com.ruoyi.wms.mapper.WmsWaveMapper;
import com.ruoyi.wms.mapper.WmsOutboundOrderMapper;
import com.ruoyi.wms.service.IWmsWaveService;

@Service
public class WmsWaveServiceImpl implements IWmsWaveService
{
    @Autowired
    private WmsWaveMapper wmsWaveMapper;

    @Autowired
    private WmsOutboundOrderMapper wmsOutboundOrderMapper;

    @Override
    public List<WmsWave> selectWaveList(WmsWave wave)
    {
        return wmsWaveMapper.selectWaveList(wave);
    }

    @Override
    public WmsWave selectWaveById(Long waveId)
    {
        return wmsWaveMapper.selectWaveById(waveId);
    }

    @Override
    public int insertWave(WmsWave wave)
    {
        if (wave.getWaveNo() == null || wave.getWaveNo().isEmpty())
        {
            wave.setWaveNo("WAVE" + System.currentTimeMillis());
        }
        wave.setDelFlag("0");
        if (wave.getStatus() == null)
        {
            wave.setStatus("0");
        }
        if (wave.getTotalQty() == null)
        {
            wave.setTotalQty(BigDecimal.ZERO);
        }
        if (wave.getOrderCount() == null)
        {
            wave.setOrderCount(0);
        }
        return wmsWaveMapper.insertWave(wave);
    }

    @Override
    public int updateWave(WmsWave wave)
    {
        return wmsWaveMapper.updateWave(wave);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWaveByIds(Long[] waveIds)
    {
        // 释放关联的出库单
        for (Long waveId : waveIds)
        {
            wmsOutboundOrderMapper.clearOutboundWaveId(waveId);
        }
        return wmsWaveMapper.deleteWaveByIds(waveIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateWave(Long waveId, Long[] orderIds)
    {
        WmsWave wave = wmsWaveMapper.selectWaveById(waveId);
        if (wave == null)
        {
            throw new ServiceException("波次不存在");
        }
        if (!"0".equals(wave.getStatus()) && !"1".equals(wave.getStatus()))
        {
            throw new ServiceException("波次状态不正确，无法添加出库单");
        }
        if (orderIds == null || orderIds.length == 0)
        {
            throw new ServiceException("请选择出库单");
        }

        BigDecimal totalQty = wave.getTotalQty() != null ? wave.getTotalQty() : BigDecimal.ZERO;
        int orderCount = wave.getOrderCount() != null ? wave.getOrderCount() : 0;

        for (Long orderId : orderIds)
        {
            WmsOutboundOrder order = wmsOutboundOrderMapper.selectOutboundOrderById(orderId);
            if (order == null)
            {
                throw new ServiceException("出库单[" + orderId + "]不存在");
            }
            if (order.getWaveId() != null)
            {
                throw new ServiceException("出库单[" + order.getOrderNo() + "]已关联其他波次");
            }
            if (!"0".equals(order.getStatus()))
            {
                throw new ServiceException("出库单[" + order.getOrderNo() + "]状态不正确，仅草稿状态可加入波次");
            }
            if (!order.getWarehouseId().equals(wave.getWarehouseId()))
            {
                throw new ServiceException("出库单[" + order.getOrderNo() + "]仓库与波次仓库不一致");
            }
            // 关联波次，出库单状态变为待拣货
            wmsOutboundOrderMapper.updateOutboundWaveId(orderId, waveId, "1");
            if (order.getTotalQty() != null)
            {
                totalQty = totalQty.add(order.getTotalQty());
            }
            orderCount++;
        }

        // 更新波次汇总信息
        wave.setStatus("1");
        wave.setTotalQty(totalQty);
        wave.setOrderCount(orderCount);
        return wmsWaveMapper.updateWave(wave);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseWave(Long waveId)
    {
        WmsWave wave = wmsWaveMapper.selectWaveById(waveId);
        if (wave == null)
        {
            throw new ServiceException("波次不存在");
        }
        if ("2".equals(wave.getStatus()) || "3".equals(wave.getStatus()))
        {
            throw new ServiceException("已完成或已作废的波次无法释放");
        }

        // 解除所有出库单的波次关联，恢复为草稿状态
        wmsOutboundOrderMapper.clearOutboundWaveId(waveId);

        // 重置波次信息
        wave.setStatus("0");
        wave.setTotalQty(BigDecimal.ZERO);
        wave.setOrderCount(0);
        return wmsWaveMapper.updateWave(wave);
    }

    @Override
    public List<WmsOutboundOrder> selectWaveOrders(Long waveId)
    {
        return wmsOutboundOrderMapper.selectOutboundOrdersByWaveId(waveId);
    }
}

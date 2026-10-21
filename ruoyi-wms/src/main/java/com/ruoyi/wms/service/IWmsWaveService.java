package com.ruoyi.wms.service;

import java.util.List;
import com.ruoyi.wms.domain.WmsWave;
import com.ruoyi.wms.domain.WmsOutboundOrder;

public interface IWmsWaveService
{
    public List<WmsWave> selectWaveList(WmsWave wave);
    public WmsWave selectWaveById(Long waveId);
    public int insertWave(WmsWave wave);
    public int updateWave(WmsWave wave);
    public int deleteWaveByIds(Long[] waveIds);

    /**
     * 生成波次：将多个出库单关联到波次
     */
    public int generateWave(Long waveId, Long[] orderIds);

    /**
     * 释放波次：解除出库单与波次的关联
     */
    public int releaseWave(Long waveId);

    /**
     * 查询波次关联的出库单列表
     */
    public List<WmsOutboundOrder> selectWaveOrders(Long waveId);
}

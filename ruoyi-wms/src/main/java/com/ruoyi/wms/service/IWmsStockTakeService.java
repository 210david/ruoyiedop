package com.ruoyi.wms.service;

import java.util.List;
import com.ruoyi.wms.domain.WmsStockTake;

public interface IWmsStockTakeService
{
    public List<WmsStockTake> selectStockTakeList(WmsStockTake take);
    public WmsStockTake selectStockTakeById(Long takeId);
    public int insertStockTake(WmsStockTake take);
    public int updateStockTake(WmsStockTake take);
    public int deleteStockTakeByIds(Long[] takeIds);
    public int startTake(Long takeId);
    public int submitDetail(Long takeId, Long detailId, java.math.BigDecimal actualQty, String diffReason);
    public int approveTake(Long takeId);
    public int voidTake(Long takeId);
}

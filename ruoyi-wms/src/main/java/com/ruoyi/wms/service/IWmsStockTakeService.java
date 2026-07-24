package com.ruoyi.wms.service;

import java.util.List;
import com.ruoyi.wms.domain.WmsStockTake;
import com.ruoyi.wms.domain.WmsStockTakeDetail;

public interface IWmsStockTakeService
{
    public List<WmsStockTake> selectStockTakeList(WmsStockTake take);
    public WmsStockTake selectStockTakeById(Long takeId);
    public int insertStockTake(WmsStockTake take);
    public int updateStockTake(WmsStockTake take);
    public int deleteStockTakeByIds(Long[] takeIds);
    public int startTake(Long takeId);
    public int submitDetail(Long takeId, Long detailId, java.math.BigDecimal actualQty, String diffReason);
    public int batchSubmitDetail(Long takeId, List<WmsStockTakeDetail> detailList);
    public String importDetail(Long takeId, List<WmsStockTakeDetail> detailList);
    public int submitForApproval(Long takeId);
    public int approveTake(Long takeId, String approveOpinion);
    public int rejectTake(Long takeId, String approveOpinion);
    public int voidTake(Long takeId);
}

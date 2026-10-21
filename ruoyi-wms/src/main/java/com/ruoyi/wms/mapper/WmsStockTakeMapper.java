package com.ruoyi.wms.mapper;

import java.util.List;
import com.ruoyi.wms.domain.WmsStockTake;
import com.ruoyi.wms.domain.WmsStockTakeDetail;
import com.ruoyi.wms.domain.WmsStockTakeSnapshot;
import com.ruoyi.wms.domain.WmsStockTakeApproveLog;

public interface WmsStockTakeMapper
{
    public List<WmsStockTake> selectStockTakeList(WmsStockTake take);
    public WmsStockTake selectStockTakeById(Long takeId);
    public int insertStockTake(WmsStockTake take);
    public int updateStockTake(WmsStockTake take);
    public int deleteStockTakeByIds(Long[] takeIds);
    public List<WmsStockTakeDetail> selectStockTakeDetailByTakeId(Long takeId);
    public int insertStockTakeDetail(WmsStockTakeDetail detail);
    public int updateStockTakeDetail(WmsStockTakeDetail detail);
    public int insertSnapshot(WmsStockTakeSnapshot snapshot);
    public List<WmsStockTakeSnapshot> selectSnapshotByTakeId(Long takeId);
    public int insertApproveLog(WmsStockTakeApproveLog log);
    public List<WmsStockTakeApproveLog> selectApproveLogByTakeId(Long takeId);
}

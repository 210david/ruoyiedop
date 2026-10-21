package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsWoRouteSnapshot;

/**
 * 工单工艺快照 Mapper
 *
 * @author ruoyi
 */
public interface MmsWoRouteSnapshotMapper
{
    /** 批量插入工单工艺快照 */
    public int batchInsertRouteSnapshot(List<MmsWoRouteSnapshot> list);

    /** 按工单ID删除工艺快照（物理删除，用于重复下达时先清理） */
    public int deleteRouteSnapshotByWorkOrderId(Long workOrderId);

    /** 按工单ID查询工艺快照列表 */
    public List<MmsWoRouteSnapshot> selectRouteSnapshotByWorkOrderId(Long workOrderId);
}

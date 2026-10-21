package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsWoBomSnapshot;

/**
 * 工单BOM快照 Mapper
 *
 * @author ruoyi
 */
public interface MmsWoBomSnapshotMapper
{
    /** 批量插入工单BOM快照 */
    public int batchInsertBomSnapshot(List<MmsWoBomSnapshot> list);

    /** 按工单ID删除BOM快照（物理删除，用于重复下达时先清理） */
    public int deleteBomSnapshotByWorkOrderId(Long workOrderId);

    /** 按工单ID查询BOM快照列表 */
    public List<MmsWoBomSnapshot> selectBomSnapshotByWorkOrderId(Long workOrderId);
}

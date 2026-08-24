package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsOeeSnapshot;

public interface MmsOeeSnapshotMapper
{
    public List<MmsOeeSnapshot> selectOeeSnapshotList(MmsOeeSnapshot snapshot);
    public MmsOeeSnapshot selectOeeSnapshotById(Long snapshotId);
    public long selectOeeSnapshotCount(MmsOeeSnapshot snapshot);
}

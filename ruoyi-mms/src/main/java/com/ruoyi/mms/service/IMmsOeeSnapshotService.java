package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsOeeSnapshot;

public interface IMmsOeeSnapshotService
{
    public List<MmsOeeSnapshot> selectOeeSnapshotList(MmsOeeSnapshot snapshot);
    public MmsOeeSnapshot selectOeeSnapshotById(Long snapshotId);
    public long selectOeeSnapshotCount(MmsOeeSnapshot snapshot);
}

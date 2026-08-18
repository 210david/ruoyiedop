package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mms.domain.MmsOeeSnapshot;
import com.ruoyi.mms.mapper.MmsOeeSnapshotMapper;
import com.ruoyi.mms.service.IMmsOeeSnapshotService;

@Service
public class MmsOeeSnapshotServiceImpl implements IMmsOeeSnapshotService
{
    @Autowired
    private MmsOeeSnapshotMapper oeeSnapshotMapper;

    @Override
    public List<MmsOeeSnapshot> selectOeeSnapshotList(MmsOeeSnapshot snapshot)
    {
        return oeeSnapshotMapper.selectOeeSnapshotList(snapshot);
    }

    @Override
    public MmsOeeSnapshot selectOeeSnapshotById(Long snapshotId)
    {
        return oeeSnapshotMapper.selectOeeSnapshotById(snapshotId);
    }
}

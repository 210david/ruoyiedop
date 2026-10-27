package com.ruoyi.wms.mapper;

import java.util.List;
import com.ruoyi.wms.domain.WmsWave;

public interface WmsWaveMapper
{
    public List<WmsWave> selectWaveList(WmsWave wave);
    public WmsWave selectWaveById(Long waveId);
    public int insertWave(WmsWave wave);
    public int updateWave(WmsWave wave);
    public int deleteWaveByIds(Long[] waveIds);
}

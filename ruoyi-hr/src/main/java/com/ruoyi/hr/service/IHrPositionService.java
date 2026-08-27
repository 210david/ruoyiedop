package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrPosition;

public interface IHrPositionService {
    public HrPosition selectHrPositionById(Long positionId);
    public List<HrPosition> selectHrPositionList(HrPosition hrPosition);
    public int insertHrPosition(HrPosition hrPosition);
    public int updateHrPosition(HrPosition hrPosition);
    public int deleteHrPositionByIds(Long[] positionIds);
    public int deleteHrPositionById(Long positionId);
}

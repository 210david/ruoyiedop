package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrPosition;
import com.ruoyi.hr.mapper.HrPositionMapper;
import com.ruoyi.hr.service.IHrPositionService;

@Service
public class HrPositionServiceImpl implements IHrPositionService
{
    @Autowired
    private HrPositionMapper hrPositionMapper;

    @Override
    public HrPosition selectHrPositionById(Long positionId) { return hrPositionMapper.selectHrPositionById(positionId); }

    @Override
    public List<HrPosition> selectHrPositionList(HrPosition hrPosition) { return hrPositionMapper.selectHrPositionList(hrPosition); }

    @Override
    public int insertHrPosition(HrPosition hrPosition) { return hrPositionMapper.insertHrPosition(hrPosition); }

    @Override
    public int updateHrPosition(HrPosition hrPosition) { return hrPositionMapper.updateHrPosition(hrPosition); }

    @Override
    public int deleteHrPositionByIds(Long[] positionIds) { return hrPositionMapper.deleteHrPositionByIds(positionIds); }

    @Override
    public int deleteHrPositionById(Long positionId) { return hrPositionMapper.deleteHrPositionById(positionId); }
}

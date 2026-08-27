package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrRegular;
import com.ruoyi.hr.mapper.HrRegularMapper;
import com.ruoyi.hr.service.IHrRegularService;

@Service
public class HrRegularServiceImpl implements IHrRegularService
{
    @Autowired
    private HrRegularMapper hrRegularMapper;

    @Override
    public HrRegular selectHrRegularById(Long regularId) { return hrRegularMapper.selectHrRegularById(regularId); }

    @Override
    public List<HrRegular> selectHrRegularList(HrRegular hrRegular) { return hrRegularMapper.selectHrRegularList(hrRegular); }

    @Override
    public int insertHrRegular(HrRegular hrRegular) { return hrRegularMapper.insertHrRegular(hrRegular); }

    @Override
    public int updateHrRegular(HrRegular hrRegular) { return hrRegularMapper.updateHrRegular(hrRegular); }

    @Override
    public int deleteHrRegularByIds(Long[] regularIds) { return hrRegularMapper.deleteHrRegularByIds(regularIds); }

    @Override
    public int deleteHrRegularById(Long regularId) { return hrRegularMapper.deleteHrRegularById(regularId); }
}

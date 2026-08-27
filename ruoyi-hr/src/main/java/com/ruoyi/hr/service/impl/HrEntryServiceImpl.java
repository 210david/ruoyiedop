package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrEntry;
import com.ruoyi.hr.mapper.HrEntryMapper;
import com.ruoyi.hr.service.IHrEntryService;

@Service
public class HrEntryServiceImpl implements IHrEntryService
{
    @Autowired
    private HrEntryMapper hrEntryMapper;

    @Override
    public HrEntry selectHrEntryById(Long entryId) { return hrEntryMapper.selectHrEntryById(entryId); }

    @Override
    public List<HrEntry> selectHrEntryList(HrEntry hrEntry) { return hrEntryMapper.selectHrEntryList(hrEntry); }

    @Override
    public int insertHrEntry(HrEntry hrEntry) { return hrEntryMapper.insertHrEntry(hrEntry); }

    @Override
    public int updateHrEntry(HrEntry hrEntry) { return hrEntryMapper.updateHrEntry(hrEntry); }

    @Override
    public int deleteHrEntryByIds(Long[] entryIds) { return hrEntryMapper.deleteHrEntryByIds(entryIds); }

    @Override
    public int deleteHrEntryById(Long entryId) { return hrEntryMapper.deleteHrEntryById(entryId); }
}

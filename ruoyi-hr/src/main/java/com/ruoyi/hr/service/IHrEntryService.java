package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrEntry;

public interface IHrEntryService {
    public HrEntry selectHrEntryById(Long entryId);
    public List<HrEntry> selectHrEntryList(HrEntry hrEntry);
    public int insertHrEntry(HrEntry hrEntry);
    public int updateHrEntry(HrEntry hrEntry);
    public int deleteHrEntryByIds(Long[] entryIds);
    public int deleteHrEntryById(Long entryId);
}

package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrPieceworkRecord;
import com.ruoyi.hr.mapper.HrPieceworkRecordMapper;
import com.ruoyi.hr.service.IHrPieceworkRecordService;

@Service
public class HrPieceworkRecordServiceImpl implements IHrPieceworkRecordService
{
    @Autowired
    private HrPieceworkRecordMapper hrPieceworkRecordMapper;

    @Override
    public HrPieceworkRecord selectHrPieceworkRecordById(Long pwRecordId) { return hrPieceworkRecordMapper.selectHrPieceworkRecordById(pwRecordId); }

    @Override
    public List<HrPieceworkRecord> selectHrPieceworkRecordList(HrPieceworkRecord hrPieceworkRecord) { return hrPieceworkRecordMapper.selectHrPieceworkRecordList(hrPieceworkRecord); }

    @Override
    public int insertHrPieceworkRecord(HrPieceworkRecord hrPieceworkRecord) { return hrPieceworkRecordMapper.insertHrPieceworkRecord(hrPieceworkRecord); }

    @Override
    public int updateHrPieceworkRecord(HrPieceworkRecord hrPieceworkRecord) { return hrPieceworkRecordMapper.updateHrPieceworkRecord(hrPieceworkRecord); }

    @Override
    public int deleteHrPieceworkRecordByIds(Long[] pwRecordIds) { return hrPieceworkRecordMapper.deleteHrPieceworkRecordByIds(pwRecordIds); }

    @Override
    public int deleteHrPieceworkRecordById(Long pwRecordId) { return hrPieceworkRecordMapper.deleteHrPieceworkRecordById(pwRecordId); }
}

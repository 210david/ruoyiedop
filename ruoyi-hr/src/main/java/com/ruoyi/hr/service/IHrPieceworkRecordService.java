package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrPieceworkRecord;

public interface IHrPieceworkRecordService {
    public HrPieceworkRecord selectHrPieceworkRecordById(Long pwRecordId);
    public List<HrPieceworkRecord> selectHrPieceworkRecordList(HrPieceworkRecord hrPieceworkRecord);
    public int insertHrPieceworkRecord(HrPieceworkRecord hrPieceworkRecord);
    public int updateHrPieceworkRecord(HrPieceworkRecord hrPieceworkRecord);
    public int deleteHrPieceworkRecordByIds(Long[] pwRecordIds);
    public int deleteHrPieceworkRecordById(Long pwRecordId);
}

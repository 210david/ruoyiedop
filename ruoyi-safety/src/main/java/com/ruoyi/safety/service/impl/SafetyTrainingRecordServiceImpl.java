package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.safety.domain.SafetyTrainingRecord;
import com.ruoyi.safety.mapper.SafetyTrainingRecordMapper;
import com.ruoyi.safety.service.ISafetyTrainingRecordService;

@Service
public class SafetyTrainingRecordServiceImpl implements ISafetyTrainingRecordService
{
    @Autowired
    private SafetyTrainingRecordMapper safetyTrainingRecordMapper;

    @Override
    public SafetyTrainingRecord selectSafetyTrainingRecordById(Long recordId) { return safetyTrainingRecordMapper.selectSafetyTrainingRecordById(recordId); }

    @Override
    public List<SafetyTrainingRecord> selectSafetyTrainingRecordList(SafetyTrainingRecord safetyTrainingRecord) { return safetyTrainingRecordMapper.selectSafetyTrainingRecordList(safetyTrainingRecord); }

    @Override
    public int insertSafetyTrainingRecord(SafetyTrainingRecord safetyTrainingRecord) { return safetyTrainingRecordMapper.insertSafetyTrainingRecord(safetyTrainingRecord); }

    @Override
    public int updateSafetyTrainingRecord(SafetyTrainingRecord safetyTrainingRecord) { return safetyTrainingRecordMapper.updateSafetyTrainingRecord(safetyTrainingRecord); }

    @Override
    public int deleteSafetyTrainingRecordByIds(Long[] recordIds) { return safetyTrainingRecordMapper.deleteSafetyTrainingRecordByIds(recordIds); }

    @Override
    public int deleteSafetyTrainingRecordById(Long recordId) { return safetyTrainingRecordMapper.deleteSafetyTrainingRecordById(recordId); }
}
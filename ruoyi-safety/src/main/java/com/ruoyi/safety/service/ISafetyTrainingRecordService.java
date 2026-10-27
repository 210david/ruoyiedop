package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyTrainingRecord;

public interface ISafetyTrainingRecordService {
    public SafetyTrainingRecord selectSafetyTrainingRecordById(Long recordId);
    public List<SafetyTrainingRecord> selectSafetyTrainingRecordList(SafetyTrainingRecord safetyTrainingRecord);
    public int insertSafetyTrainingRecord(SafetyTrainingRecord safetyTrainingRecord);
    public int updateSafetyTrainingRecord(SafetyTrainingRecord safetyTrainingRecord);
    public int deleteSafetyTrainingRecordByIds(Long[] recordIds);
    public int deleteSafetyTrainingRecordById(Long recordId);
}
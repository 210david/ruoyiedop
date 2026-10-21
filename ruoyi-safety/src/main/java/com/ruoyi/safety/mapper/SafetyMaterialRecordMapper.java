package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyMaterialRecord;

public interface SafetyMaterialRecordMapper {
    public SafetyMaterialRecord selectSafetyMaterialRecordById(Long recordId);
    public List<SafetyMaterialRecord> selectSafetyMaterialRecordList(SafetyMaterialRecord record);
    public int insertSafetyMaterialRecord(SafetyMaterialRecord record);
    public int updateSafetyMaterialRecord(SafetyMaterialRecord record);
    public int deleteSafetyMaterialRecordByIds(Long[] recordIds);
    public int deleteSafetyMaterialRecordById(Long recordId);
}

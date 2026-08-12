package com.ruoyi.safety.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.safety.domain.SafetyTrainingCert;

public interface SafetyTrainingCertMapper {
    public SafetyTrainingCert selectSafetyTrainingCertById(Long certId);
    public List<SafetyTrainingCert> selectSafetyTrainingCertList(SafetyTrainingCert safetyTrainingCert);
    public int insertSafetyTrainingCert(SafetyTrainingCert safetyTrainingCert);
    public int updateSafetyTrainingCert(SafetyTrainingCert safetyTrainingCert);
    public int deleteSafetyTrainingCertByIds(Long[] certIds);
    public int deleteSafetyTrainingCertById(Long certId);
    /** 查询是否已存在相同培训记录+用户的证书 */
    public SafetyTrainingCert selectByRecordIdAndUserId(@Param("recordId") Long recordId, @Param("userId") Long userId);
}

package com.ruoyi.pjm.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.pjm.domain.PjmRisk;

/**
 * 项目风险/问题 Service
 */
public interface IPjmRiskService
{
    PjmRisk selectRiskById(Long riskId);

    List<PjmRisk> selectRiskList(PjmRisk risk);

    int insertRisk(PjmRisk risk);

    int updateRisk(PjmRisk risk);

    int deleteRiskByIds(Long[] riskIds);

    /** ★ 按工程类别从国家标准风险点库返回推荐风险点列表（keyword 可选） */
    List<Map<String, Object>> matchStandardRisks(String engineeringCategory, String keyword);
}

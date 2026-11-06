package com.ruoyi.pjm.mapper;

import java.util.List;
import com.ruoyi.pjm.domain.PjmRisk;

/**
 * 项目风险/问题 Mapper
 */
public interface PjmRiskMapper
{
    PjmRisk selectRiskById(Long riskId);

    List<PjmRisk> selectRiskList(PjmRisk risk);

    int insertRisk(PjmRisk risk);

    int updateRisk(PjmRisk risk);

    int deleteRiskByIds(Long[] riskIds);
}

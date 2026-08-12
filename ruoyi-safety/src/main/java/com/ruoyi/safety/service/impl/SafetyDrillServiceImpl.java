package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.safety.domain.SafetyDrill;
import com.ruoyi.safety.mapper.SafetyDrillMapper;
import com.ruoyi.safety.service.ISafetyDrillService;

@Service
public class SafetyDrillServiceImpl implements ISafetyDrillService
{
    @Autowired
    private SafetyDrillMapper safetyDrillMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public SafetyDrill selectSafetyDrillById(Long drillId) { return safetyDrillMapper.selectSafetyDrillById(drillId); }

    @Override
    public List<SafetyDrill> selectSafetyDrillList(SafetyDrill safetyDrill) { return safetyDrillMapper.selectSafetyDrillList(safetyDrill); }

    @Override
    public int insertSafetyDrill(SafetyDrill safetyDrill) {
        if (StringUtils.isEmpty(safetyDrill.getDrillCode())) {
            safetyDrill.setDrillCode(mkNumberRuleService.generateNumber("safety_drill"));
        }
        return safetyDrillMapper.insertSafetyDrill(safetyDrill); }

    @Override
    public int updateSafetyDrill(SafetyDrill safetyDrill) { return safetyDrillMapper.updateSafetyDrill(safetyDrill); }

    @Override
    public int deleteSafetyDrillByIds(Long[] drillIds) { return safetyDrillMapper.deleteSafetyDrillByIds(drillIds); }

    @Override
    public int deleteSafetyDrillById(Long drillId) { return safetyDrillMapper.deleteSafetyDrillById(drillId); }
}
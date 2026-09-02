package com.ruoyi.safety.service.impl;
import java.util.HashMap;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.safety.domain.SafetyRiskPoint;
import com.ruoyi.safety.mapper.SafetyRiskPointMapper;
import com.ruoyi.safety.service.ISafetyRiskPointService;

@Service
public class SafetyRiskPointServiceImpl implements ISafetyRiskPointService
{
    @Autowired
    private SafetyRiskPointMapper safetyRiskPointMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public SafetyRiskPoint selectSafetyRiskPointById(Long riskPointId) { return safetyRiskPointMapper.selectSafetyRiskPointById(riskPointId); }

    @Override
    public List<SafetyRiskPoint> selectSafetyRiskPointList(SafetyRiskPoint safetyRiskPoint) { return safetyRiskPointMapper.selectSafetyRiskPointList(safetyRiskPoint); }

    @Override
    public int insertSafetyRiskPoint(SafetyRiskPoint safetyRiskPoint)
    {
        if (StringUtils.isEmpty(safetyRiskPoint.getRiskCode()))
        {
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(safetyRiskPoint.getRiskLevel())) { params.put("riskLevel", safetyRiskPoint.getRiskLevel()); }
        safetyRiskPoint.setRiskCode(mkNumberRuleService.generateNumber("safety_risk_point", params));
        }
        calcRiskLevel(safetyRiskPoint);
        return safetyRiskPointMapper.insertSafetyRiskPoint(safetyRiskPoint);
    }

    @Override
    public int updateSafetyRiskPoint(SafetyRiskPoint safetyRiskPoint)
    {
        calcRiskLevel(safetyRiskPoint);
        return safetyRiskPointMapper.updateSafetyRiskPoint(safetyRiskPoint);
    }

    @Override
    public int deleteSafetyRiskPointByIds(Long[] riskPointIds)
    {
        checkRiskPointDeletable(riskPointIds);
        return safetyRiskPointMapper.deleteSafetyRiskPointByIds(riskPointIds);
    }

    @Override
    public int deleteSafetyRiskPointById(Long riskPointId)
    {
        checkRiskPointDeletable(new Long[]{ riskPointId });
        return safetyRiskPointMapper.deleteSafetyRiskPointById(riskPointId);
    }

    /** 删除前校验风险点是否被隐患/排查任务引用 */
    private void checkRiskPointDeletable(Long[] riskPointIds)
    {
        for (Long riskPointId : riskPointIds)
        {
            if (safetyRiskPointMapper.countHazardByRiskPointId(riskPointId) > 0)
            {
                throw new ServiceException("删除失败，该风险点存在关联隐患，请先处理关联隐患");
            }
            if (safetyRiskPointMapper.countTaskByRiskPointId(riskPointId) > 0)
            {
                throw new ServiceException("删除失败，该风险点存在关联排查任务，请先处理关联任务");
            }
        }
    }

    @Override
    public List<Map<String, Object>> selectFourColorMapData(Long enterpriseId)
    {
        return safetyRiskPointMapper.selectFourColorMapData(enterpriseId);
    }

    @Override
    public int countByRiskLevel(String riskLevel)
    {
        return safetyRiskPointMapper.countByRiskLevel(riskLevel);
    }

    /**
     * LEC 法计算风险等级
     * D = L * E * C
     * D >= 320: 重大风险(红) → riskLevel = 1
     * 160 <= D < 320: 较大风险(橙) → riskLevel = 2
     * 70 <= D < 160: 一般风险(黄) → riskLevel = 3
     * D < 70: 低风险(蓝) → riskLevel = 4
     */
    private void calcRiskLevel(SafetyRiskPoint rp)
    {
        if (rp.getLValue() != null && rp.getEValue() != null && rp.getCValue() != null)
        {
            BigDecimal d = rp.getLValue().multiply(rp.getEValue()).multiply(rp.getCValue());
            rp.setDValue(d);
            int dInt = d.intValue();
            if (dInt >= 320)
            {
                rp.setRiskLevel("1");
            }
            else if (dInt >= 160)
            {
                rp.setRiskLevel("2");
            }
            else if (dInt >= 70)
            {
                rp.setRiskLevel("3");
            }
            else
            {
                rp.setRiskLevel("4");
            }
            // 自动计算评价日期和复评日期
            calcEvaluateDates(rp);
        }
    }

    /**
     * 自动计算评价日期和复评日期
     * 规则（规格书 §5.1.1、§9.5）：
     * - 评价日期为空时默认填当天
     * - 红橙级（riskLevel=1/2）每年复评 → 评价日期 + 1 年
     * - 黄蓝级（riskLevel=3/4）每 3 年复评 → 评价日期 + 3 年
     */
    private void calcEvaluateDates(SafetyRiskPoint rp)
    {
        // 评价日期为空则默认为当天
        if (StringUtils.isEmpty(rp.getEvaluateDate()))
        {
            rp.setEvaluateDate(DateUtils.dateTimeNow("yyyy-MM-dd"));
        }
        // 根据风险等级计算复评日期
        if (StringUtils.isNotEmpty(rp.getEvaluateDate()))
        {
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date evalDate = sdf.parse(rp.getEvaluateDate());
                Calendar cal = Calendar.getInstance();
                cal.setTime(evalDate);
                String riskLevel = rp.getRiskLevel();
                if ("1".equals(riskLevel) || "2".equals(riskLevel))
                {
                    cal.add(Calendar.YEAR, 1);
                }
                else
                {
                    cal.add(Calendar.YEAR, 3);
                }
                rp.setReEvaluateDate(sdf.format(cal.getTime()));
            }
            catch (Exception e)
            {
                // 日期解析失败，不自动计算复评日期
            }
        }
    }
}
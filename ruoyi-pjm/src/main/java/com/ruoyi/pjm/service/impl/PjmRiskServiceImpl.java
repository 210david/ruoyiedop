package com.ruoyi.pjm.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.pjm.domain.PjmRisk;
import com.ruoyi.pjm.mapper.PjmRiskMapper;
import com.ruoyi.pjm.service.IPjmRiskService;

/**
 * 项目风险/问题 Service 实现
 */
@Service
public class PjmRiskServiceImpl implements IPjmRiskService
{
    @Autowired
    private PjmRiskMapper riskMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public PjmRisk selectRiskById(Long riskId)
    {
        return riskMapper.selectRiskById(riskId);
    }

    @Override
    public List<PjmRisk> selectRiskList(PjmRisk risk)
    {
        return riskMapper.selectRiskList(risk);
    }

    @Override
    @Transactional
    public int insertRisk(PjmRisk risk)
    {
        if (risk.getStatus() == null)
        {
            risk.setStatus("0");
        }
        risk.setDelFlag("0");
        risk.setCreateTime(new Date());
        risk.setCreateBy(SecurityUtils.getUsername());
        return riskMapper.insertRisk(risk);
    }

    @Override
    @Transactional
    public int updateRisk(PjmRisk risk)
    {
        risk.setUpdateTime(new Date());
        risk.setUpdateBy(SecurityUtils.getUsername());
        // 状态变为已解决时自动填解决日期
        if ("2".equals(risk.getStatus()) && risk.getResolveDate() == null)
        {
            risk.setResolveDate(new Date());
        }
        return riskMapper.updateRisk(risk);
    }

    @Override
    @Transactional
    public int deleteRiskByIds(Long[] riskIds)
    {
        return riskMapper.deleteRiskByIds(riskIds);
    }

    /** ★ 按工程类别从国家标准风险点库返回推荐风险点列表 */
    @Override
    public List<Map<String, Object>> matchStandardRisks(String engineeringCategory, String keyword)
    {
        if (engineeringCategory == null || engineeringCategory.isEmpty())
        {
            return new ArrayList<>();
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT risk_point_id, risk_code, risk_name, area_name, gb_std, risk_level, control_level, ");
        sql.append("       hazard_desc, accident_type, measure_engine, measure_manage, measure_training, measure_ppe, measure_emergency ");
        sql.append("FROM biz_safety_risk_point WHERE del_flag='0' AND engineering_category = ? ");
        List<Object> args = new ArrayList<>();
        args.add(engineeringCategory);
        if (keyword != null && !keyword.isEmpty())
        {
            sql.append("AND (risk_name LIKE ? OR risk_code LIKE ? OR gb_std LIKE ?) ");
            String like = "%" + keyword + "%";
            args.add(like); args.add(like); args.add(like);
        }
        sql.append("ORDER BY risk_level DESC, risk_point_id DESC LIMIT 100");
        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }
}

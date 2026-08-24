package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsOutsource;
import com.ruoyi.mms.mapper.MmsOutsourceMapper;
import com.ruoyi.mms.service.IMmsOutsourceService;

@Service
public class MmsOutsourceServiceImpl implements IMmsOutsourceService
{
    @Autowired
    private MmsOutsourceMapper outsourceMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsOutsource> selectOutsourceList(MmsOutsource outsource)
    {
        return outsourceMapper.selectOutsourceList(outsource);
    }

    @Override
    public MmsOutsource selectOutsourceById(Long outsourceId)
    {
        return outsourceMapper.selectOutsourceById(outsourceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertOutsource(MmsOutsource outsource)
    {
        if (StringUtils.isEmpty(outsource.getStatus()))
        {
            outsource.setStatus("0");
        }
        outsource.setDelFlag("0");
        if (StringUtils.isEmpty(outsource.getOutsourceNo()))
        {
            outsource.setOutsourceNo(mkNumberRuleService.generateNumber("mms_outsource"));
        }
        if (outsource.getOutQty() != null && outsource.getUnitPrice() != null)
        {
            outsource.setTotalAmount(outsource.getOutQty().multiply(outsource.getUnitPrice()));
        }
        if (outsource.getBackQty() == null) outsource.setBackQty(BigDecimal.ZERO);
        if (outsource.getLossQty() == null) outsource.setLossQty(BigDecimal.ZERO);
        outsource.setCreateBy(SecurityUtils.getUsername());
        outsource.setCreateTime(DateUtils.getNowDate());
        return outsourceMapper.insertOutsource(outsource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOutsource(MmsOutsource outsource)
    {
        if (outsource.getOutQty() != null && outsource.getUnitPrice() != null)
        {
            outsource.setTotalAmount(outsource.getOutQty().multiply(outsource.getUnitPrice()));
        }
        outsource.setUpdateBy(SecurityUtils.getUsername());
        return outsourceMapper.updateOutsource(outsource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOutsourceByIds(Long[] outsourceIds)
    {
        return outsourceMapper.deleteOutsourceByIds(outsourceIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sendOut(Long outsourceId, MmsOutsource outsource)
    {
        MmsOutsource existing = outsourceMapper.selectOutsourceById(outsourceId);
        if (existing == null)
        {
            throw new RuntimeException("外协单不存在");
        }
        if (!"0".equals(existing.getStatus()))
        {
            throw new RuntimeException("只有待外协状态的外协单才能发料");
        }
        existing.setStatus("1");
        if (outsource.getOutDate() != null) existing.setOutDate(outsource.getOutDate());
        if (outsource.getPlanBackDate() != null) existing.setPlanBackDate(outsource.getPlanBackDate());
        existing.setUpdateBy(SecurityUtils.getUsername());
        return outsourceMapper.updateOutsource(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int backAccept(Long outsourceId, MmsOutsource outsource)
    {
        MmsOutsource existing = outsourceMapper.selectOutsourceById(outsourceId);
        if (existing == null)
        {
            throw new RuntimeException("外协单不存在");
        }
        if (!"1".equals(existing.getStatus()))
        {
            throw new RuntimeException("只有外协中的外协单才能回厂验收");
        }
        existing.setStatus("2");
        if (outsource.getActualBackDate() != null) existing.setActualBackDate(outsource.getActualBackDate());
        if (outsource.getBackQty() != null) existing.setBackQty(outsource.getBackQty());
        // 自动计算损耗 = 外协数量 - 回厂数量，不允许负数（回厂数量不应超过外协数量）
        if (existing.getOutQty() != null && existing.getBackQty() != null)
        {
            java.math.BigDecimal loss = existing.getOutQty().subtract(existing.getBackQty());
            if (loss.compareTo(java.math.BigDecimal.ZERO) < 0)
            {
                loss = java.math.BigDecimal.ZERO;
            }
            existing.setLossQty(loss);
        }
        existing.setUpdateBy(SecurityUtils.getUsername());
        return outsourceMapper.updateOutsource(existing);
    }
}

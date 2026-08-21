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
            outsource.setOutsourceNo(mkNumberRuleService.generateNumber("OUT"));
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
}

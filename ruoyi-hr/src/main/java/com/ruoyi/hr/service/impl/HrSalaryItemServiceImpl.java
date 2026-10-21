package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrSalaryItem;
import com.ruoyi.hr.mapper.HrSalaryItemMapper;
import com.ruoyi.hr.service.IHrSalaryItemService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class HrSalaryItemServiceImpl implements IHrSalaryItemService
{
    @Autowired
    private HrSalaryItemMapper hrSalaryItemMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrSalaryItem selectHrSalaryItemById(Long itemId) { return hrSalaryItemMapper.selectHrSalaryItemById(itemId); }

    @Override
    public List<HrSalaryItem> selectHrSalaryItemList(HrSalaryItem hrSalaryItem) { return hrSalaryItemMapper.selectHrSalaryItemList(hrSalaryItem); }

    @Override
    public int insertHrSalaryItem(HrSalaryItem hrSalaryItem)
    {
        if (StringUtils.isEmpty(hrSalaryItem.getItemCode()))
        {
            hrSalaryItem.setItemCode(mkNumberRuleService.generateNumber("hr_salary_item"));
        }
        return hrSalaryItemMapper.insertHrSalaryItem(hrSalaryItem);
    }

    @Override
    public int updateHrSalaryItem(HrSalaryItem hrSalaryItem) { return hrSalaryItemMapper.updateHrSalaryItem(hrSalaryItem); }

    @Override
    public int deleteHrSalaryItemByIds(Long[] itemIds) { return hrSalaryItemMapper.deleteHrSalaryItemByIds(itemIds); }

    @Override
    public int deleteHrSalaryItemById(Long itemId) { return hrSalaryItemMapper.deleteHrSalaryItemById(itemId); }
}

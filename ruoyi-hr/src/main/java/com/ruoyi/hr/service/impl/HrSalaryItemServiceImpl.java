package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrSalaryItem;
import com.ruoyi.hr.mapper.HrSalaryItemMapper;
import com.ruoyi.hr.service.IHrSalaryItemService;

@Service
public class HrSalaryItemServiceImpl implements IHrSalaryItemService
{
    @Autowired
    private HrSalaryItemMapper hrSalaryItemMapper;

    @Override
    public HrSalaryItem selectHrSalaryItemById(Long itemId) { return hrSalaryItemMapper.selectHrSalaryItemById(itemId); }

    @Override
    public List<HrSalaryItem> selectHrSalaryItemList(HrSalaryItem hrSalaryItem) { return hrSalaryItemMapper.selectHrSalaryItemList(hrSalaryItem); }

    @Override
    public int insertHrSalaryItem(HrSalaryItem hrSalaryItem) { return hrSalaryItemMapper.insertHrSalaryItem(hrSalaryItem); }

    @Override
    public int updateHrSalaryItem(HrSalaryItem hrSalaryItem) { return hrSalaryItemMapper.updateHrSalaryItem(hrSalaryItem); }

    @Override
    public int deleteHrSalaryItemByIds(Long[] itemIds) { return hrSalaryItemMapper.deleteHrSalaryItemByIds(itemIds); }

    @Override
    public int deleteHrSalaryItemById(Long itemId) { return hrSalaryItemMapper.deleteHrSalaryItemById(itemId); }
}

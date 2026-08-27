package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrSalaryItem;

public interface HrSalaryItemMapper {
    public HrSalaryItem selectHrSalaryItemById(Long itemId);
    public List<HrSalaryItem> selectHrSalaryItemList(HrSalaryItem hrSalaryItem);
    public int insertHrSalaryItem(HrSalaryItem hrSalaryItem);
    public int updateHrSalaryItem(HrSalaryItem hrSalaryItem);
    public int deleteHrSalaryItemByIds(Long[] itemIds);
    public int deleteHrSalaryItemById(Long itemId);
}

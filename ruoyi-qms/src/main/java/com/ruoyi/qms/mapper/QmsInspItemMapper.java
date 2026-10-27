package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsInspItem;

/**
 * 检验单项实测 Mapper
 *
 * @author ruoyi
 */
public interface QmsInspItemMapper
{
    public List<QmsInspItem> selectInspItemListByTaskId(Long taskId);
    public int insertInspItem(QmsInspItem inspItem);
    public int batchInsertInspItem(List<QmsInspItem> itemList);
    public int deleteInspItemByTaskId(Long taskId);
}

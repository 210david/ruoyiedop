package com.ruoyi.wms.mapper;

import java.util.List;
import com.ruoyi.wms.domain.WmsMoveOrder;

public interface WmsMoveOrderMapper
{
    public List<WmsMoveOrder> selectMoveOrderList(WmsMoveOrder move);
    public WmsMoveOrder selectMoveOrderById(Long moveId);
    public int insertMoveOrder(WmsMoveOrder move);
    public int updateMoveOrder(WmsMoveOrder move);
    public int deleteMoveOrderByIds(Long[] moveIds);
}

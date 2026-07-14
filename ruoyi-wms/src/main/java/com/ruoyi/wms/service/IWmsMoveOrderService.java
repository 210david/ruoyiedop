package com.ruoyi.wms.service;

import java.util.List;
import com.ruoyi.wms.domain.WmsMoveOrder;

public interface IWmsMoveOrderService
{
    public List<WmsMoveOrder> selectMoveOrderList(WmsMoveOrder move);
    public WmsMoveOrder selectMoveOrderById(Long moveId);
    public int insertMoveOrder(WmsMoveOrder move);
    public int updateMoveOrder(WmsMoveOrder move);
    public int deleteMoveOrderByIds(Long[] moveIds);
    public int approveMove(Long moveId);
    public int executeMove(Long moveId);
    public int voidMove(Long moveId);
}

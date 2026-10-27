package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsReceive;
import com.ruoyi.pms.domain.PmsReceiveDetail;

/**
 * 收货验收 Mapper
 *
 * @author ruoyi
 */
public interface PmsReceiveMapper
{
    public List<PmsReceive> selectReceiveList(PmsReceive receive);
    public PmsReceive selectReceiveById(Long receiveId);
    public int insertReceive(PmsReceive receive);
    public int updateReceive(PmsReceive receive);
    public int deleteReceiveByIds(Long[] receiveIds);
    public List<PmsReceiveDetail> selectReceiveDetailByReceiveId(Long receiveId);
    public int insertReceiveDetail(PmsReceiveDetail detail);
    public int updateReceiveDetail(PmsReceiveDetail detail);
    public int deleteReceiveDetailByReceiveId(Long receiveId);

    /**
     * 查询已有有效收货记录的采购订单ID列表
     * 有效 = 未删除(del_flag='0') 且 未作废(status != '4')
     */
    public List<Long> selectReceivedOrderIds();

    /**
     * 查询存在进行中收货单的采购订单ID列表
     * 进行中 = 状态为待验收(0)、待审核(5)、已驳回(6)
     */
    public List<Long> selectInProgressReceiveOrderIds();
}

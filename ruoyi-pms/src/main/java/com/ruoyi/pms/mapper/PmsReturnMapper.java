package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsReturn;
import com.ruoyi.pms.domain.PmsReturnDetail;

/**
 * 退货 Mapper
 *
 * @author ruoyi
 */
public interface PmsReturnMapper
{
    public List<PmsReturn> selectReturnList(PmsReturn pmsReturn);
    public PmsReturn selectReturnById(Long returnId);
    public int insertReturn(PmsReturn pmsReturn);
    public int updateReturn(PmsReturn pmsReturn);
    public int deleteReturnByIds(Long[] returnIds);
    public List<PmsReturnDetail> selectReturnDetailByReturnId(Long returnId);
    public int insertReturnDetail(PmsReturnDetail detail);
    public int deleteReturnDetailByReturnId(Long returnId);

    /**
     * 查询存在进行中退货单的采购订单ID列表
     * 进行中 = 状态为草稿(0)、待审批(1)、已审批(2)、已驳回(5)
     * 用于前端过滤，避免同一订单同时存在多个进行中的退货单
     */
    public List<Long> selectInProgressReturnOrderIds();
}

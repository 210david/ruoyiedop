package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkOrderReturnApproveLog;

/**
 * 订单退货审批记录 Mapper
 *
 * @author ruoyi
 */
public interface MkOrderReturnApproveLogMapper
{
    /** 根据退货ID查询审批记录 */
    public List<MkOrderReturnApproveLog> selectApproveLogByReturnId(Long returnId);

    /** 新增审批记录 */
    public int insertApproveLog(MkOrderReturnApproveLog log);

    /** 根据退货ID删除审批记录 */
    public int deleteApproveLogByReturnId(Long returnId);
}

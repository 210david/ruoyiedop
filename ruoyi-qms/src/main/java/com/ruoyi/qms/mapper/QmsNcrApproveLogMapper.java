package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsNcrApproveLog;

/**
 * NCR审批记录 Mapper接口
 *
 * @author ruoyi
 */
public interface QmsNcrApproveLogMapper
{
    public List<QmsNcrApproveLog> selectApproveLogByNcrId(Long ncrId);

    public int insertApproveLog(QmsNcrApproveLog approveLog);

    public int deleteApproveLogByNcrId(Long ncrId);
}

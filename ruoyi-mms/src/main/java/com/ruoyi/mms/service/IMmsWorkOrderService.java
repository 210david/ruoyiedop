package com.ruoyi.mms.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.domain.MmsWorkOrderAuditLog;

/**
 * 生产工单 Service接口
 *
 * @author ruoyi
 */
public interface IMmsWorkOrderService
{
    /** 标准CRUD */
    public List<MmsWorkOrder> selectWorkOrderList(MmsWorkOrder workOrder);
    public MmsWorkOrder selectWorkOrderById(Long workOrderId);
    public int insertWorkOrder(MmsWorkOrder workOrder);
    public int updateWorkOrder(MmsWorkOrder workOrder);
    public int deleteWorkOrderByIds(Long[] workOrderIds);

    /** 审核日志 */
    public List<MmsWorkOrderAuditLog> selectAuditLogByWorkOrderId(Long workOrderId);

    /** 工单下达：0(新建) → 1(已下达) */
    public int releaseWorkOrder(Long workOrderId);

    /** 工单暂停：1(已下达)/2(执行中) → 7(已暂停) */
    public int pauseWorkOrder(Long workOrderId, String pauseReason);

    /** 工单恢复：7(已暂停) → 1(已下达) */
    public int resumeWorkOrder(Long workOrderId);

    /** 工单完工：2(执行中)/3(报工中) → 4(待完工质检) */
    public int finishWorkOrder(Long workOrderId);

    /** 工单关闭：4(待完工质检)/5(完工入库) → 6(已关闭) */
    public int closeWorkOrder(Long workOrderId, String closeRemark);

    /** 工单作废：任意非关闭/作废状态 → 8(已作废) */
    public int cancelWorkOrder(Long workOrderId, String cancelReason);

    /** 工单拆分：返回拆分后的新工单ID */
    public Long splitWorkOrder(Long workOrderId, BigDecimal splitQty);
}

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

    /** 工单下达预览：返回BOM明细+工艺工序，供前端弹窗展示 */
    public com.ruoyi.common.core.domain.AjaxResult getReleasePreview(Long workOrderId);

    /** 工单暂停：1(已下达)/2(执行中) → 5(已暂停) */
    public int pauseWorkOrder(Long workOrderId, String pauseReason);

    /** 工单恢复：5(已暂停) → 1(已下达) */
    public int resumeWorkOrder(Long workOrderId);

    /** 工单完工：2(执行中) → 3(已完工)，同时自动生成完工质检单（独立业务） */
    public int finishWorkOrder(Long workOrderId);

    /** 工单关闭：3(已完工) → 4(已关闭)，或2(执行中)强制关闭 */
    public int closeWorkOrder(Long workOrderId, String closeRemark);

    /** 工单作废：任意非关闭/作废状态 → 6(已作废) */
    public int cancelWorkOrder(Long workOrderId, String cancelReason);

    /** 工单拆分：返回拆分后的新工单ID */
    public Long splitWorkOrder(Long workOrderId, BigDecimal splitQty);

    /**
     * 创建返工工单：基于原工单的不良品数量创建返工工单（order_type=1）
     * @param sourceWorkOrderId 原工单ID
     * @param reworkQty 返工数量（不超过原工单不良数）
     * @param reworkReason 返工原因
     * @return 新工单ID
     */
    public Long createReworkOrder(Long sourceWorkOrderId, BigDecimal reworkQty, String reworkReason);
}

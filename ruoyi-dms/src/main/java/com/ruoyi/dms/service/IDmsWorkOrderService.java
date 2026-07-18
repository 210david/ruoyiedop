package com.ruoyi.dms.service;

import java.util.List;
import com.ruoyi.dms.domain.DmsWorkOrder;

public interface IDmsWorkOrderService
{
    public List<DmsWorkOrder> selectWorkOrderList(DmsWorkOrder workOrder);
    public DmsWorkOrder selectWorkOrderById(Long orderId);
    public int insertWorkOrder(DmsWorkOrder workOrder);
    public int updateWorkOrder(DmsWorkOrder workOrder);
    public int deleteWorkOrderByIds(Long[] orderIds);

    /** 派工：0新建→1已派工 */
    public int dispatch(DmsWorkOrder workOrder);
    /** 改派：1已派工→1已派工（换人） */
    public int reassign(DmsWorkOrder workOrder);
    /** 接单：1已派工→2已接单 */
    public int accept(Long orderId);
    /** 开始处理：2已接单→3处理中 */
    public int startProcess(Long orderId);
    /** 完工：3处理中→4已完成 */
    public int complete(DmsWorkOrder workOrder);
    /** 验收通过：4已完成→5已验收 */
    public int verify(DmsWorkOrder workOrder);
    /** 驳回重做：4已完成→7驳回重做→3处理中 */
    public int reject(DmsWorkOrder workOrder);
    /** 撤销：0/1→6已撤销 */
    public int cancel(Long orderId);

    /** SLA超时检查：扫描超时工单，更新状态并发送通知 */
    public int checkSlaTimeout();
}

package com.ruoyi.dms.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.dms.domain.DmsSparePart;
import com.ruoyi.dms.domain.DmsSparePartRecord;
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.mapper.DmsWorkOrderMapper;
import com.ruoyi.dms.service.IDmsSparePartService;
import com.ruoyi.dms.service.IDmsWorkOrderLogService;
import com.ruoyi.dms.service.IDmsWorkOrderService;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;

@Service
public class DmsWorkOrderServiceImpl implements IDmsWorkOrderService
{
    @Autowired
    private DmsWorkOrderMapper dmsWorkOrderMapper;

    @Autowired
    private IDmsSparePartService dmsSparePartService;

    @Autowired
    private IDmsWorkOrderLogService workOrderLogService;

    @Autowired
    private ISysNoticeService sysNoticeService;

    /** SLA时效配置：优先级 → [响应分钟, 处理分钟] */
    private static final java.util.Map<String, int[]> SLA_CONFIG = new java.util.HashMap<>();
    static
    {
        SLA_CONFIG.put("0", new int[]{15, 4 * 60});   // 紧急：15分钟响应，4小时处理
        SLA_CONFIG.put("1", new int[]{60, 8 * 60});   // 高：1小时响应，8小时处理
        SLA_CONFIG.put("2", new int[]{4 * 60, 24 * 60}); // 中：4小时响应，24小时处理
        SLA_CONFIG.put("3", new int[]{24 * 60, 72 * 60}); // 低：24小时响应，72小时处理
    }

    @Override
    public List<DmsWorkOrder> selectWorkOrderList(DmsWorkOrder workOrder)
    {
        return dmsWorkOrderMapper.selectWorkOrderList(workOrder);
    }

    @Override
    public DmsWorkOrder selectWorkOrderById(Long orderId)
    {
        return dmsWorkOrderMapper.selectWorkOrderById(orderId);
    }

    @Override
    @Transactional
    public int insertWorkOrder(DmsWorkOrder workOrder)
    {
        workOrder.setDelFlag("0");
        if (workOrder.getOrderNo() == null || workOrder.getOrderNo().isEmpty())
        {
            workOrder.setOrderNo(generateOrderNo());
        }
        if (workOrder.getOrderStatus() == null || workOrder.getOrderStatus().isEmpty())
        {
            workOrder.setOrderStatus("0");
        }
        if (workOrder.getReportTime() == null)
        {
            workOrder.setReportTime(new Date());
        }
        workOrder.setSlaTimeoutStatus("0");
        workOrder.setSlaEscalated("0");

        // 根据优先级计算SLA截止时间
        calculateSlaDeadlines(workOrder);

        int rows = dmsWorkOrderMapper.insertWorkOrder(workOrder);

        // 记录操作日志
        workOrderLogService.recordLog(workOrder.getOrderId(), workOrder.getOrderNo(),
                null, "0", "create", getCurrentUsername(), "工单创建");

        // 发送通知：新工单生成 → 通知维修组/维修员
        sendWorkOrderNotice(workOrder, "新工单通知",
                "新工单「" + workOrder.getOrderNo() + "」已创建"
                + (workOrder.getEquipmentName() != null ? "，设备：" + workOrder.getEquipmentName() : "")
                + "，优先级：" + priorityText(workOrder.getPriority())
                + "，请尽快派工处理。");

        return rows;
    }

    @Override
    public int updateWorkOrder(DmsWorkOrder workOrder)
    {
        return dmsWorkOrderMapper.updateWorkOrder(workOrder);
    }

    @Override
    public int deleteWorkOrderByIds(Long[] orderIds)
    {
        return dmsWorkOrderMapper.deleteWorkOrderByIds(orderIds);
    }

    @Override
    @Transactional
    public int dispatch(DmsWorkOrder workOrder)
    {
        DmsWorkOrder db = dmsWorkOrderMapper.selectWorkOrderById(workOrder.getOrderId());
        if (db == null) throw new ServiceException("工单不存在");
        if (!"0".equals(db.getOrderStatus())) throw new ServiceException("当前状态不允许派工");
        workOrder.setOrderStatus("1");
        workOrder.setAssignTime(new Date());
        int rows = dmsWorkOrderMapper.updateWorkOrder(workOrder);

        // 记录操作日志
        workOrderLogService.recordLog(workOrder.getOrderId(), db.getOrderNo(),
                "0", "1", "dispatch", getCurrentUsername(),
                "派工给：" + workOrder.getAssigneeName());

        // 通知维修员
        sendWorkOrderNotice(workOrder, "工单派工通知",
                "工单「" + db.getOrderNo() + "」已派工给您"
                + (db.getEquipmentName() != null ? "，设备：" + db.getEquipmentName() : "")
                + "，请及时接单处理。");

        return rows;
    }

    @Override
    @Transactional
    public int reassign(DmsWorkOrder workOrder)
    {
        DmsWorkOrder db = dmsWorkOrderMapper.selectWorkOrderById(workOrder.getOrderId());
        if (db == null) throw new ServiceException("工单不存在");
        if (!"1".equals(db.getOrderStatus())) throw new ServiceException("当前状态不允许改派");
        workOrder.setOrderStatus("1");
        workOrder.setAssignTime(new Date());
        int rows = dmsWorkOrderMapper.updateWorkOrder(workOrder);

        workOrderLogService.recordLog(workOrder.getOrderId(), db.getOrderNo(),
                "1", "1", "reassign", getCurrentUsername(),
                "改派给：" + workOrder.getAssigneeName() + "（原维修人：" + db.getAssigneeName() + "）");

        sendWorkOrderNotice(workOrder, "工单改派通知",
                "工单「" + db.getOrderNo() + "」已改派给您"
                + (db.getEquipmentName() != null ? "，设备：" + db.getEquipmentName() : "")
                + "，请及时接单处理。");

        return rows;
    }

    @Override
    @Transactional
    public int accept(Long orderId)
    {
        DmsWorkOrder db = dmsWorkOrderMapper.selectWorkOrderById(orderId);
        if (db == null) throw new ServiceException("工单不存在");
        if (!"1".equals(db.getOrderStatus())) throw new ServiceException("当前状态不允许接单");
        DmsWorkOrder update = new DmsWorkOrder();
        update.setOrderId(orderId);
        update.setOrderStatus("2");
        update.setArriveTime(new Date());
        int rows = dmsWorkOrderMapper.updateWorkOrder(update);

        workOrderLogService.recordLog(orderId, db.getOrderNo(),
                "1", "2", "accept", getCurrentUsername(), "接单");

        // 通知报修人
        sendWorkOrderNotice(db, "报修确认通知",
                "您的报修工单「" + db.getOrderNo() + "」已被维修员「" + db.getAssigneeName() + "」接单，正在处理中。");

        return rows;
    }

    @Override
    @Transactional
    public int startProcess(Long orderId)
    {
        DmsWorkOrder db = dmsWorkOrderMapper.selectWorkOrderById(orderId);
        if (db == null) throw new ServiceException("工单不存在");
        if (!"2".equals(db.getOrderStatus()) && !"7".equals(db.getOrderStatus())) throw new ServiceException("当前状态不允许开始处理");
        DmsWorkOrder update = new DmsWorkOrder();
        update.setOrderId(orderId);
        update.setOrderStatus("3");
        int rows = dmsWorkOrderMapper.updateWorkOrder(update);

        workOrderLogService.recordLog(orderId, db.getOrderNo(),
                db.getOrderStatus(), "3", "process", getCurrentUsername(), "开始处理");

        return rows;
    }

    @Override
    @Transactional
    public int complete(DmsWorkOrder workOrder)
    {
        DmsWorkOrder db = dmsWorkOrderMapper.selectWorkOrderById(workOrder.getOrderId());
        if (db == null) throw new ServiceException("工单不存在");
        if (!"3".equals(db.getOrderStatus())) throw new ServiceException("当前状态不允许完工");
        workOrder.setOrderStatus("4");
        workOrder.setCompleteTime(new Date());
        int rows = dmsWorkOrderMapper.updateWorkOrder(workOrder);

        workOrderLogService.recordLog(workOrder.getOrderId(), db.getOrderNo(),
                "3", "4", "complete", getCurrentUsername(),
                "完工" + (workOrder.getCompleteRemark() != null ? "：" + workOrder.getCompleteRemark() : ""));

        return rows;
    }

    @Override
    @Transactional
    public int verify(DmsWorkOrder workOrder)
    {
        DmsWorkOrder db = dmsWorkOrderMapper.selectWorkOrderById(workOrder.getOrderId());
        if (db == null) throw new ServiceException("工单不存在");
        if (!"4".equals(db.getOrderStatus())) throw new ServiceException("当前状态不允许验收");
        workOrder.setOrderStatus("5");
        workOrder.setVerifyTime(new Date());
        int rows = dmsWorkOrderMapper.updateWorkOrder(workOrder);

        workOrderLogService.recordLog(workOrder.getOrderId(), db.getOrderNo(),
                "4", "5", "verify", getCurrentUsername(),
                "验收通过" + (workOrder.getRating() != null ? "，评分：" + workOrder.getRating() + "星" : ""));

        // 验收通过后，自动创建备件出库单并扣减库存
        String sparePartsUsed = db.getSparePartsUsed();
        if (sparePartsUsed != null && !sparePartsUsed.trim().isEmpty())
        {
            createSparePartOutbound(db);
        }

        return rows;
    }

    @Override
    @Transactional
    public int reject(DmsWorkOrder workOrder)
    {
        DmsWorkOrder db = dmsWorkOrderMapper.selectWorkOrderById(workOrder.getOrderId());
        if (db == null) throw new ServiceException("工单不存在");
        if (!"4".equals(db.getOrderStatus())) throw new ServiceException("当前状态不允许驳回");
        workOrder.setOrderStatus("7");
        workOrder.setVerifyTime(new Date());
        int rows = dmsWorkOrderMapper.updateWorkOrder(workOrder);

        workOrderLogService.recordLog(workOrder.getOrderId(), db.getOrderNo(),
                "4", "7", "reject", getCurrentUsername(),
                "驳回：" + (workOrder.getVerifyOpinion() != null ? workOrder.getVerifyOpinion() : ""));

        // 通知维修人重做
        sendWorkOrderNotice(db, "工单驳回通知",
                "工单「" + db.getOrderNo() + "」验收未通过，请重新处理。"
                + (workOrder.getVerifyOpinion() != null ? "驳回原因：" + workOrder.getVerifyOpinion() : ""));

        return rows;
    }

    @Override
    @Transactional
    public int cancel(Long orderId)
    {
        DmsWorkOrder db = dmsWorkOrderMapper.selectWorkOrderById(orderId);
        if (db == null) throw new ServiceException("工单不存在");
        if (!"0".equals(db.getOrderStatus()) && !"1".equals(db.getOrderStatus())) throw new ServiceException("当前状态不允许撤销");
        DmsWorkOrder update = new DmsWorkOrder();
        update.setOrderId(orderId);
        update.setOrderStatus("6");
        int rows = dmsWorkOrderMapper.updateWorkOrder(update);

        workOrderLogService.recordLog(orderId, db.getOrderNo(),
                db.getOrderStatus(), "6", "cancel", getCurrentUsername(), "撤销");

        return rows;
    }

    @Override
    public int checkSlaTimeout()
    {
        List<DmsWorkOrder> timeoutOrders = dmsWorkOrderMapper.selectSlaTimeoutOrders();
        int count = 0;
        for (DmsWorkOrder order : timeoutOrders)
        {
            try
            {
                String timeoutType = "0".equals(order.getOrderStatus()) ? "1" : "2";
                DmsWorkOrder update = new DmsWorkOrder();
                update.setOrderId(order.getOrderId());
                update.setSlaTimeoutStatus(timeoutType);
                update.setSlaEscalated("1"); // 标记已升级，避免重复通知
                dmsWorkOrderMapper.updateWorkOrder(update);

                // 发送超时通知
                String timeoutDesc = "1".equals(timeoutType) ? "响应超时" : "处理超时";
                sendWorkOrderNotice(order, "工单SLA超时升级通知",
                        "工单「" + order.getOrderNo() + "」" + timeoutDesc + "！"
                        + (order.getEquipmentName() != null ? "，设备：" + order.getEquipmentName() : "")
                        + "，优先级：" + priorityText(order.getPriority())
                        + "，请立即处理。");
                count++;
            }
            catch (Exception e)
            {
                // 单个工单失败不影响其他
            }
        }
        return count;
    }

    /**
     * 根据优先级计算SLA响应和处理截止时间
     */
    private void calculateSlaDeadlines(DmsWorkOrder workOrder)
    {
        String priority = workOrder.getPriority();
        if (priority == null) priority = "2"; // 默认中
        int[] sla = SLA_CONFIG.get(priority);
        if (sla == null) sla = SLA_CONFIG.get("2");

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        // 响应截止 = 报修时间 + 响应时效
        Calendar respCal = Calendar.getInstance();
        respCal.setTime(workOrder.getReportTime() != null ? workOrder.getReportTime() : now);
        respCal.add(Calendar.MINUTE, sla[0]);
        workOrder.setSlaResponseDeadline(respCal.getTime());

        // 处理截止 = 报修时间 + 处理时效
        Calendar procCal = Calendar.getInstance();
        procCal.setTime(workOrder.getReportTime() != null ? workOrder.getReportTime() : now);
        procCal.add(Calendar.MINUTE, sla[1]);
        workOrder.setSlaProcessDeadline(procCal.getTime());
    }

    /**
     * 发送工单通知（系统公告）
     */
    private void sendWorkOrderNotice(DmsWorkOrder order, String title, String message)
    {
        try
        {
            SysNotice notice = new SysNotice();
            notice.setNoticeTitle(title);
            notice.setNoticeType("1"); // 通知
            notice.setNoticeContent(message);
            notice.setStatus("0");
            notice.setCreateBy("system");
            sysNoticeService.insertNotice(notice);
        }
        catch (Exception e)
        {
            // 通知发送失败不影响主流程
        }
    }

    /**
     * 工单号生成（synchronized 保证并发安全）
     */
    private synchronized String generateOrderNo()
    {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String maxNo = dmsWorkOrderMapper.selectMaxOrderNo();
        int seq = 1;
        if (maxNo != null && maxNo.length() > 10 && maxNo.contains(dateStr))
        {
            try { seq = Integer.parseInt(maxNo.substring(10)) + 1; } catch (Exception e) { seq = 1; }
        }
        return "WO" + dateStr + String.format("%04d", seq);
    }

    private String getCurrentUsername()
    {
        try { return SecurityUtils.getUsername(); }
        catch (Exception e) { return "system"; }
    }

    private String priorityText(String priority)
    {
        if (priority == null) return "中";
        switch (priority)
        {
            case "0": return "紧急";
            case "1": return "高";
            case "2": return "中";
            case "3": return "低";
            default: return priority;
        }
    }

    /**
     * 验收通过后，根据工单更换备件记录自动创建备件出库单并扣减库存
     * sparePartsUsed 格式: "轴承×2，密封圈×5"
     */
    private void createSparePartOutbound(DmsWorkOrder order)
    {
        String sparePartsUsed = order.getSparePartsUsed();
        String orderNo = order.getOrderNo();
        Long orderId = order.getOrderId();
        String receiver = order.getAssigneeName();

        String documentCode = dmsSparePartService.generateOutCode();

        String[] parts = sparePartsUsed.split("[，,]");
        for (String partStr : parts)
        {
            String trimmed = partStr.trim();
            if (trimmed.isEmpty()) continue;

            String partName;
            BigDecimal quantity;

            int idx = -1;
            char[] separators = {'×', 'x', 'X', '*'};
            for (char sep : separators)
            {
                int pos = trimmed.indexOf(sep);
                if (pos > 0) { idx = pos; break; }
            }

            if (idx > 0)
            {
                partName = trimmed.substring(0, idx).trim();
                try { quantity = new BigDecimal(trimmed.substring(idx + 1).trim()); }
                catch (NumberFormatException e) { quantity = BigDecimal.ONE; }
            }
            else
            {
                partName = trimmed;
                quantity = BigDecimal.ONE;
            }

            if (partName.isEmpty() || quantity.compareTo(BigDecimal.ZERO) <= 0) continue;

            DmsSparePart query = new DmsSparePart();
            query.setPartName(partName);
            List<DmsSparePart> matched = dmsSparePartService.selectSparePartList(query);
            if (matched == null || matched.isEmpty())
            {
                throw new ServiceException("备件「" + partName + "」不存在，无法出库");
            }

            DmsSparePart part = matched.get(0);

            DmsSparePartRecord record = new DmsSparePartRecord();
            record.setPartId(part.getPartId());
            record.setDocumentCode(documentCode);
            record.setMoveType("1");
            record.setTargetType("1");
            record.setReceiver(receiver);
            record.setQuantity(quantity);
            record.setRefType("workorder");
            record.setRefId(orderId);
            record.setRefNo(orderNo);
            record.setRemark("工单「" + orderNo + "」验收自动出库");

            dmsSparePartService.stockMove(record);
        }
    }
}

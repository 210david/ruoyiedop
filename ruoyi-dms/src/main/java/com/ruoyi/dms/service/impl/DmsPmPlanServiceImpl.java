package com.ruoyi.dms.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.dms.domain.DmsEquipment;
import com.ruoyi.dms.domain.DmsPmPlan;
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.mapper.DmsEquipmentMapper;
import com.ruoyi.dms.mapper.DmsPmPlanMapper;
import com.ruoyi.dms.service.IDmsPmPlanService;
import com.ruoyi.dms.service.IDmsWorkOrderService;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;
import com.ruoyi.system.utils.MessageHelper;

@Service
public class DmsPmPlanServiceImpl implements IDmsPmPlanService
{
    @Autowired
    private DmsPmPlanMapper dmsPmPlanMapper;

    @Autowired
    private DmsEquipmentMapper dmsEquipmentMapper;

    @Autowired
    private IDmsWorkOrderService dmsWorkOrderService;

    @Autowired
    private ISysNoticeService sysNoticeService;

    @Autowired
    private MessageHelper messageHelper;

    @Override
    public List<DmsPmPlan> selectPmPlanList(DmsPmPlan pmPlan)
    {
        return dmsPmPlanMapper.selectPmPlanList(pmPlan);
    }

    @Override
    public DmsPmPlan selectPmPlanById(Long planId)
    {
        return dmsPmPlanMapper.selectPmPlanById(planId);
    }

    @Override
    public int insertPmPlan(DmsPmPlan pmPlan)
    {
        pmPlan.setDelFlag("0");
        calculateNextExecuteTime(pmPlan);
        return dmsPmPlanMapper.insertPmPlan(pmPlan);
    }

    @Override
    public int updatePmPlan(DmsPmPlan pmPlan)
    {
        calculateNextExecuteTime(pmPlan);
        return dmsPmPlanMapper.updatePmPlan(pmPlan);
    }

    @Override
    public int deletePmPlanByIds(Long[] planIds)
    {
        return dmsPmPlanMapper.deletePmPlanByIds(planIds);
    }

    @Override
    @Transactional
    public int generateWorkOrder(Long planId)
    {
        DmsPmPlan plan = dmsPmPlanMapper.selectPmPlanById(planId);
        if (plan == null) throw new ServiceException("PM计划不存在");
        if (!"0".equals(plan.getStatus())) throw new ServiceException("计划已停用，无法生成工单");

        // 检查是否存在未完结的由该PM计划生成的工单
        DmsWorkOrder query = new DmsWorkOrder();
        query.setOrderType("1"); // PM维护
        query.setRemark("PM计划ID:" + planId);
        List<DmsWorkOrder> existOrders = dmsWorkOrderService.selectWorkOrderList(query);
        for (DmsWorkOrder exist : existOrders)
        {
            String status = exist.getOrderStatus();
            if (status != null && !"5".equals(status) && !"6".equals(status))
            {
                String statusText = orderStatusText(status);
                throw new ServiceException("该计划已生成未完结的工单「" + exist.getOrderNo() + "」（当前状态：" + statusText + "），请先完成或撤销该工单后再生成新工单");
            }
        }

        DmsWorkOrder workOrder = createWorkOrderFromPlan(plan);

        dmsWorkOrderService.insertWorkOrder(workOrder);

        // 自动派工
        if ("1".equals(plan.getAutoAssign()) && plan.getAssigneeId() != null)
        {
            workOrder.setOrderStatus("1"); // 已派工
            workOrder.setAssigneeId(plan.getAssigneeId());
            workOrder.setAssigneeName(plan.getAssigneeName());
            workOrder.setAssignTime(new Date());
            dmsWorkOrderService.updateWorkOrder(workOrder);
        }

        // 更新PM计划执行时间
        plan.setLastExecuteTime(new Date());
        calculateNextExecuteTime(plan);
        dmsPmPlanMapper.updatePmPlan(plan);

        // 发送系统通知
        sendPmNotification(plan, workOrder, "PM计划「" + plan.getPlanName() + "」已生成工单「" + workOrder.getOrderNo() + "」");

        // 发送消息中心提醒：设备保养到期（DEF-03）
        sendPmDueMessage(plan, workOrder, "");

        return 1;
    }

    @Override
    @Transactional
    public int autoGeneratePmWorkOrders()
    {
        List<DmsPmPlan> expiredPlans = dmsPmPlanMapper.selectExpiredPmPlans();
        int count = 0;
        for (DmsPmPlan plan : expiredPlans)
        {
            try
            {
                // 检查是否存在未完结工单
                DmsWorkOrder query = new DmsWorkOrder();
                query.setOrderType("1");
                query.setRemark("PM计划ID:" + plan.getPlanId());
                List<DmsWorkOrder> existOrders = dmsWorkOrderService.selectWorkOrderList(query);
                boolean hasUnfinished = false;
                for (DmsWorkOrder exist : existOrders)
                {
                    String status = exist.getOrderStatus();
                    if (status != null && !"5".equals(status) && !"6".equals(status))
                    {
                        hasUnfinished = true;
                        break;
                    }
                }
                if (hasUnfinished) continue;

                DmsWorkOrder workOrder = createWorkOrderFromPlan(plan);
                dmsWorkOrderService.insertWorkOrder(workOrder);

                // 自动派工
                if ("1".equals(plan.getAutoAssign()) && plan.getAssigneeId() != null)
                {
                    workOrder.setOrderStatus("1");
                    workOrder.setAssigneeId(plan.getAssigneeId());
                    workOrder.setAssigneeName(plan.getAssigneeName());
                    workOrder.setAssignTime(new Date());
                    dmsWorkOrderService.updateWorkOrder(workOrder);
                }

                // 更新PM计划执行时间
                plan.setLastExecuteTime(new Date());
                calculateNextExecuteTime(plan);
                dmsPmPlanMapper.updatePmPlan(plan);

                // 发送通知
                sendPmNotification(plan, workOrder, "【定时任务】PM计划「" + plan.getPlanName() + "」已自动生成工单「" + workOrder.getOrderNo() + "」");

                // 发送消息中心提醒：设备保养到期（DEF-03）
                sendPmDueMessage(plan, workOrder, "【定时任务】");

                count++;
            }
            catch (Exception e)
            {
                // 单个计划失败不影响其他计划
                System.err.println("PM计划[" + plan.getPlanId() + "]自动生成工单失败: " + e.getMessage());
            }
        }
        return count;
    }

    @Override
    public List<Map<String, Object>> selectPmPlanCalendar(String month)
    {
        // 查询所有正常状态的PM计划
        DmsPmPlan query = new DmsPmPlan();
        query.setStatus("0");
        List<DmsPmPlan> allPlans = dmsPmPlanMapper.selectPmPlanList(query);

        List<Map<String, Object>> result = new ArrayList<>();
        for (DmsPmPlan plan : allPlans)
        {
            if (plan.getNextExecuteTime() != null)
            {
                Map<String, Object> item = new HashMap<>();
                item.put("planId", plan.getPlanId());
                item.put("planName", plan.getPlanName());
                item.put("equipmentName", plan.getEquipmentName());
                item.put("nextExecuteTime", plan.getNextExecuteTime());
                item.put("lastExecuteTime", plan.getLastExecuteTime());
                item.put("cycleValue", plan.getCycleValue());
                item.put("cycleUnit", plan.getCycleUnit());
                item.put("autoAssign", plan.getAutoAssign());
                item.put("status", plan.getStatus());
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 从PM计划创建工单对象
     */
    private DmsWorkOrder createWorkOrderFromPlan(DmsPmPlan plan)
    {
        DmsWorkOrder workOrder = new DmsWorkOrder();
        workOrder.setOrderType("1"); // PM维护
        workOrder.setFaultDescription("PM计划维护：" + plan.getPlanName()
                + (plan.getTaskList() != null && !plan.getTaskList().isEmpty() ? "\n任务清单：" + plan.getTaskList() : ""));
        workOrder.setPriority("3"); // 低优先级（计划性）
        workOrder.setOrderStatus("0"); // 新建
        workOrder.setReportTime(new Date());
        workOrder.setRemark("由PM计划[" + plan.getPlanName() + "]自动生成(PM计划ID:" + plan.getPlanId() + ")");

        // 设置报修人为当前操作人
        try
        {
            workOrder.setReporterId(SecurityUtils.getUserId());
            workOrder.setReporterName(SecurityUtils.getUsername());
            workOrder.setCreateBy(SecurityUtils.getUsername());
        }
        catch (Exception e)
        {
            workOrder.setReporterName("system");
            workOrder.setCreateBy("system");
        }

        // 将任务清单复制到工单（规范化为带完成情况/备注的明细结构JSON，完工时逐项填写）
        String normalizedTaskList = normalizeTaskChecklist(plan.getTaskList());
        if (StringUtils.isNotEmpty(normalizedTaskList))
        {
            workOrder.setTaskChecklist(normalizedTaskList);
        }

        // 关联设备
        if (plan.getEquipmentId() != null)
        {
            DmsEquipment eq = dmsEquipmentMapper.selectEquipmentById(plan.getEquipmentId());
            if (eq != null)
            {
                workOrder.setEquipmentId(eq.getEquipmentId());
                workOrder.setEquipmentCode(eq.getEquipmentCode());
                workOrder.setEquipmentName(eq.getEquipmentName());
            }
        }

        return workOrder;
    }

    /**
     * 规范化任务清单JSON：兼容纯文本、字符串数组、旧对象数组，
     * 统一输出为明细结构 [{text, done, result, remark}]，供完工阶段逐项填写
     */
    private String normalizeTaskChecklist(String taskList)
    {
        if (StringUtils.isEmpty(taskList))
        {
            return null;
        }
        JSONArray normalized = new JSONArray();
        try
        {
            JSONArray arr = JSON.parseArray(taskList);
            for (Object item : arr)
            {
                String text = null;
                if (item instanceof String)
                {
                    text = (String) item;
                }
                else if (item instanceof JSONObject)
                {
                    JSONObject src = (JSONObject) item;
                    text = src.getString("text");
                    if (text == null) text = src.getString("task");
                    if (text == null) text = src.getString("name");
                }
                if (StringUtils.isNotEmpty(text))
                {
                    normalized.add(buildChecklistItem(text));
                }
            }
        }
        catch (Exception e)
        {
            // 非JSON格式，按行拆分纯文本
            for (String line : taskList.split("\r?\n"))
            {
                if (StringUtils.isNotEmpty(line.trim()))
                {
                    normalized.add(buildChecklistItem(line.trim()));
                }
            }
        }
        return normalized.isEmpty() ? null : normalized.toJSONString();
    }

    /** 构造单个任务清单明细项（完成情况待完工时填写：done=已完成 undone=未完成 na=不适用） */
    private JSONObject buildChecklistItem(String text)
    {
        JSONObject obj = new JSONObject();
        obj.put("text", text);
        obj.put("done", false);
        obj.put("result", "");
        obj.put("remark", "");
        return obj;
    }

    /**
     * 发送PM通知（系统公告）
     */
    private void sendPmNotification(DmsPmPlan plan, DmsWorkOrder workOrder, String message)
    {
        try
        {
            SysNotice notice = new SysNotice();
            notice.setNoticeTitle("PM维护提醒");
            notice.setNoticeType("1"); // 通知
            notice.setNoticeContent(message
                    + (plan.getEquipmentName() != null ? "\n关联设备：" + plan.getEquipmentName() : "")
                    + "\n下次执行时间：" + plan.getNextExecuteTime());
            notice.setStatus("0");
            notice.setCreateBy("system");
            sysNoticeService.insertNotice(notice);
        }
        catch (Exception e)
        {
            System.err.println("发送PM通知失败: " + e.getMessage());
        }
    }

    /**
     * 发送消息中心提醒：设备保养到期（DEF-03）
     * 消息规范（docs/消息提醒方案设计.md §2.7）：类型2-业务预警，级别2-重要，
     * 接收角色 dms:pmplan:list，跳转 /dms/pm/plan
     */
    private void sendPmDueMessage(DmsPmPlan plan, DmsWorkOrder workOrder, String prefix)
    {
        try
        {
            String title = "设备保养到期提醒";
            String content = prefix + "PM计划「" + plan.getPlanName() + "」保养到期"
                    + "，已生成保养工单「" + workOrder.getOrderNo() + "」"
                    + "，设备：" + (plan.getEquipmentName() != null ? plan.getEquipmentName() : "-")
                    + "，请及时安排保养。";
            messageHelper.sendMessage(
                title,
                content,
                "2",   // 业务预警
                "2",   // 重要
                "dms",
                plan.getPlanId(),
                "/dms/pm/plan?equipId=" + (plan.getEquipmentId() != null ? plan.getEquipmentId() : ""),
                "dms:pmplan:list",
                "0",   // bizStatus: 工单新建待派
                "PM保养计划"
            );
        }
        catch (Exception e)
        {
            System.err.println("发送设备保养到期消息失败: " + e.getMessage());
        }
    }

    /** 工单状态中文描述 */
    private String orderStatusText(String status)
    {
        switch (status)
        {
            case "0": return "新建";
            case "1": return "已派工";
            case "2": return "已接单";
            case "3": return "处理中";
            case "4": return "已完成(待验收)";
            case "5": return "已验收";
            case "6": return "已撤销";
            case "7": return "驳回重做";
            default: return status;
        }
    }

    private void calculateNextExecuteTime(DmsPmPlan plan)
    {
        if (plan.getCycleValue() == null || plan.getCycleUnit() == null) return;
        Calendar cal = Calendar.getInstance();
        if (plan.getLastExecuteTime() != null)
        {
            cal.setTime(plan.getLastExecuteTime());
        }
        int value = plan.getCycleValue();
        String unit = plan.getCycleUnit();
        if ("天".equals(unit) || "小时".equals(unit))
        {
            if ("天".equals(unit)) cal.add(Calendar.DAY_OF_MONTH, value);
            else cal.add(Calendar.HOUR_OF_DAY, value);
        }
        else if ("月".equals(unit))
        {
            cal.add(Calendar.MONTH, value);
        }
        else if ("件".equals(unit))
        {
            // 加工量触发，暂不计算下次时间
            return;
        }
        else
        {
            cal.add(Calendar.DAY_OF_MONTH, value);
        }
        plan.setNextExecuteTime(cal.getTime());
    }
}

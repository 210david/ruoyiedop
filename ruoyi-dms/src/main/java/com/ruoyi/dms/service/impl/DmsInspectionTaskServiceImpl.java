package com.ruoyi.dms.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.dms.domain.DmsEquipment;
import com.ruoyi.dms.domain.DmsInspectionRoute;
import com.ruoyi.dms.domain.DmsInspectionTask;
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.mapper.DmsEquipmentMapper;
import com.ruoyi.dms.mapper.DmsInspectionRouteMapper;
import com.ruoyi.dms.mapper.DmsInspectionTaskMapper;
import com.ruoyi.dms.service.IDmsInspectionTaskService;
import com.ruoyi.dms.service.IDmsWorkOrderService;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.system.utils.MessageHelper;

@Service
public class DmsInspectionTaskServiceImpl implements IDmsInspectionTaskService
{
    private static final Logger log = LoggerFactory.getLogger(DmsInspectionTaskServiceImpl.class);

    @Autowired
    private DmsInspectionTaskMapper dmsInspectionTaskMapper;

    @Autowired
    private DmsInspectionRouteMapper dmsInspectionRouteMapper;

    @Autowired
    private DmsEquipmentMapper dmsEquipmentMapper;

    @Autowired
    private IDmsWorkOrderService dmsWorkOrderService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private MessageHelper messageHelper;

    @Override
    public List<DmsInspectionTask> selectTaskList(DmsInspectionTask task) { return dmsInspectionTaskMapper.selectTaskList(task); }
    @Override
    public DmsInspectionTask selectTaskById(Long taskId) { return dmsInspectionTaskMapper.selectTaskById(taskId); }

    @Override
    public int insertTask(DmsInspectionTask task)
    {
        task.setDelFlag("0");
        if (StringUtils.isEmpty(task.getTaskNo()))
        {
            task.setTaskNo(mkNumberRuleService.generateNumber("dms_inspection_task"));
        }
        if (task.getTaskStatus() == null) task.setTaskStatus("0");
        if (task.getOverdueFlag() == null) task.setOverdueFlag("0");
        return dmsInspectionTaskMapper.insertTask(task);
    }
    @Override
    public int updateTask(DmsInspectionTask task) { return dmsInspectionTaskMapper.updateTask(task); }
    @Override
    public int deleteTaskByIds(Long[] taskIds) { return dmsInspectionTaskMapper.deleteTaskByIds(taskIds); }

    /**
     * 开始执行点检任务
     * 将状态从待执行(0)改为执行中(1)，并记录开始时间
     */
    @Override
    public int startTask(Long taskId)
    {
        DmsInspectionTask db = dmsInspectionTaskMapper.selectTaskById(taskId);
        if (db == null) throw new RuntimeException("点检任务不存在");
        // 仅待执行状态可开始
        if (!"0".equals(db.getTaskStatus()) && !"1".equals(db.getTaskStatus()))
        {
            throw new RuntimeException("当前状态不允许开始执行");
        }
        DmsInspectionTask update = new DmsInspectionTask();
        update.setTaskId(taskId);
        update.setTaskStatus("1"); // 执行中
        // 如果开始时间为空则记录当前时间
        if (db.getStartTime() == null)
        {
            update.setStartTime(new Date());
        }
        try { update.setUpdateBy(SecurityUtils.getUsername()); }
        catch (Exception e) { update.setUpdateBy("system"); }
        return dmsInspectionTaskMapper.updateTask(update);
    }

    /**
     * 完成点检任务
     * 如果存在异常项（abnormalCount > 0），自动生成点检整改工单
     */
    @Override
    @Transactional
    public int completeTask(DmsInspectionTask task)
    {
        DmsInspectionTask db = dmsInspectionTaskMapper.selectTaskById(task.getTaskId());
        if (db == null) throw new RuntimeException("点检任务不存在");

        task.setTaskStatus("2"); // 已完成
        task.setCompleteTime(new Date());
        // 兜底：如果开始时间为空，补设为当前时间
        if (db.getStartTime() == null)
        {
            task.setStartTime(new Date());
        }
        int rows = dmsInspectionTaskMapper.updateTask(task);

        // 如果存在异常项，按设备自动生成工单：仅异常设备生成，且只带入该设备的异常项
        int abnormalCount = task.getAbnormalCount() != null ? task.getAbnormalCount() : 0;
        if (abnormalCount > 0)
        {
            createWorkOrdersForAbnormal(task, db, abnormalCount);
        }

        return rows;
    }

    /**
     * 按设备生成点检整改工单：
     * 1. 仅存在异常项的设备才生成工单（一个设备一张工单）
     * 2. 工单中只带入该设备的异常项目信息
     * 3. 报修人与操作人（工单历史）均为点检人
     * 旧格式结果（无设备结构）兜底生成单个工单
     */
    private void createWorkOrdersForAbnormal(DmsInspectionTask task, DmsInspectionTask db, int abnormalCount)
    {
        // 点检人：报修人 + 工单操作人
        Long reporterId = task.getInspectorId() != null ? task.getInspectorId() : db.getInspectorId();
        String reporterName = StringUtils.isNotEmpty(task.getInspectorName()) ? task.getInspectorName() : db.getInspectorName();
        if (StringUtils.isEmpty(reporterName))
        {
            try { reporterName = SecurityUtils.getUsername(); } catch (Exception e) { reporterName = "system"; }
        }

        // 新格式：按设备分组生成
        String resultData = task.getResultData();
        if (StringUtils.isNotEmpty(resultData))
        {
            try
            {
                Object parsed = JSON.parse(resultData);
                if (parsed instanceof JSONObject)
                {
                    JSONArray devices = ((JSONObject) parsed).getJSONArray("devices");
                    if (devices != null && !devices.isEmpty())
                    {
                        int orderCount = 0;
                        for (int d = 0; d < devices.size(); d++)
                        {
                            JSONObject dev = devices.getJSONObject(d);
                            String detail = buildAbnormalDetailForDevice(dev);
                            if (detail.isEmpty())
                            {
                                continue; // 无异常项的设备不生成工单
                            }
                            if (insertInspectionWorkOrder(task, db, dev.getLong("equipmentId"),
                                    dev.getString("equipmentName"), detail, reporterId, reporterName))
                            {
                                orderCount++;
                            }
                        }
                        if (orderCount > 0)
                        {
                            log.info("点检任务[{}]按设备生成{}张整改工单", db.getTaskNo(), orderCount);
                            return;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("解析点检结果失败，走兜底逻辑: {}", e.getMessage());
            }
        }

        // 兜底：旧格式（扁平数组/仅common，无设备结构），生成单个工单
        // OBS-02修复：回填路线上的首台设备，避免工单设备信息缺失
        String equipmentNames = "";
        Long fallbackEquipmentId = null;
        String fallbackEquipmentName = null;
        if (db.getRouteId() != null)
        {
            DmsInspectionRoute route = dmsInspectionRouteMapper.selectRouteById(db.getRouteId());
            if (route != null && StringUtils.isNotEmpty(route.getEquipmentIds()))
            {
                try
                {
                    JSONArray routeIds = JSON.parseArray(route.getEquipmentIds());
                    if (routeIds != null && !routeIds.isEmpty())
                    {
                        fallbackEquipmentId = routeIds.getLong(0);
                        if (fallbackEquipmentId != null)
                        {
                            DmsEquipment fallbackEquipment = dmsEquipmentMapper.selectEquipmentById(fallbackEquipmentId);
                            if (fallbackEquipment != null)
                            {
                                fallbackEquipmentName = fallbackEquipment.getEquipmentName();
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    log.warn("解析路线设备ID失败: {}", e.getMessage());
                }
                equipmentNames = getEquipmentNames(route.getEquipmentIds());
            }
        }
        String abnormalDetail = formatAbnormalDetail(resultData);
        insertInspectionWorkOrder(task, db, fallbackEquipmentId,
                StringUtils.isNotEmpty(fallbackEquipmentName) ? fallbackEquipmentName : equipmentNames,
                abnormalDetail, reporterId, reporterName);
    }

    /**
     * 构建单台设备的异常项明细（只包含标记为异常的项目）
     * @return 异常明细文本；设备无异常项时返回空串
     */
    private String buildAbnormalDetailForDevice(JSONObject dev)
    {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        JSONArray items = dev.getJSONArray("items");
        if (items != null)
        {
            for (int i = 0; i < items.size(); i++)
            {
                JSONObject item = items.getJSONObject(i);
                boolean isAbnormal = item.getBooleanValue("abnormal") || "abnormal".equals(item.getString("result"));
                if (!isAbnormal) continue;
                sb.append(index).append(". ").append(item.getString("item"));
                String desc = item.getString("abnormalDesc");
                if (StringUtils.isNotEmpty(desc))
                {
                    sb.append("\n   → 异常说明：").append(desc);
                }
                sb.append("\n");
                index++;
            }
        }
        return sb.toString();
    }

    /**
     * 创建一张点检整改工单
     * @param equipmentId 异常设备ID（兜底场景可为null）
     * @param equipmentName 设备名称
     * @param abnormalDetail 该设备的异常项明细
     * @param reporterId/Name 点检人（报修人+操作人）
     * @return 是否成功创建
     */
    private boolean insertInspectionWorkOrder(DmsInspectionTask task, DmsInspectionTask db, Long equipmentId,
            String equipmentName, String abnormalDetail, Long reporterId, String reporterName)
    {
        try
        {
            // 补充设备编号/名称
            String equipmentCode = null;
            String equipmentDispName = equipmentName;
            if (equipmentId != null)
            {
                DmsEquipment equipment = dmsEquipmentMapper.selectEquipmentById(equipmentId);
                if (equipment != null)
                {
                    equipmentCode = equipment.getEquipmentCode();
                    if (StringUtils.isEmpty(equipmentDispName))
                    {
                        equipmentDispName = equipment.getEquipmentName();
                    }
                }
            }

            // 故障描述只保留异常明细
            String desc = StringUtils.isNotEmpty(abnormalDetail) ? abnormalDetail.trim() : "点检发现异常";

            DmsWorkOrder workOrder = new DmsWorkOrder();
            workOrder.setOrderType("2"); // 点检整改
            workOrder.setFaultDescription(desc);
            workOrder.setEquipmentId(equipmentId);
            workOrder.setEquipmentCode(equipmentCode);
            workOrder.setEquipmentName(StringUtils.isEmpty(equipmentDispName) ? null : equipmentDispName);
            workOrder.setPriority("1"); // 高优先级
            workOrder.setOrderStatus("0"); // 新建
            workOrder.setReportTime(new Date());
            // 报修人为点检人
            workOrder.setReporterId(reporterId);
            workOrder.setReporterName(reporterName);
            // 工单操作历史的操作人（createBy）为点检人
            workOrder.setCreateBy(reporterName);
            workOrder.setRemark("由点检任务[" + db.getTaskNo() + "]自动生成(点检任务ID:" + task.getTaskId() + ")");
            dmsWorkOrderService.insertWorkOrder(workOrder);
            return true;
        }
        catch (Exception e)
        {
            log.error("点检任务[{}]生成整改工单失败: {}", db.getTaskNo(), e.getMessage());
            return false;
        }
    }

    /**
     * 根据路线的equipmentIds(JSON数组)获取设备名称列表
     */
    private String getEquipmentNames(String equipmentIdsJson)
    {
        try
        {
            JSONArray ids = JSON.parseArray(equipmentIdsJson);
            List<String> names = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++)
            {
                Long equipmentId = ids.getLong(i);
                DmsEquipment equipment = dmsEquipmentMapper.selectEquipmentById(equipmentId);
                if (equipment != null)
                {
                    String name = equipment.getEquipmentCode() + " - " + equipment.getEquipmentName();
                    names.add(name);
                }
            }
            return String.join("、", names);
        }
        catch (Exception e)
        {
            log.warn("解析设备ID列表失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 解析点检结果JSON，提取异常项格式化展示
     * 支持嵌套结构：{common:[...], devices:[{equipmentId, equipmentName, items:[...]}]}
     * 兼容旧格式：扁平数组
     */
    private String formatAbnormalDetail(String resultData)
    {
        if (resultData == null || resultData.isEmpty()) return "";
        try
        {
            Object parsed = JSON.parse(resultData);
            StringBuilder sb = new StringBuilder();
            int index = 1;

            if (parsed instanceof JSONArray)
            {
                // 旧格式：扁平数组
                JSONArray results = (JSONArray) parsed;
                for (int i = 0; i < results.size(); i++)
                {
                    JSONObject item = results.getJSONObject(i);
                    index = appendAbnormalItem(sb, index, item, null);
                }
            }
            else if (parsed instanceof JSONObject)
            {
                // 新格式：嵌套结构
                JSONObject obj = (JSONObject) parsed;
                // 通用异常项
                JSONArray common = obj.getJSONArray("common");
                if (common != null)
                {
                    boolean hasCommonAbnormal = false;
                    for (int i = 0; i < common.size(); i++)
                    {
                        JSONObject item = common.getJSONObject(i);
                        boolean isAbnormal = item.getBooleanValue("abnormal") || "abnormal".equals(item.getString("result"));
                        if (isAbnormal) { hasCommonAbnormal = true; index = appendAbnormalItem(sb, index, item, null); }
                    }
                }
                // 设备明细分组
                JSONArray devices = obj.getJSONArray("devices");
                if (devices != null)
                {
                    for (int d = 0; d < devices.size(); d++)
                    {
                        JSONObject dev = devices.getJSONObject(d);
                        String eqName = dev.getString("equipmentName");
                        JSONArray items = dev.getJSONArray("items");
                        if (items != null)
                        {
                            for (int i = 0; i < items.size(); i++)
                            {
                                JSONObject item = items.getJSONObject(i);
                                index = appendAbnormalItem(sb, index, item, eqName);
                            }
                        }
                    }
                }
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            log.warn("解析点检结果失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 追加单个异常项到StringBuilder
     * @param equipmentName 设备名称，null表示通用项
     * @return 更新后的序号
     */
    private int appendAbnormalItem(StringBuilder sb, int index, JSONObject item, String equipmentName)
    {
        boolean isAbnormal = item.getBooleanValue("abnormal") || "abnormal".equals(item.getString("result"));
        if (!isAbnormal) return index;

        sb.append(index).append(". ");
        if (equipmentName != null && !equipmentName.isEmpty())
        {
            sb.append("[").append(equipmentName).append("] ");
        }
        sb.append(item.getString("item"));
        // 数值类型显示记录值
        if ("number".equals(item.getString("type")) && item.get("value") != null)
        {
            sb.append("（记录值：").append(item.get("value"));
            if (item.getString("unit") != null) sb.append(item.getString("unit"));
            sb.append("）");
        }
        // 异常说明
        String desc = item.getString("abnormalDesc");
        if (desc != null && !desc.isEmpty())
        {
            sb.append("\n   → 异常说明：").append(desc);
        }
        sb.append("\n");
        return index + 1;
    }

    /**
     * 根据巡检路线周期自动生成点检任务
     */
    @Override
    public int autoGenerateTasks()
    {
        int count = 0;
        DmsInspectionRoute query = new DmsInspectionRoute();
        query.setStatus("0");
        List<DmsInspectionRoute> routes = dmsInspectionRouteMapper.selectRouteList(query);

        for (DmsInspectionRoute route : routes)
        {
            try
            {
                if (!shouldGenerateToday(route)) continue;

                List<DmsInspectionTask> todayTasks = dmsInspectionTaskMapper.selectTodayTasksByRouteId(route.getRouteId());
                if (todayTasks != null && !todayTasks.isEmpty()) continue;

                DmsInspectionTask task = new DmsInspectionTask();
                task.setRouteId(route.getRouteId());
                task.setRouteName(route.getRouteName());
                task.setPlanDate(new Date());
                task.setTaskStatus("0");
                task.setTaskNo(mkNumberRuleService.generateNumber("dms_inspection_task"));
                task.setCreateBy("system");
                dmsInspectionTaskMapper.insertTask(task);
                count++;
            }
            catch (Exception e)
            {
                log.error("路线[{}]自动生成点检任务失败: {}", route.getRouteId(), e.getMessage());
            }
        }
        return count;
    }

    @Override
    public int markOverdueTasks()
    {
        List<DmsInspectionTask> overdueTasks = dmsInspectionTaskMapper.selectOverdueTasks();
        int count = 0;
        for (DmsInspectionTask task : overdueTasks)
        {
            DmsInspectionTask update = new DmsInspectionTask();
            update.setTaskId(task.getTaskId());
            // 仅标记逾期，不改变业务状态，任务仍可正常执行
            update.setOverdueFlag("1");
            update.setUpdateBy("system");
            dmsInspectionTaskMapper.updateTask(update);
            count++;

            // 发送消息中心提醒：设备巡检逾期（DEF-03）
            sendInspectionOverdueMessage(task);
        }
        return count;
    }

    /**
     * 发送消息中心提醒：设备巡检逾期（DEF-03）
     * 消息规范（docs/消息提醒方案设计.md §2.7）：类型4-待办事项，级别1-普通，
     * 接收角色 dms:inspection:task:list，跳转 /dms/inspection/task
     */
    private void sendInspectionOverdueMessage(DmsInspectionTask task)
    {
        try
        {
            String title = "设备巡检逾期提醒";
            String content = "点检任务[" + task.getTaskNo() + "]已逾期未完成"
                    + "，路线：" + (task.getRouteName() != null ? task.getRouteName() : "-")
                    + "，计划日期：" + (task.getPlanDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(task.getPlanDate()) : "-")
                    + "，请尽快执行点检。";
            messageHelper.sendMessage(
                title,
                content,
                "4",   // 待办事项
                "1",   // 普通
                "dms",
                task.getTaskId(),
                "/dms/inspection/task?id=" + task.getTaskId(),
                "dms:inspection:task:list",
                "0",   // bizStatus: 待执行
                "点检任务"
            );
        }
        catch (Exception e)
        {
            log.error("发送设备巡检逾期消息失败: taskId={}", task.getTaskId(), e);
        }
    }

    /**
     * 按状态统计点检任务数（含全部）
     */
    @Override
    public java.util.Map<String, Object> countTaskByStatus()
    {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("all", dmsInspectionTaskMapper.countAllTasks());
        List<java.util.Map<String, Object>> statusCounts = dmsInspectionTaskMapper.countTaskByStatus();
        if (statusCounts != null)
        {
            for (java.util.Map<String, Object> item : statusCounts)
            {
                String status = String.valueOf(item.get("taskStatus"));
                Object count = item.get("cnt");
                result.put(status, count);
            }
        }
        return result;
    }

    private boolean shouldGenerateToday(DmsInspectionRoute route)
    {
        String cycleType = route.getCycleType();
        Integer cycleValue = route.getCycleValue() != null ? route.getCycleValue() : 1;
        Calendar cal = Calendar.getInstance();
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        switch (cycleType)
        {
            case "0": return dayOfYear % cycleValue == 0;
            case "1": return dayOfWeek == Calendar.MONDAY && (cal.get(Calendar.WEEK_OF_YEAR) % cycleValue == 0);
            case "2": return dayOfMonth == 1 || dayOfMonth == cycleValue;
            default: return false;
        }
    }
}

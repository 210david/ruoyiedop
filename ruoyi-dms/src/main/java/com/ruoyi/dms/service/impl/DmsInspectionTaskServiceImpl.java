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
import com.ruoyi.dms.domain.DmsEquipment;
import com.ruoyi.dms.domain.DmsInspectionRoute;
import com.ruoyi.dms.domain.DmsInspectionTask;
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.mapper.DmsEquipmentMapper;
import com.ruoyi.dms.mapper.DmsInspectionRouteMapper;
import com.ruoyi.dms.mapper.DmsInspectionTaskMapper;
import com.ruoyi.dms.service.IDmsInspectionTaskService;
import com.ruoyi.dms.service.IDmsWorkOrderService;

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

    @Override
    public List<DmsInspectionTask> selectTaskList(DmsInspectionTask task) { return dmsInspectionTaskMapper.selectTaskList(task); }
    @Override
    public DmsInspectionTask selectTaskById(Long taskId) { return dmsInspectionTaskMapper.selectTaskById(taskId); }

    @Override
    public int insertTask(DmsInspectionTask task)
    {
        task.setDelFlag("0");
        if (task.getTaskNo() == null || task.getTaskNo().isEmpty())
        {
            task.setTaskNo("INS" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        }
        if (task.getTaskStatus() == null) task.setTaskStatus("0");
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

        // 如果存在异常项，自动生成工单
        int abnormalCount = task.getAbnormalCount() != null ? task.getAbnormalCount() : 0;
        if (abnormalCount > 0)
        {
            // 获取巡检路线，提取设备信息
            DmsInspectionRoute route = null;
            String equipmentNames = "";
            if (db.getRouteId() != null)
            {
                route = dmsInspectionRouteMapper.selectRouteById(db.getRouteId());
                if (route != null && route.getEquipmentIds() != null)
                {
                    equipmentNames = getEquipmentNames(route.getEquipmentIds());
                }
            }

            // 解析异常项明细
            String abnormalDetail = formatAbnormalDetail(task.getResultData());

            // 构建格式化的故障描述
            StringBuilder desc = new StringBuilder();
            desc.append("【点检异常整改】\n");
            desc.append("巡检路线：").append(db.getRouteName() != null ? db.getRouteName() : "—").append("\n");
            desc.append("关联设备：").append(equipmentNames.isEmpty() ? "—" : equipmentNames).append("\n");
            desc.append("点检人：").append(db.getInspectorName() != null ? db.getInspectorName() : "—").append("\n");
            desc.append("异常项数：").append(abnormalCount).append("\n");
            if (!abnormalDetail.isEmpty())
            {
                desc.append("\n异常明细：\n").append(abnormalDetail);
            }

            DmsWorkOrder workOrder = new DmsWorkOrder();
            workOrder.setOrderType("2"); // 点检整改
            workOrder.setFaultDescription(desc.toString());
            workOrder.setEquipmentName(equipmentNames.isEmpty() ? null : equipmentNames);
            workOrder.setPriority("1"); // 高优先级
            workOrder.setOrderStatus("0"); // 新建
            workOrder.setReportTime(new Date());
            // 设置报修人：优先用点检人，为空时取当前操作人
            if (task.getInspectorId() != null)
            {
                workOrder.setReporterId(task.getInspectorId());
            }
            else
            {
                try { workOrder.setReporterId(SecurityUtils.getUserId()); } catch (Exception e) { /* ignore */ }
            }
            String reporterName = task.getInspectorName();
            if (reporterName == null || reporterName.isEmpty())
            {
                try { reporterName = SecurityUtils.getUsername(); } catch (Exception e) { reporterName = "system"; }
            }
            workOrder.setReporterName(reporterName);
            workOrder.setRemark("由点检任务[" + db.getTaskNo() + "]自动生成(点检任务ID:" + task.getTaskId() + ")");
            try { workOrder.setCreateBy(SecurityUtils.getUsername()); }
            catch (Exception e) { workOrder.setCreateBy("system"); }
            dmsWorkOrderService.insertWorkOrder(workOrder);
        }

        return rows;
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
                task.setTaskNo("INS" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
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
            update.setTaskStatus("3");
            update.setUpdateBy("system");
            dmsInspectionTaskMapper.updateTask(update);
            count++;
        }
        return count;
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

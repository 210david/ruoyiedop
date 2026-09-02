package com.ruoyi.safety.service.impl;
import java.util.HashMap;
import java.util.Map;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.annotation.DataScope;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.safety.domain.SafetyHazard;
import com.ruoyi.safety.domain.SafetyInspectionTask;
import com.ruoyi.safety.mapper.SafetyHazardMapper;
import com.ruoyi.safety.mapper.SafetyInspectionTaskMapper;
import com.ruoyi.safety.service.ISafetyInspectionTaskService;

@Service
public class SafetyInspectionTaskServiceImpl implements ISafetyInspectionTaskService
{
    @Autowired
    private SafetyInspectionTaskMapper safetyInspectionTaskMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private SafetyHazardMapper safetyHazardMapper;

    @Override
    public SafetyInspectionTask selectSafetyInspectionTaskById(Long taskId)
    {
        SafetyInspectionTask task = safetyInspectionTaskMapper.selectSafetyInspectionTaskById(taskId);
        if (task != null && "2".equals(task.getInspectResult()))
        {
            // 查询关联的隐患列表
            SafetyHazard query = new SafetyHazard();
            query.setTaskId(taskId);
            task.setHazardList(safetyHazardMapper.selectSafetyHazardList(query));
        }
        return task;
    }

@Override
@DataScope(deptAlias = "d", userAlias = "su")
public List<SafetyInspectionTask> selectSafetyInspectionTaskList(SafetyInspectionTask safetyInspectionTask) { return safetyInspectionTaskMapper.selectSafetyInspectionTaskList(safetyInspectionTask); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSafetyInspectionTask(SafetyInspectionTask safetyInspectionTask) {
        if (StringUtils.isEmpty(safetyInspectionTask.getTaskCode())) {
            safetyInspectionTask.setTaskCode(mkNumberRuleService.generateNumber("safety_inspection_task"));
        }
        // 设置默认值
        if (StringUtils.isEmpty(safetyInspectionTask.getTaskStatus())) {
            safetyInspectionTask.setTaskStatus("0"); // 待执行
        }
        if (safetyInspectionTask.getDelFlag() == null) {
            safetyInspectionTask.setDelFlag("0");
        }
        if (safetyInspectionTask.getHazardCount() == null) {
            safetyInspectionTask.setHazardCount(0);
        }
        return safetyInspectionTaskMapper.insertSafetyInspectionTask(safetyInspectionTask);
    }

    @Override
    public int updateSafetyInspectionTask(SafetyInspectionTask safetyInspectionTask) {
        // 通用编辑不允许直接修改状态，状态流转只能通过反馈/作废等专用接口
        safetyInspectionTask.setTaskStatus(null);
        return safetyInspectionTaskMapper.updateSafetyInspectionTask(safetyInspectionTask); }

    @Override
    public int deleteSafetyInspectionTaskByIds(Long[] taskIds) { return safetyInspectionTaskMapper.deleteSafetyInspectionTaskByIds(taskIds); }

    @Override
    public int deleteSafetyInspectionTaskById(Long taskId) { return safetyInspectionTaskMapper.deleteSafetyInspectionTaskById(taskId); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitFeedback(SafetyInspectionTask safetyInspectionTask)
    {
        if (safetyInspectionTask.getTaskId() == null)
        {
            throw new ServiceException("任务ID不能为空");
        }
        if (StringUtils.isEmpty(safetyInspectionTask.getInspectResult()))
        {
            throw new ServiceException("请选择检查结果");
        }
        SafetyInspectionTask task = safetyInspectionTaskMapper.selectSafetyInspectionTaskById(safetyInspectionTask.getTaskId());
        if (task == null)
        {
            throw new ServiceException("排查任务不存在");
        }
        if (!"0".equals(task.getTaskStatus()) && !"1".equals(task.getTaskStatus()))
        {
            throw new ServiceException("当前任务状态不允许提交执行反馈");
        }
        safetyInspectionTask.setTaskStatus("2"); // 已完成
        safetyInspectionTask.setInspectTime(DateUtils.getNowDate());

        // 如果检查结果为"有隐患"，联动创建隐患记录
        if ("2".equals(safetyInspectionTask.getInspectResult()))
        {
            List<SafetyHazard> hazardList = safetyInspectionTask.getHazardList();
            if (hazardList == null || hazardList.isEmpty())
            {
                throw new ServiceException("检查结果为有隐患时，请至少添加一条隐患记录");
            }
            int count = 0;
            for (SafetyHazard hazard : hazardList)
            {
                // 校验必填字段
                if (StringUtils.isEmpty(hazard.getHazardDesc()))
                {
                    throw new ServiceException("隐患描述不能为空");
                }
                if (StringUtils.isEmpty(hazard.getHazardType()))
                {
                    throw new ServiceException("隐患类型不能为空");
                }
                if (StringUtils.isEmpty(hazard.getHazardLevel()))
                {
                    throw new ServiceException("隐患等级不能为空");
                }
                if (StringUtils.isEmpty(hazard.getRectifyReq()))
                {
                    throw new ServiceException("整改要求不能为空");
                }
                if (hazard.getRectifyDeadline() == null)
                {
                    throw new ServiceException("整改期限不能为空");
                }
                if (hazard.getPersonId() == null)
                {
                    throw new ServiceException("责任人不能为空");
                }
                // 自动填充隐患信息
                Map<String, String> params = new HashMap<>();
            if (StringUtils.isNotEmpty(hazard.getHazardLevel())) { params.put("hazardLevel", hazard.getHazardLevel()); }
            if (StringUtils.isNotEmpty(hazard.getHazardType())) { params.put("hazardType", hazard.getHazardType()); }
            hazard.setHazardCode(mkNumberRuleService.generateNumber("safety_hazard", params));
                hazard.setSourceType("2"); // 计划排查
                hazard.setTaskId(safetyInspectionTask.getTaskId());
                hazard.setDiscoverPersonId(task.getExecutorId());
                hazard.setDiscoverPerson(task.getExecutorName());
                hazard.setDiscoverTime(DateUtils.getNowDate());
                hazard.setHazardStatus("0"); // 草稿
                hazard.setRiskPointId(task.getRiskPointId());
                if (hazard.getDelFlag() == null)
                {
                    hazard.setDelFlag("0");
                }
                safetyHazardMapper.insertSafetyHazard(hazard);
                count++;
            }
            safetyInspectionTask.setHazardCount(count);
        }
        else
        {
            safetyInspectionTask.setHazardCount(0);
        }
        return safetyInspectionTaskMapper.updateSafetyInspectionTask(safetyInspectionTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelTask(Long taskId)
    {
        SafetyInspectionTask task = safetyInspectionTaskMapper.selectSafetyInspectionTaskById(taskId);
        if (task == null)
        {
            throw new ServiceException("排查任务不存在");
        }
        if (!"0".equals(task.getTaskStatus()) && !"1".equals(task.getTaskStatus()))
        {
            throw new ServiceException("当前任务状态不允许作废");
        }
        SafetyInspectionTask update = new SafetyInspectionTask();
        update.setTaskId(taskId);
        update.setTaskStatus("3"); // 已作废
        return safetyInspectionTaskMapper.updateSafetyInspectionTask(update);
    }
}
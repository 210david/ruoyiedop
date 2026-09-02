package com.ruoyi.qms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsInspTask;
import com.ruoyi.qms.domain.QmsInspItem;
import com.ruoyi.qms.domain.QmsMaterialAttr;
import com.ruoyi.qms.domain.QmsEsigRecord;
import com.ruoyi.qms.domain.QmsNcr;
import com.ruoyi.qms.mapper.QmsInspTaskMapper;
import com.ruoyi.qms.mapper.QmsInspItemMapper;
import com.ruoyi.qms.mapper.QmsMaterialAttrMapper;
import com.ruoyi.qms.service.IQmsInspTaskService;
import com.ruoyi.qms.service.IQmsEsigRecordService;
import com.ruoyi.qms.service.IQmsNcrService;
import com.ruoyi.qms.util.AqlCalculator;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 检验任务 Service实现
 *
 * @author ruoyi
 */
@Service
public class QmsInspTaskServiceImpl implements IQmsInspTaskService
{
    private static final Logger log = LoggerFactory.getLogger(QmsInspTaskServiceImpl.class);

    @Autowired
    private QmsInspTaskMapper qmsInspTaskMapper;

    @Autowired
    private QmsInspItemMapper qmsInspItemMapper;

    @Autowired
    private QmsMaterialAttrMapper qmsMaterialAttrMapper;

    @Autowired
    private IQmsEsigRecordService qmsEsigRecordService;

    @Autowired
    private IQmsNcrService qmsNcrService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<QmsInspTask> selectInspTaskList(QmsInspTask inspTask)
    {
        return qmsInspTaskMapper.selectInspTaskList(inspTask);
    }

    @Override
    public QmsInspTask selectInspTaskById(Long taskId)
    {
        QmsInspTask task = qmsInspTaskMapper.selectInspTaskById(taskId);
        if (task != null)
        {
            List<QmsInspItem> items = qmsInspItemMapper.selectInspItemListByTaskId(taskId);
            task.setItemList(items);
        }
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertInspTask(QmsInspTask inspTask)
    {
        // 自动生成检验任务编号
        if (StringUtils.isEmpty(inspTask.getTaskNo()))
        {
            // 传递动态前缀参数（检验类型），使动态前缀规则可生效
            Map<String, String> params = new HashMap<>();
            if (StringUtils.isNotEmpty(inspTask.getTaskType()))
            {
                params.put("taskType", inspTask.getTaskType());
            }
            if (StringUtils.isNotEmpty(inspTask.getSourceType()))
            {
                params.put("sourceType", inspTask.getSourceType());
            }
            inspTask.setTaskNo(mkNumberRuleService.generateNumber("qms_insp_task", params));
        }
        // 设置默认检验水平（OQC默认S-4, 其他默认II）
        if (StringUtils.isEmpty(inspTask.getInspectMethod()))
        {
            inspTask.setInspectMethod(getDefaultInspectMethod(inspTask.getTaskType()));
        }
        // 设置默认检验严格度
        if (inspTask.getInspectLevel() == null)
        {
            inspTask.setInspectLevel("1");
        }
        // 自动计算AQL抽样数（根据检验水平）
        if (inspTask.getInspectQty() != null && StringUtils.isNotEmpty(inspTask.getAqlLevel()))
        {
            int[] result = AqlCalculator.calculate(inspTask.getInspectQty(), inspTask.getAqlLevel(), inspTask.getInspectMethod());
            if (result != null)
            {
                inspTask.setSampleSize(result[0]);
                inspTask.setAcVal(result[1]);
                inspTask.setReVal(result[2]);
            }
        }
        if (inspTask.getTaskStatus() == null)
        {
            inspTask.setTaskStatus("0");
        }
        inspTask.setDelFlag("0");
        inspTask.setStatus("0");
        if (inspTask.getIsRecheck() == null)
        {
            inspTask.setIsRecheck("0");
        }
        return qmsInspTaskMapper.insertInspTask(inspTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateInspTask(QmsInspTask inspTask)
    {
        // 送检数量、AQL等级或检验水平变更时，重新计算抽样参数
        if (inspTask.getInspectQty() != null && StringUtils.isNotEmpty(inspTask.getAqlLevel()))
        {
            // 若未指定检验水平，根据任务类型取默认值
            String inspectMethod = inspTask.getInspectMethod();
            if (StringUtils.isEmpty(inspectMethod))
            {
                inspectMethod = getDefaultInspectMethod(inspTask.getTaskType());
            }
            int[] result = AqlCalculator.calculate(inspTask.getInspectQty(), inspTask.getAqlLevel(), inspectMethod);
            if (result != null)
            {
                inspTask.setSampleSize(result[0]);
                inspTask.setAcVal(result[1]);
                inspTask.setReVal(result[2]);
            }
        }
        return qmsInspTaskMapper.updateInspTask(inspTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteInspTaskByIds(Long[] taskIds)
    {
        for (Long taskId : taskIds)
        {
            qmsInspItemMapper.deleteInspItemByTaskId(taskId);
        }
        return qmsInspTaskMapper.deleteInspTaskByIds(taskIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveInspectResult(QmsInspTask inspTask)
    {
        QmsInspTask existing = qmsInspTaskMapper.selectInspTaskById(inspTask.getTaskId());
        if (existing == null)
        {
            throw new ServiceException("检验任务不存在");
        }
        if ("2".equals(existing.getTaskStatus()))
        {
            throw new ServiceException("该检验任务已判定，不可修改");
        }
        // 先删除旧明细
        qmsInspItemMapper.deleteInspItemByTaskId(inspTask.getTaskId());
        // 批量保存检验明细
        List<QmsInspItem> items = inspTask.getItemList();
        if (items != null && !items.isEmpty())
        {
            for (QmsInspItem item : items)
            {
                item.setTaskId(inspTask.getTaskId());
            }
            qmsInspItemMapper.batchInsertInspItem(items);
        }
        // 自动判定 - 按缺陷数量统计（不是按行数）
        int defectCount = 0;
        String highestDefectLevel = "4"; // 默认轻微
        if (items != null)
        {
            for (QmsInspItem item : items)
            {
                if ("2".equals(item.getItemResult()))
                {
                    // 使用缺陷数量字段，默认为1（兼容旧数据）
                    int qty = item.getDefectQty() != null ? item.getDefectQty() : 1;
                    defectCount += qty;
                    // 取最高缺陷等级（数值越小等级越高）
                    if (item.getDefectLevel() != null)
                    {
                        int level = Integer.parseInt(item.getDefectLevel());
                        int current = Integer.parseInt(highestDefectLevel);
                        if (level < current)
                        {
                            highestDefectLevel = item.getDefectLevel();
                        }
                    }
                }
            }
        }
        // 致命/严重缺陷直接判不合格
        boolean hasCriticalDefect = "1".equals(highestDefectLevel) || "2".equals(highestDefectLevel);
        boolean batchResult;
        if (hasCriticalDefect)
        {
            batchResult = false;
        }
        else if (existing.getAcVal() != null && existing.getReVal() != null)
        {
            batchResult = AqlCalculator.judgeBatch(defectCount, existing.getAcVal(), existing.getReVal());
        }
        else
        {
            batchResult = defectCount == 0;
        }
        inspTask.setInspectResult(batchResult ? "1" : "2");
        inspTask.setDefectLevel(highestDefectLevel);
        inspTask.setTaskStatus("2");
        inspTask.setJudgeTime(new Date());
        if (inspTask.getInspectTime() == null)
        {
            inspTask.setInspectTime(new Date());
        }
        int updateResult = qmsInspTaskMapper.updateInspTask(inspTask);
        // 保存电子签名记录（合规行业要求）
        QmsEsigRecord esig = new QmsEsigRecord();
        esig.setBizType("insp_judge");
        esig.setBizId(inspTask.getTaskId());
        esig.setBizNo(inspTask.getTaskNo());
        esig.setSigMeaning("检验员判定");
        esig.setSigOpinion(batchResult ? "批量合格" : "批量不合格，最高缺陷等级：" + highestDefectLevel);
        esig.setSigResult(batchResult ? "pass" : "fail");
        qmsEsigRecordService.saveEsigRecord(esig);
        // 检验不合格时自动创建NCR（不合格品报告）
        if (!batchResult)
        {
            createNcrFromInspection(existing, inspTask, items, highestDefectLevel);
            log.info("检验任务 {} 判定不合格，已自动创建NCR", inspTask.getTaskNo());
        }
        return updateResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QmsInspTask createRecheckTask(Long taskId)
    {
        QmsInspTask original = qmsInspTaskMapper.selectInspTaskById(taskId);
        if (original == null)
        {
            throw new ServiceException("原检验任务不存在");
        }
        QmsInspTask recheck = new QmsInspTask();
        recheck.setTaskType(original.getTaskType());
        recheck.setSourceType(original.getSourceType());
        recheck.setSourceNo(original.getSourceNo());
        recheck.setMaterialId(original.getMaterialId());
        recheck.setMaterialCode(original.getMaterialCode());
        recheck.setMaterialName(original.getMaterialName());
        recheck.setSupplierId(original.getSupplierId());
        recheck.setSupplierName(original.getSupplierName());
        recheck.setBatchNo(original.getBatchNo());
        recheck.setInspectQty(original.getInspectQty());
        recheck.setAqlLevel(original.getAqlLevel());
        recheck.setInspectLevel(original.getInspectLevel());
        recheck.setSampleSize(original.getSampleSize());
        recheck.setAcVal(original.getAcVal());
        recheck.setReVal(original.getReVal());
        recheck.setTaskStatus("0");
        recheck.setIsRecheck("1");
        recheck.setOriginTaskId(taskId);
        recheck.setDelFlag("0");
        recheck.setStatus("0");
        recheck.setRemark("基于任务 " + original.getTaskNo() + " 的复检");
                // 传递动态前缀参数（检验类型），使动态前缀规则可生效
        Map<String, String> recheckParams = new HashMap<>();
        if (StringUtils.isNotEmpty(recheck.getTaskType()))
        {
            recheckParams.put("taskType", recheck.getTaskType());
        }
        if (StringUtils.isNotEmpty(recheck.getSourceType()))
        {
            recheckParams.put("sourceType", recheck.getSourceType());
        }
        recheck.setTaskNo(mkNumberRuleService.generateNumber("qms_insp_task", recheckParams));
        qmsInspTaskMapper.insertInspTask(recheck);
        return recheck;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoGenerateIqcTasks()
    {
        int count = 0;
        // 查询所有需要IQC检验的物料属性（非免检）
        QmsMaterialAttr attrQuery = new QmsMaterialAttr();
        attrQuery.setStatus("0");
        attrQuery.setIsExempt("0");
        List<QmsMaterialAttr> attrList = qmsMaterialAttrMapper.selectMaterialAttrList(attrQuery);
        for (QmsMaterialAttr attr : attrList)
        {
            if (attr.getInspectTypes() == null || !attr.getInspectTypes().contains("IQC"))
            {
                continue;
            }
            // 构造来源单号：IQC_物料ID_YYYYMMDD
            String sourceNo = "IQC_" + attr.getMaterialId() + "_" + new java.text.SimpleDateFormat("yyyyMMdd").format(new Date());
            // 防重复
            if (qmsInspTaskMapper.countBySourceNo(sourceNo) > 0)
            {
                continue;
            }
            // 创建IQC检验任务
            QmsInspTask task = new QmsInspTask();
            task.setTaskType("IQC");
            task.setSourceType("auto_inbound");
            task.setSourceNo(sourceNo);
            task.setMaterialId(attr.getMaterialId());
            task.setMaterialCode(attr.getMaterialCode());
            task.setMaterialName(attr.getMaterialName());
            task.setInspectQty(new BigDecimal("100")); // 默认送检数量，实际应从WMS到货明细获取
            task.setAqlLevel("2.5"); // 默认AQL等级，实际应从物料属性或检验标准获取
            task.setInspectLevel("1"); // 检验严格度：正常
            task.setInspectMethod(getDefaultInspectMethod(task.getTaskType())); // 检验水平
            task.setTaskStatus("0");
            task.setIsRecheck("0");
            task.setDelFlag("0");
            task.setStatus("0");
            task.setRemark("系统自动生成IQC检验任务");
            task.setCreateBy("system");
            // 自动计算AQL抽样数
            int[] aqlResult = AqlCalculator.calculate(task.getInspectQty(), task.getAqlLevel(), task.getInspectMethod());
            if (aqlResult != null)
            {
                task.setSampleSize(aqlResult[0]);
                task.setAcVal(aqlResult[1]);
                task.setReVal(aqlResult[2]);
            }
            qmsInspTaskMapper.insertInspTask(task);
            count++;
            log.info("自动生成IQC检验任务: {} - {}", sourceNo, attr.getMaterialName());
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkOverdueTasks()
    {
        int overdueDays = 3; // 默认3天未完成视为逾期
        List<QmsInspTask> overdueTasks = qmsInspTaskMapper.selectOverdueTasks(overdueDays);
        int count = 0;
        for (QmsInspTask task : overdueTasks)
        {
            // 将状态更新为检验中（标记逾期）
            QmsInspTask update = new QmsInspTask();
            update.setTaskId(task.getTaskId());
            update.setTaskStatus("1");
            update.setUpdateBy("system");
            update.setRemark("系统标记逾期（超过" + overdueDays + "天未完成检验）");
            qmsInspTaskMapper.updateInspTask(update);
            count++;
        }
        return count;
    }

    @Override
    public int countBySourceNo(String sourceNo)
    {
        return qmsInspTaskMapper.countBySourceNo(sourceNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int startInspect(Long taskId)
    {
        QmsInspTask existing = qmsInspTaskMapper.selectInspTaskById(taskId);
        if (existing == null)
        {
            throw new ServiceException("检验任务不存在");
        }
        if (!"0".equals(existing.getTaskStatus()))
        {
            throw new ServiceException("只有待检状态的任务才能开始检验");
        }
        QmsInspTask update = new QmsInspTask();
        update.setTaskId(taskId);
        update.setTaskStatus("1");
        update.setInspectTime(new Date());
        return qmsInspTaskMapper.updateInspTask(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int voidTask(Long taskId, String reason, String voidType)
    {
        QmsInspTask existing = qmsInspTaskMapper.selectInspTaskById(taskId);
        if (existing == null)
        {
            throw new ServiceException("检验任务不存在");
        }
        if ("2".equals(existing.getTaskStatus()))
        {
            throw new ServiceException("已判定的检验任务不可作废");
        }
        QmsInspTask update = new QmsInspTask();
        update.setTaskId(taskId);
        update.setTaskStatus("3");
        update.setRemark(buildVoidRemark(reason, voidType));
        return qmsInspTaskMapper.updateInspTask(update);
    }

    /**
     * 根据检验类型获取默认检验水平
     * OQC（出货检验）默认使用 S-4（特殊检验水平，样本量小）
     * IQC（来料检验）默认使用 II（一般检验水平）
     * IPQC（过程检验）默认使用 II
     * FQC（成品检验）默认使用 II
     */
    private String getDefaultInspectMethod(String taskType)
    {
        if ("OQC".equals(taskType))
        {
            return "S-4";
        }
        return "II";
    }

    private String buildVoidRemark(String reason, String voidType)
    {
        String typeLabel = switchVoidTypeLabel(voidType);
        StringBuilder sb = new StringBuilder();
        if (typeLabel != null)
        {
            sb.append("作废类型：").append(typeLabel).append("；");
        }
        sb.append("作废原因：").append(reason != null ? reason : "-");
        return sb.toString();
    }

    private String switchVoidTypeLabel(String voidType)
    {
        if (voidType == null || voidType.isEmpty())
        {
            return null;
        }
        switch (voidType)
        {
            case "1": return "重复创建";
            case "2": return "信息填写错误";
            case "3": return "物料/批次变更";
            case "4": return "客户取消订单";
            case "5": return "检验计划取消";
            case "9": return "其他";
            default: return "其他";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int assignInspector(Long[] taskIds, Long inspectorId, String inspectorName)
    {
        int count = 0;
        for (Long taskId : taskIds)
        {
            QmsInspTask existing = qmsInspTaskMapper.selectInspTaskById(taskId);
            if (existing == null)
            {
                continue;
            }
            if ("2".equals(existing.getTaskStatus()) || "3".equals(existing.getTaskStatus()))
            {
                throw new ServiceException("任务" + existing.getTaskNo() + "已判定或已作废，不可分配检验员");
            }
            QmsInspTask update = new QmsInspTask();
            update.setTaskId(taskId);
            update.setInspectorId(inspectorId);
            update.setInspectorName(inspectorName);
            qmsInspTaskMapper.updateInspTask(update);
            count++;
        }
        return count;
    }

    @Override
    public Map<String, Integer> selectStatusCounts()
    {
        List<Map<String, Object>> rawList = qmsInspTaskMapper.selectStatusCounts();
        Map<String, Integer> result = new HashMap<>();
        result.put("all", 0);
        result.put("0", 0); // 待检
        result.put("1", 0); // 检验中
        result.put("2", 0); // 已判定
        result.put("3", 0); // 已作废
        int total = 0;
        for (Map<String, Object> row : rawList)
        {
            String status = String.valueOf(row.get("status"));
            int cnt = ((Number) row.get("cnt")).intValue();
            result.put(status, cnt);
            total += cnt;
        }
        result.put("all", total);
        return result;
    }

    /**
     * 检验不合格时自动创建NCR（不合格品报告）
     */
    private void createNcrFromInspection(QmsInspTask existing, QmsInspTask inspTask, List<QmsInspItem> items, String highestDefectLevel)
    {
        QmsNcr ncr = new QmsNcr();
        ncr.setSourceType("inspection");
        ncr.setSourceId(inspTask.getTaskId());
        // 来源单号取库中任务的taskNo：判定请求体仅含taskId/itemList，inspTask.taskNo恒为空
        ncr.setSourceNo(existing.getTaskNo());
        ncr.setTaskId(inspTask.getTaskId());
        ncr.setMaterialId(existing.getMaterialId());
        ncr.setMaterialCode(existing.getMaterialCode());
        ncr.setMaterialName(existing.getMaterialName());
        ncr.setSupplierId(existing.getSupplierId());
        ncr.setSupplierName(existing.getSupplierName());
        ncr.setBatchNo(existing.getBatchNo());
        ncr.setDefectLevel(highestDefectLevel);
        // 构建缺陷描述
        StringBuilder defectDesc = new StringBuilder();
        if (items != null)
        {
            for (QmsInspItem item : items)
            {
                if ("2".equals(item.getItemResult()))
                {
                    if (defectDesc.length() > 0) defectDesc.append("；");
                    defectDesc.append(item.getStdName());
                    if (StringUtils.isNotEmpty(item.getDefectName()))
                    {
                        defectDesc.append("：").append(item.getDefectName());
                    }
                }
            }
        }
        ncr.setDefectDesc(defectDesc.toString());
        // 不合格数量 = 样本中不合格项的缺陷数量之和（与批量判定逻辑保持一致）
        int defectCount = 0;
        if (items != null)
        {
            for (QmsInspItem item : items)
            {
                if ("2".equals(item.getItemResult()))
                {
                    // 使用缺陷数量字段，默认为1（兼容旧数据）
                    int qty = item.getDefectQty() != null ? item.getDefectQty() : 1;
                    defectCount += qty;
                }
            }
        }
        ncr.setDefectQty(new BigDecimal(defectCount));
        ncr.setNcrStatus("0"); // 已登记
        ncr.setIsolateFlag("0");
        ncr.setDiscovererId(inspTask.getInspectorId());
        ncr.setDiscovererName(inspTask.getInspectorName());
        ncr.setDiscoverTime(new Date());
        ncr.setRemark("检验任务" + inspTask.getTaskNo() + "判定不合格，系统自动创建");
        qmsNcrService.insertNcr(ncr);
    }

    @Override
    public QmsInspTask getReportData(Long taskId)
    {
        QmsInspTask task = selectInspTaskById(taskId);
        if (task != null)
        {
            // 加载电子签名记录
            List<QmsEsigRecord> esigList = qmsEsigRecordService.selectEsigRecordByBiz("insp_judge", taskId);
            task.setEsigList(esigList);
        }
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveInspectDraft(QmsInspTask inspTask)
    {
        QmsInspTask existing = qmsInspTaskMapper.selectInspTaskById(inspTask.getTaskId());
        if (existing == null)
        {
            throw new ServiceException("检验任务不存在");
        }
        if ("2".equals(existing.getTaskStatus()))
        {
            throw new ServiceException("该检验任务已判定，不可修改");
        }
        if ("3".equals(existing.getTaskStatus()))
        {
            throw new ServiceException("该检验任务已作废，不可修改");
        }
        // 先删除旧明细
        qmsInspItemMapper.deleteInspItemByTaskId(inspTask.getTaskId());
        // 批量保存检验明细（不做判定，不改变任务状态）
        List<QmsInspItem> items = inspTask.getItemList();
        if (items != null && !items.isEmpty())
        {
            for (QmsInspItem item : items)
            {
                item.setTaskId(inspTask.getTaskId());
            }
            qmsInspItemMapper.batchInsertInspItem(items);
        }
        // 如果当前是待检状态，切换为检验中
        if ("0".equals(existing.getTaskStatus()))
        {
            QmsInspTask update = new QmsInspTask();
            update.setTaskId(inspTask.getTaskId());
            update.setTaskStatus("1");
            update.setInspectTime(new Date());
            return qmsInspTaskMapper.updateInspTask(update);
        }
        return 1;
    }
}

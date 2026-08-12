package com.ruoyi.qms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
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
import com.ruoyi.qms.mapper.QmsInspTaskMapper;
import com.ruoyi.qms.mapper.QmsInspItemMapper;
import com.ruoyi.qms.mapper.QmsMaterialAttrMapper;
import com.ruoyi.qms.service.IQmsInspTaskService;
import com.ruoyi.qms.service.IQmsEsigRecordService;
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
            inspTask.setTaskNo(mkNumberRuleService.generateNumber("qms_insp_task"));
        }
        // 自动计算AQL抽样数
        if (inspTask.getInspectQty() != null && StringUtils.isNotEmpty(inspTask.getAqlLevel()))
        {
            int[] result = AqlCalculator.calculate(inspTask.getInspectQty(), inspTask.getAqlLevel());
            if (result != null)
            {
                inspTask.setSampleSize(result[0]);
                inspTask.setAcVal(result[1]);
                inspTask.setReVal(result[2]);
            }
        }
        if (inspTask.getInspectLevel() == null)
        {
            inspTask.setInspectLevel("1");
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
        // 自动判定
        int defectCount = 0;
        String highestDefectLevel = "4"; // 默认轻微
        if (items != null)
        {
            for (QmsInspItem item : items)
            {
                if ("2".equals(item.getItemResult()))
                {
                    defectCount++;
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
            task.setInspectLevel("1");
            task.setTaskStatus("0");
            task.setIsRecheck("0");
            task.setDelFlag("0");
            task.setStatus("0");
            task.setRemark("系统自动生成IQC检验任务");
            task.setCreateBy("system");
            // 自动计算AQL抽样数
            int[] aqlResult = AqlCalculator.calculate(task.getInspectQty(), task.getAqlLevel());
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
    public QmsInspTask getReportData(Long taskId)
    {
        QmsInspTask task = selectInspTaskById(taskId);
        if (task != null)
        {
            // 加载电子签名记录
            List<QmsEsigRecord> esigList = qmsEsigRecordService.selectEsigRecordByBiz("insp_judge", taskId);
            // 设置到非数据库字段（需要扩展domain）
            // task.setEsigList(esigList);
        }
        return task;
    }
}

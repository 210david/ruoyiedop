package com.ruoyi.qms.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsNcr;
import com.ruoyi.qms.domain.QmsNcrApproveLog;
import com.ruoyi.qms.domain.QmsEsigRecord;
import com.ruoyi.qms.mapper.QmsNcrMapper;
import com.ruoyi.qms.mapper.QmsNcrApproveLogMapper;
import com.ruoyi.qms.service.IQmsNcrService;
import com.ruoyi.qms.service.IQmsEsigRecordService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 不合格品报告 Service实现
 *
 * MRB审批流程（轻量级工作流，无需Flowable引擎）：
 *
 * 致命/严重缺陷（level 1/2）：
 *   已登记 → [提交评审] → 节点1:质量经理审批 → 节点2:责任部门主管审批 → 处置中 → 待验证 → 已关闭
 *
 * 一般/轻微缺陷（level 3/4）：
 *   已登记 → [提交评审] → 节点1:QE审批 → 处置中 → 待验证 → 已关闭
 *
 * @author ruoyi
 */
@Service
public class QmsNcrServiceImpl implements IQmsNcrService
{
    @Autowired
    private QmsNcrMapper qmsNcrMapper;

    @Autowired
    private QmsNcrApproveLogMapper qmsNcrApproveLogMapper;

    @Autowired
    private IQmsEsigRecordService qmsEsigRecordService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<QmsNcr> selectNcrList(QmsNcr ncr)
    {
        return qmsNcrMapper.selectNcrList(ncr);
    }

    @Override
    public QmsNcr selectNcrById(Long ncrId)
    {
        QmsNcr ncr = qmsNcrMapper.selectNcrById(ncrId);
        if (ncr != null)
        {
            List<QmsNcrApproveLog> logList = qmsNcrApproveLogMapper.selectApproveLogByNcrId(ncrId);
            ncr.setApproveLogList(logList);
        }
        return ncr;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertNcr(QmsNcr ncr)
    {
        if (StringUtils.isEmpty(ncr.getNcrNo()))
        {
            ncr.setNcrNo(mkNumberRuleService.generateNumber("qms_ncr"));
        }
        ncr.setDelFlag("0");
        ncr.setStatus("0");
        if (ncr.getNcrStatus() == null)
        {
            ncr.setNcrStatus("0");
        }
        if (ncr.getIsolateFlag() == null)
        {
            ncr.setIsolateFlag("0");
        }
        if (ncr.getDiscoverTime() == null)
        {
            ncr.setDiscoverTime(new Date());
        }
        ncr.setCurrentNode(0);
        ncr.setMaxNode(0);
        return qmsNcrMapper.insertNcr(ncr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateNcr(QmsNcr ncr)
    {
        return qmsNcrMapper.updateNcr(ncr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteNcrByIds(Long[] ncrIds)
    {
        for (Long ncrId : ncrIds)
        {
            qmsNcrApproveLogMapper.deleteApproveLogByNcrId(ncrId);
        }
        return qmsNcrMapper.deleteNcrByIds(ncrIds);
    }

    /**
     * 提交MRB评审
     * 根据缺陷等级确定审批路径：
     * - 致命/严重（1/2）：2个审批节点（质量经理 → 责任部门主管）
     * - 一般/轻微（3/4）：1个审批节点（QE）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitReview(Long ncrId)
    {
        QmsNcr existing = qmsNcrMapper.selectNcrById(ncrId);
        if (existing == null)
        {
            throw new ServiceException("NCR不存在");
        }
        if (!"0".equals(existing.getNcrStatus()))
        {
            throw new ServiceException("只有已登记状态的NCR才能提交评审");
        }
        // 根据缺陷等级确定审批节点数
        boolean isCritical = "1".equals(existing.getDefectLevel()) || "2".equals(existing.getDefectLevel());
        int maxNode = isCritical ? 2 : 1;
        String firstNodeName = isCritical ? "质量经理审批" : "QE审批";
        String firstNodeRole = isCritical ? "ROLE_QM_MANAGER" : "ROLE_QE";

        QmsNcr update = new QmsNcr();
        update.setNcrId(ncrId);
        update.setNcrStatus("1");
        update.setCurrentNode(1);
        update.setMaxNode(maxNode);
        qmsNcrMapper.updateNcr(update);

        // 写入审批日志
        QmsNcrApproveLog log = new QmsNcrApproveLog();
        log.setNcrId(ncrId);
        log.setNodeSeq(1);
        log.setNodeName(firstNodeName);
        log.setApproverRole(firstNodeRole);
        log.setActionType("1");
        log.setApproveOpinion("提交MRB评审");
        log.setApproverName(SecurityUtils.getUsername());
        log.setApproveTime(new Date());
        log.setCreateBy(SecurityUtils.getUsername());
        qmsNcrApproveLogMapper.insertApproveLog(log);
        return 1;
    }

    /**
     * 审批通过
     * 推进到下一节点；若已到达最大节点，则进入处置中状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveNode(Long ncrId, String approveOpinion)
    {
        QmsNcr existing = qmsNcrMapper.selectNcrById(ncrId);
        if (existing == null)
        {
            throw new ServiceException("NCR不存在");
        }
        if (!"1".equals(existing.getNcrStatus()))
        {
            throw new ServiceException("当前状态不允许审批操作");
        }
        int currentNode = existing.getCurrentNode() == null ? 0 : existing.getCurrentNode();
        int maxNode = existing.getMaxNode() == null ? 1 : existing.getMaxNode();

        // 确定当前节点名称
        String nodeName = getNodeName(currentNode, existing.getDefectLevel());
        String nodeRole = getNodeRole(currentNode, existing.getDefectLevel());

        // 写入审批日志
        QmsNcrApproveLog log = new QmsNcrApproveLog();
        log.setNcrId(ncrId);
        log.setNodeSeq(currentNode);
        log.setNodeName(nodeName);
        log.setApproverRole(nodeRole);
        log.setActionType("2");
        log.setApproveOpinion(approveOpinion);
        log.setApproverName(SecurityUtils.getUsername());
        log.setApproveTime(new Date());
        log.setCreateBy(SecurityUtils.getUsername());
        qmsNcrApproveLogMapper.insertApproveLog(log);

        // 保存电子签名记录（合规行业要求）
        QmsEsigRecord esig = new QmsEsigRecord();
        esig.setBizType("ncr_approve");
        esig.setBizId(ncrId);
        esig.setBizNo(existing.getNcrNo());
        esig.setSigMeaning(nodeName + "审批通过");
        esig.setSigOpinion(approveOpinion);
        esig.setSigResult("pass");
        qmsEsigRecordService.saveEsigRecord(esig);

        QmsNcr update = new QmsNcr();
        update.setNcrId(ncrId);
        if (currentNode >= maxNode)
        {
            // 所有审批节点通过，进入处置中
            update.setNcrStatus("2");
            update.setCurrentNode(maxNode + 1);
        }
        else
        {
            // 推进到下一节点
            update.setCurrentNode(currentNode + 1);
        }
        return qmsNcrMapper.updateNcr(update);
    }

    /**
     * 审批驳回
     * 退回到已登记状态，允许修改后重新提交
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectNode(Long ncrId, String approveOpinion)
    {
        QmsNcr existing = qmsNcrMapper.selectNcrById(ncrId);
        if (existing == null)
        {
            throw new ServiceException("NCR不存在");
        }
        if (!"1".equals(existing.getNcrStatus()))
        {
            throw new ServiceException("当前状态不允许驳回操作");
        }
        int currentNode = existing.getCurrentNode() == null ? 0 : existing.getCurrentNode();
        String nodeName = getNodeName(currentNode, existing.getDefectLevel());
        String nodeRole = getNodeRole(currentNode, existing.getDefectLevel());

        // 写入审批日志
        QmsNcrApproveLog log = new QmsNcrApproveLog();
        log.setNcrId(ncrId);
        log.setNodeSeq(currentNode);
        log.setNodeName(nodeName);
        log.setApproverRole(nodeRole);
        log.setActionType("3");
        log.setApproveOpinion(approveOpinion);
        log.setApproverName(SecurityUtils.getUsername());
        log.setApproveTime(new Date());
        log.setCreateBy(SecurityUtils.getUsername());
        qmsNcrApproveLogMapper.insertApproveLog(log);

        // 退回到已登记
        QmsNcr update = new QmsNcr();
        update.setNcrId(ncrId);
        update.setNcrStatus("0");
        update.setCurrentNode(0);
        return qmsNcrMapper.updateNcr(update);
    }

    /**
     * 提交处置结论
     * 处置中(2) → 待验证(3)，记录处置方式
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitDisposition(QmsNcr ncr)
    {
        QmsNcr existing = qmsNcrMapper.selectNcrById(ncr.getNcrId());
        if (existing == null)
        {
            throw new ServiceException("NCR不存在");
        }
        if (!"2".equals(existing.getNcrStatus()))
        {
            throw new ServiceException("只有处置中状态的NCR才能提交处置结论");
        }
        if (StringUtils.isEmpty(ncr.getDisposition()))
        {
            throw new ServiceException("请选择处置方式");
        }

        // 写入审批日志
        QmsNcrApproveLog log = new QmsNcrApproveLog();
        log.setNcrId(ncr.getNcrId());
        log.setNodeSeq(existing.getMaxNode() + 1);
        log.setNodeName("处置执行");
        log.setActionType("4");
        log.setApproveOpinion("处置方式：" + getDispositionText(ncr.getDisposition()) + "，" + (ncr.getRemark() != null ? ncr.getRemark() : ""));
        log.setApproverName(SecurityUtils.getUsername());
        log.setApproveTime(new Date());
        log.setCreateBy(SecurityUtils.getUsername());
        qmsNcrApproveLogMapper.insertApproveLog(log);

        // 更新NCR状态
        ncr.setNcrStatus("3");
        ncr.setCurrentNode(existing.getMaxNode() + 2);
        return qmsNcrMapper.updateNcr(ncr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int verifyNcr(QmsNcr ncr)
    {
        QmsNcr existing = qmsNcrMapper.selectNcrById(ncr.getNcrId());
        if (existing == null)
        {
            throw new ServiceException("NCR不存在");
        }
        if (!"3".equals(existing.getNcrStatus()))
        {
            throw new ServiceException("当前状态不允许验证操作");
        }

        // 写入审批日志
        String actionType = "1".equals(ncr.getVerifyResult()) ? "5" : "6";
        QmsNcrApproveLog log = new QmsNcrApproveLog();
        log.setNcrId(ncr.getNcrId());
        log.setNodeSeq(existing.getMaxNode() + 2);
        log.setNodeName("QE验证");
        log.setActionType(actionType);
        log.setApproveOpinion(ncr.getVerifyRemark());
        log.setApproverName(SecurityUtils.getUsername());
        log.setApproveTime(new Date());
        log.setCreateBy(SecurityUtils.getUsername());
        qmsNcrApproveLogMapper.insertApproveLog(log);

        ncr.setNcrStatus("1".equals(ncr.getVerifyResult()) ? "4" : "2");
        if ("4".equals(ncr.getNcrStatus()))
        {
            ncr.setCloseTime(new Date());
            ncr.setIsolateFlag("0");
        }
        ncr.setVerifyTime(new Date());
        return qmsNcrMapper.updateNcr(ncr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeNcr(Long ncrId)
    {
        QmsNcr ncr = new QmsNcr();
        ncr.setNcrId(ncrId);
        ncr.setNcrStatus("4");
        ncr.setCloseTime(new Date());
        ncr.setIsolateFlag("0");
        return qmsNcrMapper.updateNcr(ncr);
    }

    /**
     * 根据节点序号和缺陷等级获取节点名称
     */
    private String getNodeName(int nodeSeq, String defectLevel)
    {
        boolean isCritical = "1".equals(defectLevel) || "2".equals(defectLevel);
        if (isCritical)
        {
            if (nodeSeq == 1) return "质量经理审批";
            if (nodeSeq == 2) return "责任部门主管审批";
        }
        else
        {
            if (nodeSeq == 1) return "QE审批";
        }
        return "审批节点" + nodeSeq;
    }

    /**
     * 根据节点序号和缺陷等级获取审批角色
     */
    private String getNodeRole(int nodeSeq, String defectLevel)
    {
        boolean isCritical = "1".equals(defectLevel) || "2".equals(defectLevel);
        if (isCritical)
        {
            if (nodeSeq == 1) return "ROLE_QM_MANAGER";
            if (nodeSeq == 2) return "ROLE_PROD";
        }
        else
        {
            if (nodeSeq == 1) return "ROLE_QE";
        }
        return "ROLE_QE";
    }

    private String getDispositionText(String disposition)
    {
        switch (disposition)
        {
            case "1": return "退货";
            case "2": return "挑选使用";
            case "3": return "返工/返修";
            case "4": return "报废";
            case "5": return "让步接收";
            default: return disposition;
        }
    }

    /**
     * 生成NCR编号（使用编号规则服务）
     */
    private String generateNcrNo()
    {
        return mkNumberRuleService.generateNumber("qms_ncr");
    }
}

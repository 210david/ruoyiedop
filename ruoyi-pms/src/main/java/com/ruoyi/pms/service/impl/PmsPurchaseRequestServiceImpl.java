package com.ruoyi.pms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pms.domain.PmsPurchaseRequest;
import com.ruoyi.pms.domain.PmsPurchaseRequestDetail;
import com.ruoyi.pms.domain.PmsRequestAuditLog;
import com.ruoyi.pms.mapper.PmsPurchaseRequestMapper;
import com.ruoyi.pms.mapper.PmsRequestAuditLogMapper;
import com.ruoyi.pms.service.IPmsPurchaseRequestService;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.system.utils.MessageHelper;

/**
 * 采购申请 Service实现
 *
 * @author ruoyi
 */
@Service
public class PmsPurchaseRequestServiceImpl implements IPmsPurchaseRequestService
{
    @Autowired
    private PmsPurchaseRequestMapper pmsPurchaseRequestMapper;

    @Autowired
    private PmsRequestAuditLogMapper pmsRequestAuditLogMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private MessageHelper messageHelper;

    @Override
    public List<PmsPurchaseRequest> selectPurchaseRequestList(PmsPurchaseRequest request)
    {
        return pmsPurchaseRequestMapper.selectPurchaseRequestList(request);
    }

    @Override
    public PmsPurchaseRequest selectPurchaseRequestById(Long requestId)
    {
        PmsPurchaseRequest request = pmsPurchaseRequestMapper.selectPurchaseRequestById(requestId);
        if (request != null)
        {
            request.setDetailList(pmsPurchaseRequestMapper.selectRequestDetailByRequestId(requestId));
            request.setAuditLogList(pmsRequestAuditLogMapper.selectAuditLogByRequestId(requestId));
        }
        return request;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPurchaseRequest(PmsPurchaseRequest request)
    {
        if (StringUtils.isEmpty(request.getRequestNo()))
        {
            request.setRequestNo(mkNumberRuleService.generateNumber("pms_request"));
        }
        request.setDelFlag("0");
        if (request.getStatus() == null)
        {
            request.setStatus("0");
        }
        // 计算预估总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (request.getDetailList() != null)
        {
            for (PmsPurchaseRequestDetail d : request.getDetailList())
            {
                if (d.getEstimatedAmount() != null)
                {
                    totalAmount = totalAmount.add(d.getEstimatedAmount());
                }
                else if (d.getQty() != null && d.getEstimatedPrice() != null)
                {
                    d.setEstimatedAmount(d.getQty().multiply(d.getEstimatedPrice()));
                    totalAmount = totalAmount.add(d.getEstimatedAmount());
                }
                d.setDelFlag("0");
            }
        }
        request.setTotalAmount(totalAmount);
        int rows = pmsPurchaseRequestMapper.insertPurchaseRequest(request);
        // 插入明细
        if (request.getDetailList() != null)
        {
            for (PmsPurchaseRequestDetail d : request.getDetailList())
            {
                d.setRequestId(request.getRequestId());
                d.setDelFlag("0");
                pmsPurchaseRequestMapper.insertRequestDetail(d);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePurchaseRequest(PmsPurchaseRequest request)
    {
        // 驳回状态修改后重置为待审批，清空主表审批信息（审核日志保留）
        PmsPurchaseRequest existing = pmsPurchaseRequestMapper.selectPurchaseRequestById(request.getRequestId());
        if (existing != null && "3".equals(existing.getStatus()))
        {
            request.setStatus("1");
            request.setAuditBy(null);
            request.setAuditTime(null);
            request.setAuditOpinion(null);
        }
        // 删除旧明细，重新插入
        pmsPurchaseRequestMapper.deleteRequestDetailByRequestId(request.getRequestId());
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (request.getDetailList() != null)
        {
            for (PmsPurchaseRequestDetail d : request.getDetailList())
            {
                d.setRequestId(request.getRequestId());
                if (d.getEstimatedAmount() != null)
                {
                    totalAmount = totalAmount.add(d.getEstimatedAmount());
                }
                else if (d.getQty() != null && d.getEstimatedPrice() != null)
                {
                    d.setEstimatedAmount(d.getQty().multiply(d.getEstimatedPrice()));
                    totalAmount = totalAmount.add(d.getEstimatedAmount());
                }
                d.setDelFlag("0");
                pmsPurchaseRequestMapper.insertRequestDetail(d);
            }
        }
        request.setTotalAmount(totalAmount);
        return pmsPurchaseRequestMapper.updatePurchaseRequest(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePurchaseRequestByIds(Long[] requestIds)
    {
        return pmsPurchaseRequestMapper.deletePurchaseRequestByIds(requestIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitPurchaseRequest(Long requestId)
    {
        PmsPurchaseRequest request = pmsPurchaseRequestMapper.selectPurchaseRequestById(requestId);
        if (request == null)
        {
            throw new ServiceException("采购申请不存在");
        }
        if (!"0".equals(request.getStatus()))
        {
            throw new ServiceException("只有草稿状态的采购申请才能提交审批");
        }
        // 校验明细不能为空
        List<PmsPurchaseRequestDetail> details = pmsPurchaseRequestMapper.selectRequestDetailByRequestId(requestId);
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("采购申请明细不能为空，无法提交");
        }
        request.setStatus("1");
        int rows = pmsPurchaseRequestMapper.updatePurchaseRequest(request);

        // 发送消息给有审批权限的角色
        String content = "申请部门：" + (request.getDeptName() != null ? request.getDeptName() : "-");
        messageHelper.sendMessage(
            "采购申请" + request.getRequestNo() + "待审批",
            content,
            "3",   // 审批消息
            "2",   // 重要
            "pms",
            requestId,
            "/pms/demand/request?id=" + requestId,
            "pms:request:approve",
"1",   // bizStatus: 待审批
"采购申请"  // bizEntryName
);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditPurchaseRequest(Long requestId, String status, String auditOpinion)
    {
        PmsPurchaseRequest request = pmsPurchaseRequestMapper.selectPurchaseRequestById(requestId);
        if (request == null)
        {
            throw new ServiceException("采购申请不存在");
        }
        if (!"0".equals(request.getStatus()) && !"1".equals(request.getStatus()))
        {
            throw new ServiceException("只有草稿或待审批状态的采购申请才能审批");
        }
        request.setStatus(status);
        request.setAuditBy(SecurityUtils.getUsername());
        request.setAuditTime(new Date());
        request.setAuditOpinion(auditOpinion);
        int rows = pmsPurchaseRequestMapper.updatePurchaseRequest(request);

        // 写入审核日志（永久留存）
        PmsRequestAuditLog auditLog = new PmsRequestAuditLog();
        auditLog.setRequestId(requestId);
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditRemark(auditOpinion);
        // auditAction: 1=通过 2=驳回（与前端时间线模板一致）
        if ("2".equals(status)) {
            auditLog.setAuditAction("1");
        } else {
            auditLog.setAuditAction("2");
        }
        pmsRequestAuditLogMapper.insertAuditLog(auditLog);

        // 发送审批结果消息
        String resultText = "2".equals(status) ? "已审批通过，请生成采购订单" : "被驳回，原因：" + (auditOpinion != null ? auditOpinion : "");
        String recipientRole = "2".equals(status) ? "pms:order:add" : "pms:request:edit";
        messageHelper.sendMessage(
            "采购申请" + request.getRequestNo() + resultText,
            "审批结果：" + resultText,
            "1",   // 系统通知
            "2".equals(status) ? "1" : "2",  // 通过=普通，驳回=重要
            "pms",
            requestId,
            "/pms/demand/request?id=" + requestId,
recipientRole,
            "采购申请"  // bizEntryName
);

        return rows;
    }
}

package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkContract;
import com.ruoyi.mk.domain.MkContractApproveLog;
import com.ruoyi.mk.domain.MkContractChangeLog;
import com.ruoyi.mk.domain.MkPaymentPlan;
import com.ruoyi.mk.mapper.MkContractApproveLogMapper;
import com.ruoyi.mk.mapper.MkContractChangeLogMapper;
import com.ruoyi.mk.mapper.MkContractMapper;
import com.ruoyi.mk.mapper.MkPaymentPlanMapper;
import com.ruoyi.mk.service.IMkContractService;
import com.ruoyi.mk.service.IMkContractChangeService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 合同 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkContractServiceImpl implements IMkContractService
{
    private static final Logger log = LoggerFactory.getLogger(MkContractServiceImpl.class);

    @Autowired
    private MkContractMapper mkContractMapper;

    @Autowired
    private MkPaymentPlanMapper mkPaymentPlanMapper;

    @Autowired
    private MkContractApproveLogMapper mkContractApproveLogMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private MkContractChangeLogMapper mkContractChangeLogMapper;

    @Override
    public List<MkContract> selectContractList(MkContract contract)
    {
        return mkContractMapper.selectContractList(contract);
    }

    @Override
    public MkContract selectContractById(Long contractId)
    {
        MkContract contract = mkContractMapper.selectContractById(contractId);
        if (contract != null)
        {
            List<MkPaymentPlan> paymentPlanList = mkPaymentPlanMapper.selectPaymentPlanByContractId(contractId);
            contract.setPaymentPlanList(paymentPlanList);
            List<MkContractApproveLog> approveLogList = mkContractApproveLogMapper.selectApproveLogByContractId(contractId);
            contract.setApproveLogList(approveLogList);
            List<MkContractChangeLog> changeLogs = mkContractChangeLogMapper.selectChangeLogByContractId(contractId);
            contract.setChangeLogs(changeLogs);
        }
        return contract;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertContract(MkContract contract)
    {
        contract.setDelFlag("0");
        if (contract.getContractStatus() == null)
        {
            contract.setContractStatus("0");
        }
        if (contract.getContractType() == null)
        {
            contract.setContractType("0");
        }
        if (contract.getPaymentMethod() == null)
        {
            contract.setPaymentMethod("0");
        }
        // 自动生成合同编号
        if (StringUtils.isEmpty(contract.getContractNo()))
        {
            contract.setContractNo(mkNumberRuleService.generateNumber("contract"));
        }
        int rows = mkContractMapper.insertContract(contract);
        // 插入回款计划
        if (contract.getPaymentPlanList() != null)
        {
            for (MkPaymentPlan plan : contract.getPaymentPlanList())
            {
                plan.setContractId(contract.getContractId());
                plan.setCustomerId(contract.getCustomerId());
                if (plan.getPaymentStatus() == null)
                {
                    plan.setPaymentStatus("0");
                }
                if (plan.getActualAmount() == null)
                {
                    plan.setActualAmount(BigDecimal.ZERO);
                }
                if (plan.getOverdueFlag() == null)
                {
                    plan.setOverdueFlag("0");
                }
                mkPaymentPlanMapper.insertPaymentPlan(plan);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateContract(MkContract contract)
    {
        // 校验：非草稿状态的合同不允许修改核心信息
        MkContract existing = mkContractMapper.selectContractById(contract.getContractId());
        if (existing != null && !"0".equals(existing.getContractStatus()))
        {
            throw new ServiceException("非草稿状态的合同不允许修改，请先驳回至草稿状态");
        }
        // 删除旧回款计划，重新插入
        mkPaymentPlanMapper.deletePaymentPlanByContractId(contract.getContractId());
        if (contract.getPaymentPlanList() != null)
        {
            for (MkPaymentPlan plan : contract.getPaymentPlanList())
            {
                plan.setContractId(contract.getContractId());
                plan.setCustomerId(contract.getCustomerId());
                if (plan.getPaymentStatus() == null)
                {
                    plan.setPaymentStatus("0");
                }
                if (plan.getActualAmount() == null)
                {
                    plan.setActualAmount(BigDecimal.ZERO);
                }
                if (plan.getOverdueFlag() == null)
                {
                    plan.setOverdueFlag("0");
                }
                mkPaymentPlanMapper.insertPaymentPlan(plan);
            }
        }
        return mkContractMapper.updateContract(contract);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteContractByIds(Long[] contractIds)
    {
        // 校验：非草稿状态的合同不允许删除
        for (Long contractId : contractIds)
        {
            MkContract contract = mkContractMapper.selectContractById(contractId);
            if (contract != null && !"0".equals(contract.getContractStatus()))
            {
                throw new ServiceException("合同【" + contract.getContractNo() + "】非草稿状态，不允许删除");
            }
        }
        // 删除关联回款计划和审批记录
        for (Long contractId : contractIds)
        {
            mkPaymentPlanMapper.deletePaymentPlanByContractId(contractId);
            mkContractApproveLogMapper.deleteApproveLogByContractId(contractId);
        }
        return mkContractMapper.deleteContractByIds(contractIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitApproval(Long contractId)
    {
        MkContract contract = mkContractMapper.selectContractById(contractId);
        if (contract == null)
        {
            throw new ServiceException("合同不存在");
        }
        if (!"0".equals(contract.getContractStatus()))
        {
            throw new ServiceException("只有草稿状态的合同才能提交审批");
        }
        mkContractMapper.updateContractStatus(contractId, "1");
        // 写入审批记录
        MkContractApproveLog approveLog = new MkContractApproveLog();
        approveLog.setContractId(contractId);
        approveLog.setActionType("1");
        approveLog.setApproveOpinion("提交审批");
        approveLog.setApproveBy(SecurityUtils.getUsername());
        mkContractApproveLogMapper.insertApproveLog(approveLog);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveContract(Long contractId, String approveOpinion)
    {
        MkContract contract = mkContractMapper.selectContractById(contractId);
        if (contract == null)
        {
            throw new ServiceException("合同不存在");
        }
        if (!"1".equals(contract.getContractStatus()))
        {
            throw new ServiceException("只有审批中状态的合同才能审批通过");
        }
        mkContractMapper.updateContractStatus(contractId, "2");
        // 写入审批记录
        MkContractApproveLog approveLog = new MkContractApproveLog();
        approveLog.setContractId(contractId);
        approveLog.setActionType("2");
        approveLog.setApproveOpinion(approveOpinion);
        approveLog.setApproveBy(SecurityUtils.getUsername());
        mkContractApproveLogMapper.insertApproveLog(approveLog);
        // 合同审批通过后，自动初始化回款计划到回款管理
        autoCreatePaymentPlansFromContract(contractId);
        log.info("合同[{}]审批通过，已自动初始化回款计划到回款管理", contract.getContractNo());
        return 1;
    }

    /**
     * 合同审批通过后，自动从合同回款计划初始化回款管理数据
     * 确保每个回款计划都有正确的初始状态（实际回款金额、逾期标志、回款状态）
     */
    private void autoCreatePaymentPlansFromContract(Long contractId)
    {
        List<MkPaymentPlan> planList = mkPaymentPlanMapper.selectPaymentPlanByContractId(contractId);
        if (planList == null || planList.isEmpty())
        {
            log.warn("合同[{}]审批通过，但未配置回款计划", contractId);
            return;
        }
        for (MkPaymentPlan plan : planList)
        {
            // 初始化回款计划的默认值，确保回款管理中数据完整
            if (plan.getActualAmount() == null)
            {
                plan.setActualAmount(BigDecimal.ZERO);
            }
            if (plan.getOverdueFlag() == null)
            {
                plan.setOverdueFlag("0");
            }
            if (plan.getPaymentStatus() == null)
            {
                plan.setPaymentStatus("0"); // 待回款
            }
            mkPaymentPlanMapper.updatePaymentPlan(plan);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectContract(Long contractId, String approveOpinion)
    {
        MkContract contract = mkContractMapper.selectContractById(contractId);
        if (contract == null)
        {
            throw new ServiceException("合同不存在");
        }
        if (!"1".equals(contract.getContractStatus()))
        {
            throw new ServiceException("只有审批中状态的合同才能驳回");
        }
        mkContractMapper.updateContractStatus(contractId, "0");
        // 写入审批记录
        MkContractApproveLog approveLog = new MkContractApproveLog();
        approveLog.setContractId(contractId);
        approveLog.setActionType("3");
        approveLog.setApproveOpinion(approveOpinion);
        approveLog.setApproveBy(SecurityUtils.getUsername());
        mkContractApproveLogMapper.insertApproveLog(approveLog);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int terminateContract(Long contractId, String terminateReason)
    {
        MkContract contract = mkContractMapper.selectContractById(contractId);
        if (contract == null)
        {
            throw new ServiceException("合同不存在");
        }
        if (!"2".equals(contract.getContractStatus()))
        {
            throw new ServiceException("只有已生效的合同才能终止");
        }
        MkContract update = new MkContract();
        update.setContractId(contractId);
        update.setContractStatus("4");
        mkContractMapper.updateContract(update);
        // 写入审批记录
        MkContractApproveLog approveLog = new MkContractApproveLog();
        approveLog.setContractId(contractId);
        approveLog.setActionType("4");
        approveLog.setApproveOpinion(terminateReason);
        approveLog.setApproveBy(SecurityUtils.getUsername());
        mkContractApproveLogMapper.insertApproveLog(approveLog);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkExpiredContracts()
    {
        List<MkContract> expiredList = mkContractMapper.selectExpiredContracts();
        int count = 0;
        for (MkContract contract : expiredList)
        {
            count += mkContractMapper.updateContractStatus(contract.getContractId(), "3");
        }
        return count;
    }

    @Override
    public List<MkContract> selectExpiringContracts(int days)
    {
        return mkContractMapper.selectExpiringContracts(days);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int renewContract(Long contractId, com.ruoyi.mk.domain.dto.ContractRenewDTO renewDTO)
    {
        MkContract contract = mkContractMapper.selectContractById(contractId);
        if (contract == null)
        {
            throw new ServiceException("合同不存在");
        }
        if (!"2".equals(contract.getContractStatus()) && !"3".equals(contract.getContractStatus()))
        {
            throw new ServiceException("只有已生效或已过期的合同才能续签");
        }
        // 创建续签合同（新合同，关联原合同）
        MkContract newContract = new MkContract();
        newContract.setContractNo(mkNumberRuleService.generateNumber("contract"));
        // 合同名称：优先使用用户输入的，否则自动加(续签)后缀
        String newName = renewDTO.getNewContractName();
        if (newName != null && !newName.trim().isEmpty())
        {
            newContract.setContractName(newName.trim());
        }
        else
        {
            newContract.setContractName(contract.getContractName() + "(续签)");
        }
        newContract.setContractType("1"); // 续签
        newContract.setCustomerId(contract.getCustomerId());
        newContract.setCustomerName(contract.getCustomerName());
        newContract.setOpportunityId(contract.getOpportunityId());
        // 合同金额：优先使用用户输入的
        newContract.setContractAmount(renewDTO.getNewAmount() != null ? renewDTO.getNewAmount() : contract.getContractAmount());
        newContract.setSignDate(new java.util.Date());
        // 生效日期：优先使用用户选择的，否则使用原合同到期日期
        newContract.setEffectiveDate(renewDTO.getNewEffectiveDate() != null ? renewDTO.getNewEffectiveDate() : contract.getExpireDate());
        // 到期日期：使用用户选择的
        newContract.setExpireDate(renewDTO.getNewExpireDate());
        // 付款方式：优先使用用户选择的，否则使用原合同
        newContract.setPaymentMethod(renewDTO.getNewPaymentMethod() != null ? renewDTO.getNewPaymentMethod() : contract.getPaymentMethod());
        newContract.setContractStatus("0"); // 草稿
        newContract.setUserId(contract.getUserId());
        newContract.setUserName(contract.getUserName());
        newContract.setDeptId(contract.getDeptId());
        newContract.setDeptName(contract.getDeptName());
        // 附件：使用用户上传的新附件
        newContract.setAttachment(renewDTO.getAttachment());
        newContract.setDelFlag("0");
        newContract.setRemark(renewDTO.getRemark() != null ? renewDTO.getRemark() : "续签合同");
        return mkContractMapper.insertContract(newContract);
    }

    @Override
    public List<MkContractChangeLog> selectContractChangeLogs(Long contractId)
    {
        return mkContractChangeLogMapper.selectChangeLogByContractId(contractId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitContractChange(MkContractChangeLog changeLog)
    {
        if (changeLog.getChangeStatus() == null)
        {
            changeLog.setChangeStatus("0");
        }
        MkContract contract = mkContractMapper.selectContractById(changeLog.getContractId());
        if (contract == null)
        {
            throw new ServiceException("合同不存在");
        }
        if (!"2".equals(contract.getContractStatus()))
        {
            throw new ServiceException("只有已生效的合同才能申请变更");
        }
        changeLog.setContractNo(contract.getContractNo());
        changeLog.setCreateBy(SecurityUtils.getUsername());
        int rows = mkContractChangeLogMapper.insertChangeLog(changeLog);
        // 将合同状态改为"变更审批中"
        mkContractMapper.updateContractStatus(changeLog.getContractId(), "5");
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitContractChanges(List<MkContractChangeLog> changeLogs)
    {
        if (changeLogs == null || changeLogs.isEmpty())
        {
            throw new ServiceException("变更内容不能为空");
        }
        Long contractId = changeLogs.get(0).getContractId();
        MkContract contract = mkContractMapper.selectContractById(contractId);
        if (contract == null)
        {
            throw new ServiceException("合同不存在");
        }
        if (!"2".equals(contract.getContractStatus()))
        {
            throw new ServiceException("只有已生效的合同才能申请变更");
        }
        String username = SecurityUtils.getUsername();
        String contractNo = contract.getContractNo();
        int rows = 0;
        for (MkContractChangeLog log : changeLogs)
        {
            if (log.getChangeStatus() == null)
            {
                log.setChangeStatus("0");
            }
            log.setContractId(contractId);
            log.setContractNo(contractNo);
            log.setCreateBy(username);
            rows += mkContractChangeLogMapper.insertChangeLog(log);
        }
        // 将合同状态改为"变更审批中"
        mkContractMapper.updateContractStatus(contractId, "5");
        return rows;
    }
}

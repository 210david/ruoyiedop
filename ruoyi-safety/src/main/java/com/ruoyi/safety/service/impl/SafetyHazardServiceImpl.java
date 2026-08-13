package com.ruoyi.safety.service.impl;
import java.util.HashMap;


import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.safety.domain.SafetyHazard;
import com.ruoyi.safety.domain.SafetyHazardAuditLog;
import com.ruoyi.safety.mapper.SafetyHazardMapper;
import com.ruoyi.safety.mapper.SafetyHazardAuditLogMapper;
import com.ruoyi.safety.service.ISafetyHazardService;

@Service
@Component
public class SafetyHazardServiceImpl implements ISafetyHazardService
{
    @Autowired
    private SafetyHazardMapper safetyHazardMapper;

    @Autowired
    private SafetyHazardAuditLogMapper safetyHazardAuditLogMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public SafetyHazard selectSafetyHazardById(Long hazardId)
    {
        SafetyHazard hazard = safetyHazardMapper.selectSafetyHazardById(hazardId);
        if (hazard != null)
        {
            hazard.setAuditLogList(safetyHazardAuditLogMapper.selectAuditLogByHazardId(hazardId));
        }
        return hazard;
    }

    @Override
    public List<SafetyHazard> selectSafetyHazardList(SafetyHazard safetyHazard)
    {
        return safetyHazardMapper.selectSafetyHazardList(safetyHazard);
    }

    @Override
    public int insertSafetyHazard(SafetyHazard safetyHazard)
    {
        if (StringUtils.isEmpty(safetyHazard.getHazardCode()))
        {
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(safetyHazard.getHazardLevel())) { params.put("hazardLevel", safetyHazard.getHazardLevel()); }
        if (StringUtils.isNotEmpty(safetyHazard.getHazardType())) { params.put("hazardType", safetyHazard.getHazardType()); }
        safetyHazard.setHazardCode(mkNumberRuleService.generateNumber("safety_hazard", params));
        }
        if (safetyHazard.getHazardStatus() == null)
        {
            safetyHazard.setHazardStatus("0");
        }
        if (safetyHazard.getDiscoverTime() == null)
        {
            safetyHazard.setDiscoverTime(DateUtils.getNowDate());
        }
        return safetyHazardMapper.insertSafetyHazard(safetyHazard);
    }

    @Override
    public int updateSafetyHazard(SafetyHazard safetyHazard)
    {
        return safetyHazardMapper.updateSafetyHazard(safetyHazard);
    }

    @Override
    public int deleteSafetyHazardByIds(Long[] hazardIds)
    {
        return safetyHazardMapper.deleteSafetyHazardByIds(hazardIds);
    }

    @Override
    public int deleteSafetyHazardById(Long hazardId)
    {
        return safetyHazardMapper.deleteSafetyHazardById(hazardId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitHazard(Long hazardId)
    {
        SafetyHazard hazard = safetyHazardMapper.selectSafetyHazardById(hazardId);
        if (hazard == null)
        {
            throw new ServiceException("隐患不存在");
        }
        // 草稿或审批驳回状态均可提交
        if (!"0".equals(hazard.getHazardStatus()) && !"2".equals(hazard.getHazardStatus()))
        {
            throw new ServiceException("只有草稿或审批驳回状态的隐患才能提交");
        }
        // 提交前校验必填字段
        if (StringUtils.isEmpty(hazard.getHazardDesc()))
        {
            throw new ServiceException("隐患描述不能为空，请完善后提交");
        }
        if (StringUtils.isEmpty(hazard.getHazardType()))
        {
            throw new ServiceException("隐患类型不能为空，请完善后提交");
        }
        if (StringUtils.isEmpty(hazard.getHazardLevel()))
        {
            throw new ServiceException("隐患等级不能为空，请完善后提交");
        }
        if (hazard.getPersonId() == null)
        {
            throw new ServiceException("责任人不能为空，请完善后提交");
        }
        if (StringUtils.isEmpty(hazard.getRectifyReq()))
        {
            throw new ServiceException("整改要求不能为空，请完善后提交");
        }
        if (hazard.getRectifyDeadline() == null)
        {
            throw new ServiceException("整改期限不能为空，请完善后提交");
        }
        SafetyHazard update = new SafetyHazard();
        update.setHazardId(hazardId);
        update.setHazardStatus("1");
        update.setUpdateBy(SecurityUtils.getUsername());
        int rows = safetyHazardMapper.updateHazardStatus(update);

        // 写入审批日志（提交审核）
        SafetyHazardAuditLog auditLog = new SafetyHazardAuditLog();
        auditLog.setHazardId(hazardId);
        auditLog.setAuditAction("3");
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditRemark("提交隐患审批");
        safetyHazardAuditLogMapper.insertAuditLog(auditLog);

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveHazard(SafetyHazard safetyHazard)
    {
        SafetyHazard hazard = safetyHazardMapper.selectSafetyHazardById(safetyHazard.getHazardId());
        if (hazard == null)
        {
            throw new ServiceException("隐患不存在");
        }
        if (!"1".equals(hazard.getHazardStatus()))
        {
            throw new ServiceException("只有待审批状态的隐患才能审批");
        }
        // 审批结果校验：必须为"1"(通过)或"2"(不通过)
        if (StringUtils.isEmpty(safetyHazard.getApproveResult()))
        {
            throw new ServiceException("审批结果不能为空");
        }
        if (!"1".equals(safetyHazard.getApproveResult()) && !"2".equals(safetyHazard.getApproveResult()))
        {
            throw new ServiceException("审批结果值非法");
        }
        // 审批意见校验：驳回时必须填写审批意见
        if ("2".equals(safetyHazard.getApproveResult()) && StringUtils.isEmpty(safetyHazard.getApproveComment()))
        {
            throw new ServiceException("审批不通过时必须填写审批意见");
        }
        // 自动填充审批人信息（从当前登录用户获取，防止前端伪造）
        Long currentUserId = SecurityUtils.getUserId();
        String currentUserName = SecurityUtils.getLoginUser().getUser().getNickName();
        // 审批回避校验：审批人不得为隐患发现人
        if (hazard.getDiscoverPersonId() != null && currentUserId.equals(hazard.getDiscoverPersonId()))
        {
            throw new ServiceException("审批人不得为隐患发现人，请由其他人员审批");
        }
        safetyHazard.setApprovePersonId(currentUserId);
        safetyHazard.setApprovePerson(currentUserName);
        safetyHazard.setUpdateBy(SecurityUtils.getUsername());
        if (safetyHazard.getApproveTime() == null)
        {
            safetyHazard.setApproveTime(DateUtils.getNowDate());
        }
        if ("1".equals(safetyHazard.getApproveResult()))
        {
            // 审批通过 → 待整改
            safetyHazard.setHazardStatus("3");
        }
        else
        {
            // 审批不通过 → 审批驳回
            safetyHazard.setHazardStatus("2");
        }
        int rows = safetyHazardMapper.updateSafetyHazard(safetyHazard);

        // 写入审批日志（永久留存）
        SafetyHazardAuditLog auditLog = new SafetyHazardAuditLog();
        auditLog.setHazardId(safetyHazard.getHazardId());
        auditLog.setAuditBy(currentUserName);
        auditLog.setAuditAction(safetyHazard.getApproveResult()); // 1=通过 2=驳回
        auditLog.setAuditRemark(safetyHazard.getApproveComment());
        safetyHazardAuditLogMapper.insertAuditLog(auditLog);

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int startRectify(Long hazardId)
    {
        SafetyHazard hazard = safetyHazardMapper.selectSafetyHazardById(hazardId);
        if (hazard == null)
        {
            throw new ServiceException("隐患不存在");
        }
        if (!"3".equals(hazard.getHazardStatus()) && !"7".equals(hazard.getHazardStatus()))
        {
            throw new ServiceException("只有待整改或超期未整改状态的隐患才能开始整改");
        }
        SafetyHazard update = new SafetyHazard();
        update.setHazardId(hazardId);
        update.setHazardStatus("4");
        update.setUpdateBy(SecurityUtils.getUsername());
        int rows = safetyHazardMapper.updateHazardStatus(update);

        // 写入审批日志（开始整改）
        SafetyHazardAuditLog auditLog = new SafetyHazardAuditLog();
        auditLog.setHazardId(hazardId);
        auditLog.setAuditAction("4");
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditRemark("开始整改");
        safetyHazardAuditLogMapper.insertAuditLog(auditLog);

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitRectify(SafetyHazard safetyHazard)
    {
        SafetyHazard hazard = safetyHazardMapper.selectSafetyHazardById(safetyHazard.getHazardId());
        if (hazard == null)
        {
            throw new ServiceException("隐患不存在");
        }
        // 待整改或整改中状态均可提交整改反馈
        if (!"3".equals(hazard.getHazardStatus()) && !"4".equals(hazard.getHazardStatus()))
        {
            throw new ServiceException("只有待整改或整改中状态的隐患才能提交整改反馈");
        }
        // 整改说明必填校验
        if (StringUtils.isEmpty(safetyHazard.getRectifyDesc()))
        {
            throw new ServiceException("整改说明不能为空");
        }
        safetyHazard.setHazardStatus("5");
        safetyHazard.setUpdateBy(SecurityUtils.getUsername());
        if (safetyHazard.getRectifyTime() == null)
        {
            safetyHazard.setRectifyTime(DateUtils.getNowDate());
        }
        int rows = safetyHazardMapper.updateSafetyHazard(safetyHazard);

        // 写入审批日志（整改提交）
        SafetyHazardAuditLog auditLog = new SafetyHazardAuditLog();
        auditLog.setHazardId(safetyHazard.getHazardId());
        auditLog.setAuditAction("5");
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditRemark("整改完成：" + (safetyHazard.getRectifyDesc() != null ? safetyHazard.getRectifyDesc() : ""));
        safetyHazardAuditLogMapper.insertAuditLog(auditLog);

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int verifyHazard(SafetyHazard safetyHazard)
    {
        SafetyHazard hazard = safetyHazardMapper.selectSafetyHazardById(safetyHazard.getHazardId());
        if (hazard == null)
        {
            throw new ServiceException("隐患不存在");
        }
        if (!"5".equals(hazard.getHazardStatus()))
        {
            throw new ServiceException("只有待验收状态的隐患才能验收");
        }
        // 验收结果校验：必须为"1"(通过)或"2"(不通过)
        if (StringUtils.isEmpty(safetyHazard.getVerifyResult()))
        {
            throw new ServiceException("验收结果不能为空");
        }
        if (!"1".equals(safetyHazard.getVerifyResult()) && !"2".equals(safetyHazard.getVerifyResult()))
        {
            throw new ServiceException("验收结果值非法");
        }
        // 验收意见校验：不通过时必须填写验收意见
        if ("2".equals(safetyHazard.getVerifyResult()) && StringUtils.isEmpty(safetyHazard.getVerifyComment()))
        {
            throw new ServiceException("验收不通过时必须填写验收意见");
        }
        // 自动填充验收人信息（从当前登录用户获取）
        Long currentUserId = SecurityUtils.getUserId();
        String currentUserName = SecurityUtils.getLoginUser().getUser().getNickName();
        // 验收回避校验：验收人不得为整改责任人
        if (hazard.getPersonId() != null && currentUserId.equals(hazard.getPersonId()))
        {
            throw new ServiceException("验收人不得为整改责任人，请由其他人员验收");
        }
        safetyHazard.setVerifyPersonId(currentUserId);
        safetyHazard.setVerifyPerson(currentUserName);
        safetyHazard.setUpdateBy(SecurityUtils.getUsername());
        if (safetyHazard.getVerifyTime() == null)
        {
            safetyHazard.setVerifyTime(DateUtils.getNowDate());
        }
        if ("1".equals(safetyHazard.getVerifyResult()))
        {
            // 验收通过 → 已闭环
            safetyHazard.setHazardStatus("6");
        }
        else
        {
            // 验收不通过 → 退回整改中
            safetyHazard.setHazardStatus("4");
        }
        int rows = safetyHazardMapper.updateSafetyHazard(safetyHazard);

        // 写入审批日志（验收结果）
        SafetyHazardAuditLog auditLog = new SafetyHazardAuditLog();
        auditLog.setHazardId(safetyHazard.getHazardId());
        auditLog.setAuditBy(currentUserName);
        auditLog.setAuditAction("1".equals(safetyHazard.getVerifyResult()) ? "6" : "7");
        auditLog.setAuditRemark("验收" + ("1".equals(safetyHazard.getVerifyResult()) ? "通过" : "不通过") + (safetyHazard.getVerifyComment() != null && !safetyHazard.getVerifyComment().isEmpty() ? " - " + safetyHazard.getVerifyComment() : ""));
        safetyHazardAuditLogMapper.insertAuditLog(auditLog);

        return rows;
    }

    @Override
    public int countHazardsByStatus(String hazardStatus)
    {
        return safetyHazardMapper.countHazardsByStatus(hazardStatus);
    }

    @Override
    public List<Map<String, Object>> getHazardTrendByMonth()
    {
        return safetyHazardMapper.selectHazardTrendByMonth();
    }

    @Override
    public List<Map<String, Object>> getHazardCountByType()
    {
        return safetyHazardMapper.selectHazardCountByType();
    }

    @Override
    public List<Map<String, Object>> getHazardCountByLevel()
    {
        return safetyHazardMapper.selectHazardCountByLevel();
    }

    @Override
    public List<Map<String, Object>> getHazardCountBySource()
    {
        return safetyHazardMapper.selectHazardCountBySource();
    }

    @Override
    public List<Map<String, Object>> getHazardCountByDept()
    {
        return safetyHazardMapper.selectHazardCountByDept();
    }
}
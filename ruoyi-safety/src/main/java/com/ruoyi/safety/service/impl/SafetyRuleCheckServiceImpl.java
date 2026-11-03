package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.safety.domain.SafetyRuleCheck;
import com.ruoyi.safety.mapper.SafetyRuleCheckMapper;
import com.ruoyi.safety.service.ISafetyRuleCheckService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 安全守则执行检查 Service业务层处理（需求一）
 *
 * 业务规则：
 * SR-04 检查结果“不符合”项必须填写整改责任人、期限与整改结果，闭环前不允许归档
 */
@Service
public class SafetyRuleCheckServiceImpl implements ISafetyRuleCheckService {
    @Autowired
    private SafetyRuleCheckMapper mapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<SafetyRuleCheck> selectCheckList(SafetyRuleCheck check) { return mapper.selectCheckList(check); }

    @Override
    public SafetyRuleCheck selectCheckById(Long checkId) { return mapper.selectCheckById(checkId); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCheck(SafetyRuleCheck check) {
        // SR-04：不符合项必须填写整改责任人、期限
        if ("2".equals(check.getCheckResult())) {
            if (StringUtils.isEmpty(check.getRectifyUser())) throw new ServiceException("不符合项必须填写整改责任人（SR-04）");
            if (check.getRectifyDeadline() == null) throw new ServiceException("不符合项必须填写整改期限（SR-04）");
        }
        if (StringUtils.isEmpty(check.getCheckNo())) {
            try {
                check.setCheckNo(mkNumberRuleService.generateNumber("biz_safety_rule_check"));
            } catch (Exception e) {
                check.setCheckNo("SRC" + DateUtils.dateTimeNow("yyyyMMddHHmmssSSS"));
            }
        }
        check.setCheckStatus("2".equals(check.getCheckResult()) ? "0" : "2");
        check.setDelFlag("0");
        check.setCreateBy(SecurityUtils.getUsername());
        return mapper.insertCheck(check);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCheck(SafetyRuleCheck check) {
        SafetyRuleCheck existing = mapper.selectCheckById(check.getCheckId());
        if (existing == null) throw new ServiceException("检查记录不存在");
        // SR-04：不符合项必须填整改责任人/期限/结果，闭环前不允许归档
        if ("2".equals(check.getCheckResult())) {
            if (StringUtils.isEmpty(check.getRectifyUser())) throw new ServiceException("不符合项必须填写整改责任人（SR-04）");
            if (check.getRectifyDeadline() == null) throw new ServiceException("不符合项必须填写整改期限（SR-04）");
        }
        if ("3".equals(existing.getCheckStatus())) throw new ServiceException("已归档的检查记录不允许修改");
        check.setUpdateBy(SecurityUtils.getUsername());
        return mapper.updateCheck(check);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reviewCheck(SafetyRuleCheck check) {
        if (check.getCheckId() == null) throw new ServiceException("检查记录不能为空");
        SafetyRuleCheck existing = mapper.selectCheckById(check.getCheckId());
        if (existing == null) throw new ServiceException("检查记录不存在");
        // SR-04：不符合项必须填整改结果，闭环前不允许归档
        if ("2".equals(existing.getCheckResult())) {
            if (StringUtils.isEmpty(check.getRectifyResult())) throw new ServiceException("不符合项必须填写整改结果后才能闭环（SR-04）");
        }
        SafetyRuleCheck update = new SafetyRuleCheck();
        update.setCheckId(check.getCheckId());
        update.setCheckStatus("2");
        update.setReviewer(SecurityUtils.getUsername());
        update.setReviewTime(DateUtils.getNowDate());
        update.setRectifyResult(check.getRectifyResult());
        update.setUpdateBy(SecurityUtils.getUsername());
        return mapper.updateCheck(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int archiveCheck(Long checkId) {
        SafetyRuleCheck existing = mapper.selectCheckById(checkId);
        if (existing == null) throw new ServiceException("检查记录不存在");
        // SR-04：不符合项必须整改闭环后才能归档
        if ("2".equals(existing.getCheckResult()) && !"2".equals(existing.getCheckStatus())) {
            throw new ServiceException("不符合项必须整改闭环后才能归档（SR-04）");
        }
        SafetyRuleCheck update = new SafetyRuleCheck();
        update.setCheckId(checkId);
        update.setCheckStatus("3");
        update.setUpdateBy(SecurityUtils.getUsername());
        return mapper.updateCheck(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCheckByIds(Long[] checkIds) { return mapper.deleteCheckByIds(checkIds); }

    @Override
    public int countChecks() { return mapper.countChecks(); }

    @Override
    public int countQualified() { return mapper.countQualified(); }
}

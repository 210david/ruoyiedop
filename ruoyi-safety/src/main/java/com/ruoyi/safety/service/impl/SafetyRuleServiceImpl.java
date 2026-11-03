package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.safety.domain.SafetyRule;
import com.ruoyi.safety.domain.SafetyRuleVersion;
import com.ruoyi.safety.mapper.SafetyRuleMapper;
import com.ruoyi.safety.service.ISafetyRuleService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 安全守则 Service业务层处理（需求一）
 *
 * 业务规则：
 * SR-01 守则关联“安全生产制度名称+附件”为选填弱关联（YHNY 无独立制度模块，评审决策口径）
 * SR-02 生效守则不允许删除，只可“废止”并留痕（版本快照留痕）
 */
@Service
public class SafetyRuleServiceImpl implements ISafetyRuleService {
    @Autowired
    private SafetyRuleMapper mapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<SafetyRule> selectRuleList(SafetyRule rule) { return mapper.selectRuleList(rule); }

    @Override
    public SafetyRule selectRuleById(Long ruleId) { return mapper.selectRuleById(ruleId); }

    @Override
    public int insertRule(SafetyRule rule) {
        // 守则编号：空则生成（沿用安全模块 generateNumber 约定；未配置编号规则时退化为日期+序号）
        if (StringUtils.isEmpty(rule.getRuleNo())) {
            // 编号生成失败不影响主流程（在事务外尝试，避免内部事务异常导致外层 rollback-only）
            try {
                rule.setRuleNo(mkNumberRuleService.generateNumber("biz_safety_rule"));
            } catch (Exception e) {
                rule.setRuleNo("AQGZ-" + DateUtils.dateTimeNow("yyyy") + "-" + DateUtils.dateTimeNow("HHmmssSSS"));
            }
        }
        if (rule.getVersionNo() == null) rule.setVersionNo(1);
        if (rule.getRuleStatus() == null) rule.setRuleStatus("0");
        rule.setCreateBy(SecurityUtils.getUsername());
        return mapper.insertRule(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRule(SafetyRule rule) {
        SafetyRule existing = mapper.selectRuleById(rule.getRuleId());
        if (existing == null) throw new ServiceException("守则不存在");
        // SR-02：生效守则不允许删除，只可废止并留痕（版本快照留痕）
        if ("1".equals(existing.getRuleStatus()) && "2".equals(rule.getRuleStatus())) {
            throw new ServiceException("生效守则不允许直接删除，请使用「废止」并留痕（SR-02）");
        }
        // 新版本快照：内容/附件等变化时生成新版本，保留历史版本可查看、回溯
        boolean contentChanged = !StringUtils.equals(existing.getRuleName(), rule.getRuleName())
                || !StringUtils.equals(existing.getRuleContent(), rule.getRuleContent())
                || !StringUtils.equals(existing.getAttachment(), rule.getAttachment())
                || !StringUtils.equals(existing.getDeptName(), rule.getDeptName())
                || !StringUtils.equals(existing.getPostName(), rule.getPostName())
                || !StringUtils.equals(existing.getWorkScene(), rule.getWorkScene())
                || !StringUtils.equals(existing.getRegulationName(), rule.getRegulationName())
                || !StringUtils.equals(existing.getRegulationAttachment(), rule.getRegulationAttachment());
        int rows = mapper.updateRule(rule);
        if (rows > 0 && contentChanged) {
            Integer versionNo = existing.getVersionNo() == null ? 1 : existing.getVersionNo();
            insertVersionSnapshot(existing, versionNo + 1, rule, "内容/附件等变化，自动生成新版本");
            rule.setVersionNo(versionNo + 1);
        }
        return rows;
    }

    /** 版本快照：新增历史版本 */
    private void insertVersionSnapshot(SafetyRule existing, Integer versionNo, SafetyRule rule, String changeNote) {
        SafetyRuleVersion v = new SafetyRuleVersion();
        v.setRuleId(existing.getRuleId());
        v.setRuleNo(existing.getRuleNo());
        v.setVersionNo(versionNo);
        v.setRuleName(existing.getRuleName());
        v.setDeptName(existing.getDeptName());
        v.setPostName(existing.getPostName());
        v.setWorkScene(existing.getWorkScene());
        v.setRuleContent(existing.getRuleContent());
        v.setAttachment(existing.getAttachment());
        v.setRegulationName(existing.getRegulationName());
        v.setEffectiveDate(existing.getEffectiveDate());
        v.setExpireDate(existing.getExpireDate());
        v.setRuleStatus(existing.getRuleStatus());
        v.setChangeNote(changeNote);
        v.setChangeBy(SecurityUtils.getUsername());
        v.setChangeTime(DateUtils.getNowDate());
        mapper.insertRuleVersion(v);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publishRule(Long ruleId) {
        SafetyRule existing = mapper.selectRuleById(ruleId);
        if (existing == null) throw new ServiceException("守则不存在");
        if ("1".equals(existing.getRuleStatus())) return 0;
        SafetyRule update = new SafetyRule();
        update.setRuleId(ruleId);
        update.setRuleStatus("1");
        if (update.getEffectiveDate() == null) update.setEffectiveDate(DateUtils.getNowDate());
        // 版本快照：发布生成新版本留痕
        Integer versionNo = existing.getVersionNo() == null ? 1 : existing.getVersionNo();
        insertVersionSnapshot(existing, versionNo + 1, update, "守则发布，自动生成新版本");
        update.setVersionNo(versionNo + 1);
        return mapper.updateRule(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int abolishRule(Long ruleId) {
        SafetyRule existing = mapper.selectRuleById(ruleId);
        if (existing == null) throw new ServiceException("守则不存在");
        // SR-02：生效守则不允许删除，只可废止并留痕（版本快照留痕）
        SafetyRule update = new SafetyRule();
        update.setRuleId(ruleId);
        update.setRuleStatus("2");
        return mapper.updateRule(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRuleByIds(Long[] ruleIds) {
        int rows = 0;
        for (Long ruleId : ruleIds) {
            SafetyRule existing = mapper.selectRuleById(ruleId);
            // SR-02：生效守则不允许删除，只可“废止”并留痕
            if (existing != null && "1".equals(existing.getRuleStatus())) {
                throw new ServiceException("生效守则不允许删除，请使用「废止」并留痕（SR-02）");
            }
            rows += mapper.deleteRuleByIds(new Long[]{ ruleId });
        }
        return rows;
    }

    @Override
    public List<SafetyRuleVersion> selectRuleVersionList(Long ruleId) { return mapper.selectRuleVersionList(ruleId); }

    @Override
    public int countLearnTotalByRule(Long ruleId) { return mapper.countLearnTotalByRule(ruleId); }

    @Override
    public int countLearnSignedByRule(Long ruleId) { return mapper.countLearnSignedByRule(ruleId); }
}

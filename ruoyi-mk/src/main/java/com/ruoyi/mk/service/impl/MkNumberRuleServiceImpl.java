package com.ruoyi.mk.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkNumberRule;
import com.ruoyi.mk.domain.MkNumberRulePrefix;
import com.ruoyi.mk.mapper.MkNumberRuleMapper;
import com.ruoyi.mk.mapper.MkNumberRulePrefixMapper;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 编号规则 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkNumberRuleServiceImpl implements IMkNumberRuleService
{
    @Autowired
    private MkNumberRuleMapper mkNumberRuleMapper;

    @Autowired
    private MkNumberRulePrefixMapper mkNumberRulePrefixMapper;

    @Override
    public List<MkNumberRule> selectRuleList(MkNumberRule rule)
    {
        List<MkNumberRule> list = mkNumberRuleMapper.selectRuleList(rule);
        // 为每条规则生成预览编号
        for (MkNumberRule r : list)
        {
            r.setPreview(previewNumber(r));
        }
        return list;
    }

    @Override
    public MkNumberRule selectRuleById(Long ruleId)
    {
        MkNumberRule rule = mkNumberRuleMapper.selectRuleById(ruleId);
        if (rule != null)
        {
            // 先加载动态前缀映射子表，再生成预览（预览需要用到前缀映射数据）
            rule.setPrefixList(mkNumberRulePrefixMapper.selectPrefixListByRuleId(ruleId));
            rule.setPreview(previewNumber(rule));
        }
        return rule;
    }

    @Override
    public MkNumberRule selectRuleByCode(String ruleCode)
    {
        return mkNumberRuleMapper.selectRuleByCode(ruleCode);
    }

    @Override
    public int insertRule(MkNumberRule rule)
    {
        if (StringUtils.isEmpty(rule.getRuleCode()))
        {
            throw new ServiceException("规则编码不能为空");
        }
        if (!checkRuleCodeUnique(rule))
        {
            throw new ServiceException("规则编码'" + rule.getRuleCode() + "'已存在");
        }
        validateDynamicPrefixConfig(rule);
        // 设置默认值
        if (rule.getStatus() == null)
        {
            rule.setStatus("0");
        }
        if (rule.getResetType() == null)
        {
            rule.setResetType("0");
        }
        if (rule.getSeqLength() == null)
        {
            rule.setSeqLength(4);
        }
        if (rule.getSeqStart() == null)
        {
            rule.setSeqStart(1L);
        }
        if (rule.getStep() == null)
        {
            rule.setStep(1);
        }
        if (rule.getCurrentSeq() == null)
        {
            rule.setCurrentSeq(0L);
        }
        if (rule.getPrefix() == null)
        {
            rule.setPrefix("");
        }
        if (rule.getDateFormat() == null)
        {
            rule.setDateFormat("");
        }
        if (rule.getConnector() == null)
        {
            rule.setConnector("");
        }
        if (rule.getPrefixFieldEnabled() == null)
        {
            rule.setPrefixFieldEnabled("0");
        }
        rule.setCurrentDateStr("");
        int rows = mkNumberRuleMapper.insertRule(rule);
        // 保存动态前缀映射子表
        if (rows > 0 && "1".equals(rule.getPrefixFieldEnabled()) && rule.getPrefixList() != null)
        {
            for (MkNumberRulePrefix prefix : rule.getPrefixList())
            {
                prefix.setRuleId(rule.getRuleId());
                if (prefix.getEnabled() == null)
                {
                    prefix.setEnabled("1");
                }
                if (StringUtils.isEmpty(prefix.getCreateBy()))
                {
                    prefix.setCreateBy(rule.getCreateBy());
                }
                mkNumberRulePrefixMapper.insertPrefix(prefix);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRule(MkNumberRule rule)
    {
        if (StringUtils.isNotEmpty(rule.getRuleCode()))
        {
            MkNumberRule existing = mkNumberRuleMapper.selectRuleById(rule.getRuleId());
            if (existing != null && !existing.getRuleCode().equals(rule.getRuleCode()))
            {
                if (!checkRuleCodeUnique(rule))
                {
                    throw new ServiceException("规则编码'" + rule.getRuleCode() + "'已存在");
                }
            }
        }
        validateDynamicPrefixConfig(rule);
        int rows = mkNumberRuleMapper.updateRule(rule);
        // 更新动态前缀映射子表（先删后增）
        if (rows > 0 && rule.getPrefixList() != null)
        {
            mkNumberRulePrefixMapper.deletePrefixByRuleId(rule.getRuleId());
            for (MkNumberRulePrefix prefix : rule.getPrefixList())
            {
                prefix.setRuleId(rule.getRuleId());
                if (prefix.getEnabled() == null)
                {
                    prefix.setEnabled("1");
                }
                if (StringUtils.isEmpty(prefix.getCreateBy()))
                {
                    prefix.setCreateBy(rule.getUpdateBy());
                }
                mkNumberRulePrefixMapper.insertPrefix(prefix);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRuleByIds(Long[] ruleIds)
    {
        for (Long ruleId : ruleIds)
        {
            mkNumberRulePrefixMapper.deletePrefixByRuleId(ruleId);
        }
        return mkNumberRuleMapper.deleteRuleByIds(ruleIds);
    }

    /**
     * 校验：按动态前缀重置时必须启用动态前缀并配置前缀映射
     */
    private void validateDynamicPrefixConfig(MkNumberRule rule)
    {
        if ("4".equals(rule.getResetType()))
        {
            if (!"1".equals(rule.getPrefixFieldEnabled()))
            {
                throw new ServiceException("序号重置类型为'按动态前缀'时，必须启用动态前缀配置");
            }
            if (StringUtils.isEmpty(rule.getPrefixField()))
            {
                throw new ServiceException("按动态前缀重置时，必须选择关联字段名");
            }
            if (rule.getPrefixList() == null || rule.getPrefixList().isEmpty())
            {
                throw new ServiceException("按动态前缀重置时，至少需要配置一个前缀映射");
            }
        }
    }

    @Override
    public boolean checkRuleCodeUnique(MkNumberRule rule)
    {
        Long ruleId = rule.getRuleId() == null ? -1L : rule.getRuleId();
        MkNumberRule info = mkNumberRuleMapper.checkRuleCodeUnique(rule.getRuleCode());
        if (info != null && info.getRuleId().longValue() != ruleId.longValue())
        {
            return false;
        }
        return true;
    }

    /**
     * 根据规则编码生成下一个编号（线程安全，使用数据库行锁）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateNumber(String ruleCode)
    {
        return generateNumber(ruleCode, null);
    }

    /**
     * 根据规则编码生成下一个编号（支持动态前缀，线程安全）
     *
     * @param ruleCode 规则编码
     * @param params   业务参数（如 materialType=0），用于动态前缀查找
     * @return 生成的编号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateNumber(String ruleCode, Map<String, String> params)
    {
        // 使用 SELECT ... FOR UPDATE 加行锁，保证并发安全
        MkNumberRule rule = mkNumberRuleMapper.selectRuleForUpdate(ruleCode);
        if (rule == null)
        {
            throw new ServiceException("编号规则'" + ruleCode + "'不存在");
        }
        if ("1".equals(rule.getStatus()))
        {
            throw new ServiceException("编号规则'" + rule.getRuleName() + "'已停用");
        }

        // 确定固定前缀和动态前缀
        String fixedPrefix = rule.getPrefix();
        String dynamicPrefix = "";
        MkNumberRulePrefix prefixMapping = null;
        if ("1".equals(rule.getPrefixFieldEnabled()) && params != null && StringUtils.isNotEmpty(rule.getPrefixField()))
        {
            String fieldValue = params.get(rule.getPrefixField());
            if (StringUtils.isNotEmpty(fieldValue))
            {
                // 按动态前缀重置时使用 FOR UPDATE 锁行，否则普通查询
                if ("4".equals(rule.getResetType()))
                {
                    prefixMapping = mkNumberRulePrefixMapper.selectEnabledPrefixForUpdate(rule.getRuleId(), fieldValue);
                }
                else
                {
                    prefixMapping = mkNumberRulePrefixMapper.selectEnabledPrefix(rule.getRuleId(), fieldValue);
                }
                if (prefixMapping != null && StringUtils.isNotEmpty(prefixMapping.getPrefix()))
                {
                    dynamicPrefix = prefixMapping.getPrefix();
                }
            }
        }

        // 计算当前日期串
        String todayDateStr = buildDateStr(rule.getDateFormat(), rule.getResetType());

        // ===== 序列号计算 =====
        // 按动态前缀重置：每个动态前缀维护独立的序号
        // 其他重置类型：使用主规则的序号
        long newSeq;
        if ("4".equals(rule.getResetType()) && prefixMapping != null)
        {
            // 按动态前缀重置：序号存储在 mk_number_rule_prefix 表中
            boolean needReset = false;
            // 如果配置了日期格式，还需判断日期是否变化
            if (StringUtils.isNotEmpty(rule.getDateFormat()))
            {
                if (!todayDateStr.equals(prefixMapping.getCurrentDateStr()))
                {
                    needReset = true;
                }
            }
            if (needReset || prefixMapping.getCurrentSeq() == null || prefixMapping.getCurrentSeq() == 0)
            {
                newSeq = rule.getSeqStart();
            }
            else
            {
                newSeq = prefixMapping.getCurrentSeq() + rule.getStep();
            }

            // 拼装编号
            String number = buildNumber(rule, fixedPrefix, dynamicPrefix, todayDateStr, newSeq);

            // 更新前缀映射表的序号和日期串
            prefixMapping.setCurrentSeq(newSeq);
            prefixMapping.setCurrentDateStr(todayDateStr);
            mkNumberRulePrefixMapper.updatePrefixSeq(prefixMapping);

            return number;
        }
        else
        {
            // 非按动态前缀重置：序号存储在 mk_number_rule 表中
            boolean needReset = false;
            if (StringUtils.isNotEmpty(rule.getDateFormat()))
            {
                if (!todayDateStr.equals(rule.getCurrentDateStr()))
                {
                    needReset = true;
                }
            }

            if (needReset)
            {
                newSeq = rule.getSeqStart();
            }
            else
            {
                if (rule.getCurrentSeq() == null || rule.getCurrentSeq() == 0)
                {
                    newSeq = rule.getSeqStart();
                }
                else
                {
                    newSeq = rule.getCurrentSeq() + rule.getStep();
                }
            }

            // 拼装编号（固定前缀 + 连接符 + 动态前缀 + 日期 + 序列号）
            String number = buildNumber(rule, fixedPrefix, dynamicPrefix, todayDateStr, newSeq);

            // 更新当前序列号和日期串
            rule.setCurrentSeq(newSeq);
            rule.setCurrentDateStr(todayDateStr);
            mkNumberRuleMapper.updateCurrentSeq(rule);

            return number;
        }
    }

    /**
     * 生成预览编号（不消耗序列号）
     */
    @Override
    public String previewNumber(MkNumberRule rule)
    {
        if (rule == null)
        {
            return "";
        }
        String dateStr = buildDateStr(rule.getDateFormat(), rule.getResetType());
        long previewSeq = rule.getSeqStart() != null ? rule.getSeqStart() : 1L;
        // 固定前缀始终使用 rule.getPrefix()
        String fixedPrefix = rule.getPrefix();
        // 预览时显示第一个启用的动态前缀作为示例
        String dynamicPrefix = "";
        if ("1".equals(rule.getPrefixFieldEnabled()) && rule.getPrefixList() != null && !rule.getPrefixList().isEmpty())
        {
            for (MkNumberRulePrefix p : rule.getPrefixList())
            {
                if ("1".equals(p.getEnabled()) && StringUtils.isNotEmpty(p.getPrefix()))
                {
                    dynamicPrefix = p.getPrefix();
                    break;
                }
            }
        }
        return buildNumber(rule, fixedPrefix, dynamicPrefix, dateStr, previewSeq);
    }

    /**
     * 根据日期格式和重置类型构建日期串
     */
    private String buildDateStr(String dateFormat, String resetType)
    {
        if (StringUtils.isEmpty(dateFormat))
        {
            return "";
        }
        String pattern = dateFormat;
        // 根据重置类型自动推断日期格式
        if ("1".equals(resetType))
        {
            pattern = "yyyyMMdd";
        }
        else if ("2".equals(resetType))
        {
            pattern = "yyyyMM";
        }
        else if ("3".equals(resetType))
        {
            pattern = "yyyy";
        }
        else
        {
            // 不重置时，使用配置的日期格式
            pattern = StringUtils.isNotEmpty(dateFormat) ? dateFormat : "";
        }
        if (StringUtils.isEmpty(pattern))
        {
            return "";
        }
        return new SimpleDateFormat(pattern).format(new Date());
    }

    /**
     * 拼装完整编号
     * 格式：固定前缀 + 连接符 + 动态前缀 + 日期串 + 序列号（补零）
     * - 有动态前缀时：固定前缀 + 连接符 + 动态前缀 + 日期 + 序列号
     * - 无动态前缀时：固定前缀 + 日期 + 序列号（连接符用于前缀和日期之间）
     */
    private String buildNumber(MkNumberRule rule, String fixedPrefix, String dynamicPrefix, String dateStr, long seq)
    {
        StringBuilder sb = new StringBuilder();
        // 固定前缀
        if (StringUtils.isNotEmpty(fixedPrefix))
        {
            sb.append(fixedPrefix);
        }
        // 动态前缀（用连接符连接固定前缀和动态前缀）
        if (StringUtils.isNotEmpty(dynamicPrefix))
        {
            if (StringUtils.isNotEmpty(fixedPrefix) && StringUtils.isNotEmpty(rule.getConnector()))
            {
                sb.append(rule.getConnector());
            }
            sb.append(dynamicPrefix);
        }
        // 连接符 + 日期串（连接符用于动态前缀和日期之间，或固定前缀和日期之间）
        if (StringUtils.isNotEmpty(dateStr))
        {
            boolean hasPrefix = StringUtils.isNotEmpty(fixedPrefix) || StringUtils.isNotEmpty(dynamicPrefix);
            if (hasPrefix && StringUtils.isNotEmpty(rule.getConnector()))
            {
                sb.append(rule.getConnector());
            }
            sb.append(dateStr);
        }
        // 序列号（左侧补零）
        int seqLength = rule.getSeqLength() != null ? rule.getSeqLength() : 4;
        String seqStr = StringUtils.padl(seq, seqLength);
        sb.append(seqStr);
        return sb.toString();
    }
}

package com.ruoyi.food.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.food.domain.FoodBatchRule;
import com.ruoyi.food.mapper.FoodBatchRuleMapper;
import com.ruoyi.food.service.IFoodBatchRuleService;

/**
 * 食品批次号规则Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class FoodBatchRuleServiceImpl implements IFoodBatchRuleService
{
    /** {seq:n} 占位符 */
    private static final Pattern SEQ_PATTERN = Pattern.compile("\\{seq:(\\d+)}");

    @Autowired
    private FoodBatchRuleMapper ruleMapper;

    @Override
    public FoodBatchRule selectRuleById(Long ruleId)
    {
        return ruleMapper.selectRuleById(ruleId);
    }

    @Override
    public List<FoodBatchRule> selectRuleList(FoodBatchRule rule)
    {
        return ruleMapper.selectRuleList(rule);
    }

    @Override
    public int insertRule(FoodBatchRule rule)
    {
        validatePattern(rule.getRulePattern());
        if (rule.getMaterialId() == null)
        {
            rule.setMaterialId(0L);
        }
        if (rule.getStatus() == null)
        {
            rule.setStatus("0");
        }
        rule.setCreateBy(rule.getCreateBy() == null ? "system" : rule.getCreateBy());
        return ruleMapper.insertRule(rule);
    }

    @Override
    public int updateRule(FoodBatchRule rule)
    {
        if (rule.getRulePattern() != null)
        {
            validatePattern(rule.getRulePattern());
        }
        rule.setUpdateBy(rule.getUpdateBy() == null ? "system" : rule.getUpdateBy());
        return ruleMapper.updateRule(rule);
    }

    @Override
    public int deleteRuleByIds(Long[] ruleIds)
    {
        return ruleMapper.deleteRuleByIds(ruleIds);
    }

    /**
     * 原子取号：跨日重置+自增单条SQL完成；LAST_INSERT_ID连接级读取保证并发安全
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String nextBatchCode(Long materialId, String materialCode)
    {
        FoodBatchRule rule = materialId != null ? ruleMapper.selectRuleByMaterial(materialId) : null;
        if (rule == null)
        {
            rule = ruleMapper.selectRuleByMaterial(0L);
        }
        if (rule == null)
        {
            throw new ServiceException("未配置可用的批次号规则，请先在【批次规则】中配置默认规则");
        }
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        FoodBatchRule seqUpdater = new FoodBatchRule();
        seqUpdater.setRuleId(rule.getRuleId());
        seqUpdater.setSeqDate(today);
        ruleMapper.nextSeq(seqUpdater);
        Long seq = ruleMapper.selectLastSeq();
        return expandPattern(rule.getRulePattern(), today, materialCode, String.valueOf(seq), null);
    }

    @Override
    public String previewPattern(String rulePattern, String materialCode)
    {
        validatePattern(rulePattern);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 试算用 1 号流水，位数按占位符补零
        Matcher m = SEQ_PATTERN.matcher(rulePattern);
        String seqStr = null;
        if (m.find())
        {
            seqStr = "1";
        }
        return expandPattern(rulePattern, today, materialCode, seqStr, null);
    }

    /**
     * 展开模板占位符
     */
    private String expandPattern(String pattern, String today, String materialCode, String seqStr, Integer seqWidth)
    {
        String result = pattern;
        result = result.replace("{yyyyMMdd}", today);
        result = result.replace("{yyMM}", today.substring(2, 6));
        result = result.replace("{yyyy}", today.substring(0, 4));
        result = result.replace("{materialCode}", materialCode == null || materialCode.isEmpty() ? "MAT" : materialCode);
        if (seqStr != null)
        {
            Matcher m = SEQ_PATTERN.matcher(result);
            StringBuffer sb = new StringBuffer();
            while (m.find())
            {
                int width = Integer.parseInt(m.group(1));
                String padded = String.format("%0" + width + "d", Long.parseLong(seqStr));
                m.appendReplacement(sb, Matcher.quoteReplacement(padded));
            }
            m.appendTail(sb);
            result = sb.toString();
        }
        return result;
    }

    /**
     * 模板合法性校验：必须包含 {seq:n} 占位符保证唯一性
     */
    private void validatePattern(String pattern)
    {
        if (pattern == null || pattern.isEmpty())
        {
            throw new ServiceException("批次号模板不能为空");
        }
        Matcher m = SEQ_PATTERN.matcher(pattern);
        if (!m.find())
        {
            throw new ServiceException("批次号模板必须包含 {seq:n} 流水占位符（如 {seq:4}），否则无法保证批次号唯一");
        }
    }
}

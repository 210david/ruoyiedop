package com.ruoyi.mk.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mk.domain.MkNumberRule;

/**
 * 编号规则 Service接口
 *
 * @author ruoyi
 */
public interface IMkNumberRuleService
{
    public List<MkNumberRule> selectRuleList(MkNumberRule rule);

    public MkNumberRule selectRuleById(Long ruleId);

    public MkNumberRule selectRuleByCode(String ruleCode);

    public int insertRule(MkNumberRule rule);

    public int updateRule(MkNumberRule rule);

    public int deleteRuleByIds(Long[] ruleIds);

    public boolean checkRuleCodeUnique(MkNumberRule rule);

    /**
     * 根据规则编码生成下一个编号
     *
     * @param ruleCode 规则编码
     * @return 生成的编号
     */
    public String generateNumber(String ruleCode);

    /**
     * 根据规则编码生成下一个编号（支持动态前缀）
     *
     * @param ruleCode 规则编码
     * @param params   业务参数（如 materialType=0），用于动态前缀查找
     * @return 生成的编号
     */
    public String generateNumber(String ruleCode, Map<String, String> params);

    /**
     * 根据规则生成预览编号（不消耗序列号）
     *
     * @param rule 规则对象
     * @return 预览编号
     */
    public String previewNumber(MkNumberRule rule);
}

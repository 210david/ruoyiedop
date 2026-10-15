package com.ruoyi.food.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.food.domain.FoodCcpOverLimitEvent;
import com.ruoyi.food.domain.FoodCcpRecord;
import com.ruoyi.food.domain.FoodCcpTemplate;
import com.ruoyi.food.mapper.FoodCcpRecordMapper;
import com.ruoyi.food.mapper.FoodCcpTemplateMapper;
import com.ruoyi.food.service.IFoodCcpService;

/**
 * HACCP CCP电子台账 Service实现
 *
 * 核心规则：
 * 1. 监控记录录入即按模板关键限值自动判定（合格/超限），记录后核心数据不可修改（验厂留痕）
 * 2. 超限记录发布同步事件（同一线程同一事务），由 qms 监听器创建NCR并冻结关联批次
 * 3. 模板配置驱动：监控项类型/限值类型/限值/监控频率/纠正措施均来自模板快照
 *
 * @author ruoyi
 */
@Service
public class FoodCcpServiceImpl implements IFoodCcpService
{
    @Autowired
    private FoodCcpTemplateMapper templateMapper;

    @Autowired
    private FoodCcpRecordMapper recordMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // ================= 模板（配置驱动） =================

    @Override
    public List<FoodCcpTemplate> selectTemplateList(FoodCcpTemplate template)
    {
        return templateMapper.selectCcpTemplateList(template);
    }

    @Override
    public FoodCcpTemplate selectTemplateById(Long templateId)
    {
        return templateMapper.selectCcpTemplateById(templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTemplate(FoodCcpTemplate template)
    {
        validateTemplate(template);
        template.setCreateBy(SecurityUtils.getUsername());
        template.setCreateTime(DateUtils.getNowDate());
        return templateMapper.insertCcpTemplate(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTemplate(FoodCcpTemplate template)
    {
        validateTemplate(template);
        template.setUpdateBy(SecurityUtils.getUsername());
        template.setUpdateTime(DateUtils.getNowDate());
        return templateMapper.updateCcpTemplate(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTemplateByIds(Long[] templateIds)
    {
        return templateMapper.deleteCcpTemplateByIds(templateIds);
    }

    // ================= 监控记录 =================

    @Override
    public List<FoodCcpRecord> selectRecordList(FoodCcpRecord record)
    {
        return recordMapper.selectCcpRecordList(record);
    }

    @Override
    public FoodCcpRecord selectRecordById(Long recordId)
    {
        return recordMapper.selectCcpRecordById(recordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCcpRecord(FoodCcpRecord record)
    {
        FoodCcpTemplate tpl = templateMapper.selectCcpTemplateById(record.getTemplateId());
        if (tpl == null || !"0".equals(tpl.getStatus()) || "2".equals(tpl.getDelFlag()))
        {
            throw new ServiceException("CCP控制点模板不存在或已停用");
        }
        // 模板字段快照（记录不受模板后续变更影响）
        record.setCcpName(tpl.getCcpName());
        record.setMonitorType(tpl.getMonitorType());
        record.setRouteId(tpl.getRouteId());
        record.setProcessName(tpl.getProcessName());
        record.setLimitSnapshot(buildLimitSnapshot(tpl));
        // 关键限值校验
        String judge = judgeLimit(tpl, record);
        record.setJudgeResult(judge);
        if ("1".equals(judge) && StringUtils.isEmpty(record.getCorrective()))
        {
            throw new ServiceException("监控值超出关键限值（" + record.getLimitSnapshot() + "），必须填写纠正措施");
        }
        if (record.getRecordTime() == null)
        {
            record.setRecordTime(new Date());
        }
        record.setRecordNo(recordMapper.selectNextRecordNo());
        String username = SecurityUtils.getUsername();
        record.setRecorder(username);
        record.setCreateBy(username);
        record.setCreateTime(DateUtils.getNowDate());
        record.setDelFlag("0");
        int rows = recordMapper.insertCcpRecord(record);
        // 超限联动：同步事件（同一线程同一事务），由qms监听器创建NCR+冻结批次
        if ("1".equals(judge))
        {
            eventPublisher.publishEvent(new FoodCcpOverLimitEvent(record));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRecord(FoodCcpRecord record)
    {
        FoodCcpRecord existing = recordMapper.selectCcpRecordById(record.getRecordId());
        if (existing == null)
        {
            throw new ServiceException("CCP记录不存在");
        }
        record.setUpdateBy(SecurityUtils.getUsername());
        record.setUpdateTime(DateUtils.getNowDate());
        return recordMapper.updateCcpRecord(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOverLink(Long recordId, Long ncrId, String ncrNo)
    {
        return recordMapper.updateOverLink(recordId, ncrId, ncrNo);
    }

    @Override
    public Map<String, Object> statsSummary(FoodCcpRecord record)
    {
        List<FoodCcpRecord> list = recordMapper.selectCcpRecordList(record);
        int total = list.size();
        int over = 0;
        for (FoodCcpRecord r : list)
        {
            if ("1".equals(r.getJudgeResult()))
            {
                over++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pass", total - over);
        result.put("over", over);
        return result;
    }

    /**
     * 模板校验：控制点名称、监控项类型、限值完整性
     */
    private void validateTemplate(FoodCcpTemplate template)
    {
        if (StringUtils.isEmpty(template.getCcpName()))
        {
            throw new ServiceException("请填写控制点名称");
        }
        if (StringUtils.isEmpty(template.getMonitorType()))
        {
            throw new ServiceException("请选择监控项类型");
        }
        String limitType = template.getLimitType();
        if (StringUtils.isEmpty(limitType))
        {
            throw new ServiceException("请选择限值类型");
        }
        if ("4".equals(limitType))
        {
            if (StringUtils.isEmpty(template.getLimitValue()))
            {
                throw new ServiceException("枚举限值不能为空");
            }
        }
        else if ("2".equals(limitType))
        {
            if (template.getLimitMin() == null)
            {
                throw new ServiceException("请填写限值下限");
            }
        }
        else if ("3".equals(limitType))
        {
            if (template.getLimitMax() == null)
            {
                throw new ServiceException("请填写限值上限");
            }
        }
        else if ("1".equals(limitType))
        {
            if (template.getLimitMin() == null || template.getLimitMax() == null)
            {
                throw new ServiceException("区间限值需同时填写下限与上限");
            }
            if (template.getLimitMin().compareTo(template.getLimitMax()) > 0)
            {
                throw new ServiceException("限值下限不能大于上限");
            }
        }
        else
        {
            throw new ServiceException("未知限值类型：" + limitType);
        }
    }

    /**
     * 关键限值判定：返回 "0"合格 / "1"超限；监控值缺失直接抛异常拦截
     */
    private String judgeLimit(FoodCcpTemplate tpl, FoodCcpRecord record)
    {
        // 枚举限值：限定字典枚举值（当前用于上霉状态）
        if ("4".equals(tpl.getLimitType()))
        {
            String value = record.getMoldStatus();
            if (StringUtils.isEmpty(value))
            {
                throw new ServiceException("请选择上霉状态");
            }
            return tpl.getLimitValue() != null && tpl.getLimitValue().equals(value) ? "0" : "1";
        }
        // 数值监控值
        BigDecimal value = getMonitorValue(tpl.getMonitorType(), record);
        if (value == null)
        {
            throw new ServiceException("请填写监控值");
        }
        if ("2".equals(tpl.getLimitType()))
        {
            // 不小于下限
            return (tpl.getLimitMin() != null && value.compareTo(tpl.getLimitMin()) < 0) ? "1" : "0";
        }
        if ("3".equals(tpl.getLimitType()))
        {
            // 不大于上限
            return (tpl.getLimitMax() != null && value.compareTo(tpl.getLimitMax()) > 0) ? "1" : "0";
        }
        // 区间
        if (tpl.getLimitMin() != null && value.compareTo(tpl.getLimitMin()) < 0)
        {
            return "1";
        }
        if (tpl.getLimitMax() != null && value.compareTo(tpl.getLimitMax()) > 0)
        {
            return "1";
        }
        return "0";
    }

    /**
     * 按监控项类型取对应监控值（1温度 2湿度 3盐量比例 5其他）
     */
    private BigDecimal getMonitorValue(String monitorType, FoodCcpRecord record)
    {
        switch (monitorType == null ? "" : monitorType)
        {
            case "1": return record.getTempValue();
            case "2": return record.getHumidityValue();
            case "3": return record.getSaltRate();
            case "5": return record.getMonitorValue();
            default: throw new ServiceException("该监控项类型不支持下值校验，请检查模板配置");
        }
    }

    /**
     * 构建关键限值快照文本（如 0~4℃ / ≥2.5% / 枚举:正常）
     */
    private String buildLimitSnapshot(FoodCcpTemplate tpl)
    {
        String unit = StringUtils.isNotEmpty(tpl.getUnit()) ? tpl.getUnit() : "";
        if ("4".equals(tpl.getLimitType()))
        {
            String label = DictUtils.getDictLabel("food_mold_status", tpl.getLimitValue());
            return "枚举:" + (StringUtils.isNotEmpty(label) ? label : StringUtils.defaultString(tpl.getLimitValue()));
        }
        if ("2".equals(tpl.getLimitType()))
        {
            return "≥" + plain(tpl.getLimitMin()) + unit;
        }
        if ("3".equals(tpl.getLimitType()))
        {
            return "≤" + plain(tpl.getLimitMax()) + unit;
        }
        String min = tpl.getLimitMin() != null ? plain(tpl.getLimitMin()) : "-∞";
        String max = tpl.getLimitMax() != null ? plain(tpl.getLimitMax()) : "+∞";
        return min + "~" + max + unit;
    }

    private String plain(BigDecimal value)
    {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }
}

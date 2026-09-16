package com.ruoyi.ai.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ai.domain.AiCallLog;
import com.ruoyi.ai.mapper.AiCallLogMapper;
import com.ruoyi.ai.service.IAiAuditService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * AI调用审计 Service实现
 *
 * @author ruoyi
 */
@Service
public class AiAuditServiceImpl implements IAiAuditService
{
    @Autowired
    private AiCallLogMapper logMapper;

    @Override
    public int insertLog(AiCallLog log)
    {
        // 自动填充当前登录人（定时任务/异步等无登录上下文时跳过）
        try
        {
            log.setUserId(SecurityUtils.getUserId());
            log.setUserName(SecurityUtils.getUsername());
        }
        catch (Exception ignore) {}
        log.setCallTime(DateUtils.getNowDate());
        return logMapper.insertLog(log);
    }

    @Override
    public List<AiCallLog> selectLogList(AiCallLog log)
    {
        return logMapper.selectLogList(log);
    }

    @Override
    public Map<String, Object> selectUsageStat(int days)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("days", days);
        Map<String, Object> stat = new HashMap<>();
        stat.put("total", logMapper.selectTotalStat(params));
        stat.put("moduleStat", logMapper.selectModuleStat(params));
        stat.put("featureStat", logMapper.selectFeatureStat(params));
        return stat;
    }
}

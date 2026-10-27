package com.ruoyi.mk.task;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.mk.domain.MkOpportunity;
import com.ruoyi.mk.service.IMkOpportunityService;

/**
 * 商机定时任务
 *
 * 调用方式：在Quartz定时任务中配置
 * - Bean名称: mkOpportunityTask
 * - 方法: checkStagnantOpportunities
 * - cron表达式建议: 0 0 9 * * ? （每天早上9点执行）
 *
 * @author ruoyi
 */
@Component("mkOpportunityTask")
public class MkOpportunityTask
{
    private static final Logger log = LoggerFactory.getLogger(MkOpportunityTask.class);

    @Autowired
    private IMkOpportunityService mkOpportunityService;

    /**
     * 检查阶段超时的停滞商机
     * 查询在当前阶段停留时间超过 maxDays 的进行中商机
     */
    public void checkStagnantOpportunities()
    {
        List<MkOpportunity> stagnantList = mkOpportunityService.selectStagnantOpportunities();
        if (stagnantList != null && !stagnantList.isEmpty())
        {
            log.warn("发现 {} 个商机在当前阶段停留超时:", stagnantList.size());
            for (MkOpportunity opp : stagnantList)
            {
                log.warn("  商机[{}] {} - 阶段[{}] 负责人[{}]",
                    opp.getOpportunityNo(), opp.getOpportunityName(),
                    opp.getStageName(), opp.getUserName());
            }
        }
        else
        {
            log.info("商机阶段超时检查完成，无停滞商机");
        }
    }
}

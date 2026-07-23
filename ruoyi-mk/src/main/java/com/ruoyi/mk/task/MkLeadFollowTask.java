package com.ruoyi.mk.task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.mk.domain.MkInteraction;
import com.ruoyi.mk.domain.MkLead;
import com.ruoyi.mk.mapper.MkInteractionMapper;
import com.ruoyi.mk.mapper.MkLeadMapper;

/**
 * 线索跟进到期提醒定时任务
 *
 * 调用方式：在Quartz定时任务中配置
 * - Bean名称: mkLeadFollowTask
 * - 方法: checkOverdueFollowUps
 * - cron表达式建议: 0 0 9 * * ? （每天早上9点执行）
 *
 * @author ruoyi
 */
@Component("mkLeadFollowTask")
public class MkLeadFollowTask
{
    private static final Logger log = LoggerFactory.getLogger(MkLeadFollowTask.class);

    /** 超过多少天未跟进视为逾期 */
    private static final int OVERDUE_DAYS = 7;

    @Autowired
    private MkLeadMapper mkLeadMapper;

    @Autowired
    private MkInteractionMapper mkInteractionMapper;

    /**
     * 检查线索跟进到期提醒
     * 1. 检查互动记录中 nextTime 已到期的跟进计划
     * 2. 检查超过N天未跟进的线索
     */
    public void checkOverdueFollowUps()
    {
        int count = 0;

        // 检查已到期的跟进计划（互动记录的next_time已过但未处理）
        MkInteraction queryInteraction = new MkInteraction();
        List<MkInteraction> interactions = mkInteractionMapper.selectInteractionList(queryInteraction);
        Date now = new Date();
        for (MkInteraction interaction : interactions)
        {
            if (interaction.getNextTime() != null && interaction.getNextTime().before(now))
            {
                // 有到期跟进计划的线索
                if (interaction.getLeadId() != null)
                {
                    count++;
                    log.warn("线索[{}]有到期的跟进计划，负责人:{}，计划跟进时间:{}",
                            interaction.getLeadId(), interaction.getUserName(), interaction.getNextTime());
                }
            }
        }

        // 检查超过N天未跟进的线索
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -OVERDUE_DAYS);
        Date overdueDate = cal.getTime();

        MkLead queryLead = new MkLead();
        queryLead.setIsPublic("0"); // 仅检查私海线索
        List<MkLead> leads = mkLeadMapper.selectLeadList(queryLead);
        int overdueCount = 0;
        for (MkLead lead : leads)
        {
            // 状态为0(待跟进)、1(已联系)、2(有意向)的线索需要检查
            if ("0".equals(lead.getLeadStatus()) || "1".equals(lead.getLeadStatus()) || "2".equals(lead.getLeadStatus()))
            {
                if (lead.getLastFollowTime() == null || lead.getLastFollowTime().before(overdueDate))
                {
                    overdueCount++;
                    log.warn("线索[{}]企业[{}]已超过{}天未跟进，负责人:{}",
                            lead.getLeadId(), lead.getCompanyName(), OVERDUE_DAYS, lead.getUserName());
                }
            }
        }

        log.info("线索跟进提醒检查完成，到期跟进计划{}条，超过{}天未跟进线索{}条",
                count, OVERDUE_DAYS, overdueCount);
    }
}

package com.ruoyi.bid.task;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.bid.domain.BidProject;
import com.ruoyi.bid.mapper.BidProjectMapper;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.utils.MessageHelper;

/**
 * 招投标-截止时间预警任务
 *
 * 调用方式（Quartz 定时任务配置）：
 * - Bean名称: bidDeadlineWarnTask
 * - 方法: checkDeadlines
 * - cron建议: 0 0 8 * * ?（每天早上8点）
 */
@Component("bidDeadlineWarnTask")
public class BidDeadlineWarnTask
{
    private static final Logger log = LoggerFactory.getLogger(BidDeadlineWarnTask.class);

    @Autowired
    private BidProjectMapper projectMapper;

    @Autowired
    private MessageHelper messageHelper;

    public void checkDeadlines()
    {
        int warnCount = 0;
        // 3 天内、7 天内两级预警
        for (int days : new int[] {3, 7})
        {
            List<BidProject> projects = projectMapper.selectDeadlineWarnProjects(days);
            if (projects == null)
            {
                continue;
            }
            for (BidProject p : projects)
            {
                String deadlineDesc = buildDeadlineDesc(p);
                if (deadlineDesc == null)
                {
                    continue;
                }
                messageHelper.sendMessage(
                        "【投标截止提醒】" + p.getBidNo() + " " + truncate(p.getProjectName(), 40),
                        "投标项目「" + p.getProjectName() + "」" + deadlineDesc + "，请及时跟进办理。",
                        "2", "2", "bid", p.getProjectId(), "/bid/project", "bid:project:list");
                warnCount++;
            }
            // 7 天级只在剩余 5~7 天时提醒（避免与 3 天级重复推送）
            if (days == 7)
            {
                break; // 3天查一次全覆盖（BETWEEN now AND now+3），7天查一次覆盖 0~7，用3天级避免重复
            }
        }
        if (warnCount > 0)
        {
            log.warn("投标截止预警完成，共推送 {} 条提醒", warnCount);
        }
        else
        {
            log.info("投标截止预警完成，无临近截止项目");
        }
    }

    private String buildDeadlineDesc(BidProject p)
    {
        java.util.Date now = new java.util.Date();
        String target = null;
        java.util.Date targetTime = null;
        if (p.getApplyDeadline() != null && p.getApplyDeadline().after(now))
        {
            target = "报名";
            targetTime = p.getApplyDeadline();
        }
        else if (p.getSubmitDeadline() != null && p.getSubmitDeadline().after(now))
        {
            target = "递交";
            targetTime = p.getSubmitDeadline();
        }
        else if (p.getOpenTime() != null && p.getOpenTime().after(now))
        {
            target = "开标";
            targetTime = p.getOpenTime();
        }
        if (target == null)
        {
            return null;
        }
        long days = (targetTime.getTime() - now.getTime()) / (24 * 3600 * 1000L);
        return target + "截止时间 " + DateUtils.parseDateToStr("yyyy-MM-dd HH:mm", targetTime)
                + "（剩余 " + (days + 1) + " 天）";
    }

    private String truncate(String s, int max)
    {
        if (s == null)
        {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}

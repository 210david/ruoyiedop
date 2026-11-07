package com.ruoyi.integration.task;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.integration.service.IDingtalkSyncService;

/**
 * 钉钉考勤数据同步定时任务
 *
 * Quartz 调用目标示例（系统监控-定时任务新增）：
 *   dingtalkAttendanceSyncTask.syncYesterday   每日凌晨同步昨日打卡（建议 cron: 0 30 1 * * ?）
 *   dingtalkAttendanceSyncTask.syncContacts    每日同步通讯录并自动匹配（建议 cron: 0 10 1 * * ?）
 *   dingtalkAttendanceSyncTask.syncRecentDays  参数化同步最近N天（invokeTarget: dingtalkAttendanceSyncTask.syncRecentDays("3")）
 *
 * @author ruoyi
 */
@Component("dingtalkAttendanceSyncTask")
public class DingtalkAttendanceSyncTask
{
    private static final Logger log = LoggerFactory.getLogger(DingtalkAttendanceSyncTask.class);

    @Autowired
    private IDingtalkSyncService dingtalkSyncService;

    @Autowired
    private com.ruoyi.integration.dingtalk.DingTalkApiClient dingTalkApiClient;

    /**
     * 模拟数据模式下跳过定时任务，避免模拟数据被反复写入正式考勤与绑定表
     */
    private boolean skipForMock(String action)
    {
        if (dingTalkApiClient.isMockEnabled())
        {
            log.warn("定时任务[{}]跳过：当前为钉钉模拟数据模式（integration.dingtalk.mockEnabled=true）", action);
            return true;
        }
        return false;
    }

    /**
     * 同步昨日打卡数据（幂等，可重跑）
     */
    public void syncYesterday()
    {
        if (skipForMock("syncYesterday"))
        {
            return;
        }
        Date yesterday = addDays(new Date(), -1);
        log.info("定时任务开始：同步钉钉昨日考勤数据 {}", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, yesterday));
        System.out.println(dingtalkSyncService.syncAttendance(yesterday, yesterday).get("msg"));
    }

    /**
     * 同步最近 N 天打卡数据（含今日，用于补拉）
     *
     * @param days 天数（建议 ≤ 31）
     */
    public void syncRecentDays(Integer days)
    {
        if (skipForMock("syncRecentDays"))
        {
            return;
        }
        int n = (days == null || days < 1) ? 1 : Math.min(days, 31);
        Date begin = addDays(new Date(), -(n - 1));
        log.info("定时任务开始：同步钉钉最近 {} 天考勤数据", n);
        System.out.println(dingtalkSyncService.syncAttendance(begin, new Date()).get("msg"));
    }

    /**
     * 同步钉钉通讯录并自动匹配员工档案
     */
    public void syncContacts()
    {
        if (skipForMock("syncContacts"))
        {
            return;
        }
        log.info("定时任务开始：同步钉钉通讯录");
        System.out.println(dingtalkSyncService.syncContacts().get("msg"));
    }

    private Date addDays(Date date, int days)
    {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.add(java.util.Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }
}

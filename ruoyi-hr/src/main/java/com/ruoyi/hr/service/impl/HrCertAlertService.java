package com.ruoyi.hr.service.impl;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.hr.domain.HrCertificate;
import com.ruoyi.hr.mapper.HrCertificateMapper;
import com.ruoyi.system.utils.MessageHelper;

/**
 * 健康证到期提醒Service（定时任务专用）
 *
 * 食品从业人员健康证监管必备：刷新预警状态 → 临期（60天内）每日提醒、过期提醒一次；
 * 噪音受控：临期按"同一证书同一天只发一次"去重、过期按"同一证书只发一次"去重，
 * 接收人按 hr:certificate:list 权限分发，处置闭环走消息中心 biz_handled 留痕。
 *
 * @author ruoyi
 */
@Service
public class HrCertAlertService
{
    private static final Logger log = LoggerFactory.getLogger(HrCertAlertService.class);

    /** 消息业务来源 */
    private static final String BIZ_SOURCE = "hr";

    /** 接收角色权限标识（持有证书台账查询权限的角色可见） */
    private static final String RECIPIENT_PERM = "hr:certificate:list";

    @Autowired
    private HrCertificateMapper certificateMapper;

    @Autowired
    private MessageHelper messageHelper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 健康证到期提醒（quartz 入口）
     *
     * @return 本轮发送消息数
     */
    public int pushExpiringAlerts()
    {
        // 1. 先刷新健康证预警状态（有效期/复审日期临近自动升级临期、过期）
        certificateMapper.refreshHealthCertRemindStatus();
        // 2. 扫描临期/过期健康证
        List<HrCertificate> expiringList = certificateMapper.selectExpiringHealthCerts();
        String today = DateUtils.getDate();
        int count = 0;
        for (HrCertificate cert : expiringList)
        {
            try
            {
                boolean expired = "2".equals(cert.getRemindStatus());
                // 噪音受控：临期每天最多一条；过期只发一次
                if (existsAlert(cert.getCertId(), cert.getRemindStatus(), expired ? null : today))
                {
                    continue;
                }
                long daysRemain = cert.getValidTo() == null ? 0L
                        : (cert.getValidTo().getTime() - new Date().getTime()) / (24 * 3600 * 1000L);
                String title = (expired ? "健康证已过期：" : "健康证临期提醒：") + cert.getEmployeeName();
                String content = "员工：" + cert.getEmployeeName() + "（工号 " + cert.getEmployeeNo() + "，部门 "
                        + cert.getDeptName() + "），健康证编号 " + cert.getCertNo()
                        + "，有效期至 " + DateUtils.parseDateToStr("yyyy-MM-dd", cert.getValidTo())
                        + (expired ? "，已过期，根据食品从业人员监管要求请立即停岗并安排复检换证"
                                  : "，剩余 " + daysRemain + " 天，请及时安排复检换证");
                messageHelper.sendMessage(title, content, "2", expired ? "3" : "2",
                        BIZ_SOURCE, cert.getCertId(), "/hr/extra/certificate", RECIPIENT_PERM,
                        cert.getRemindStatus(), "证书台账");
                count++;
            }
            catch (Exception e)
            {
                log.error("健康证提醒推送失败 certId={}", cert.getCertId(), e);
            }
        }
        if (count > 0)
        {
            log.info("[健康证到期提醒] 本轮推送 {} 条", count);
        }
        return count;
    }

    /**
     * 去重判断：sys_message 中同业务来源+证书+预警状态是否已有消息
     * 临期传 today 仅匹配当天发送的（每日一条）；过期传 null 匹配任意时间（只发一次）
     */
    private boolean existsAlert(Long certId, String bizStatus, String today)
    {
        String sql = "SELECT COUNT(*) FROM sys_message WHERE biz_source = 'hr' AND biz_id = ? "
                + "AND biz_status = ? AND status = '1'";
        Object[] args;
        if (today != null)
        {
            sql += " AND create_time >= ?";
            args = new Object[] { certId, bizStatus, today + " 00:00:00" };
        }
        else
        {
            args = new Object[] { certId, bizStatus };
        }
        Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return cnt != null && cnt > 0;
    }
}

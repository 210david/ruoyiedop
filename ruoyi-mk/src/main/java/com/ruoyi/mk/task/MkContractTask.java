package com.ruoyi.mk.task;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.mk.domain.MkContract;
import com.ruoyi.mk.service.IMkContractService;
import com.ruoyi.system.utils.MessageHelper;

/**
 * 合同定时任务
 *
 * 调用方式：在Quartz定时任务中配置
 * - Bean名称: mkContractTask
 * - 方法: checkExpiredContracts
 * - cron表达式建议: 0 0 1 * * ? （每天凌晨1点执行）
 *
 * @author ruoyi
 */
@Component("mkContractTask")
public class MkContractTask
{
    private static final Logger log = LoggerFactory.getLogger(MkContractTask.class);

    @Autowired
    private IMkContractService mkContractService;

    @Autowired
    private MessageHelper messageHelper;

    /** 临期提醒提前天数（与定时任务描述「7天内到期」保持一致） */
    private static final int EXPIRING_DAYS = 7;

    /**
     * 自动检查并更新过期合同
     * 将已生效但到期日期已过期的合同状态更新为"已过期"
     * 注意：到期日期为空的「长期有效」合同不会被标记过期
     */
    public void checkExpiredContracts()
    {
        int count = mkContractService.checkExpiredContracts();
        log.info("合同过期检查完成，共更新{}条过期合同", count);
    }

    /**
     * 检查即将到期的合同并推送站内消息提醒
     * 默认提前 7 天提醒，仅针对已生效且填写了到期日期的合同
     * 长期有效合同（到期日期为空）不参与提醒
     */
    public void checkExpiringContracts()
    {
        List<MkContract> expiringList = mkContractService.selectExpiringContracts(EXPIRING_DAYS);
        if (expiringList.isEmpty())
        {
            log.info("合同临期提醒：{}天内无到期合同", EXPIRING_DAYS);
            return;
        }
        log.info("发现{}个合同将在{}天内到期，开始推送提醒", expiringList.size(), EXPIRING_DAYS);
        for (MkContract contract : expiringList)
        {
            String expireText = new java.text.SimpleDateFormat("yyyy-MM-dd").format(contract.getExpireDate());
            String title = "合同即将到期：" + contract.getContractNo();
            String content = String.format("合同【%s】%s 将于 %s 到期（剩余 %d 天），客户：%s，负责人：%s，请及时跟进续签或终止。",
                    contract.getContractNo(),
                    contract.getContractName() == null ? "" : contract.getContractName(),
                    expireText,
                    daysBetween(contract.getExpireDate()),
                    contract.getCustomerName() == null ? "-" : contract.getCustomerName(),
                    contract.getUserName() == null ? "-" : contract.getUserName());
            // 发送给营销相关角色：合同维护岗与销售经理
            messageHelper.sendMessage(title, content, "2", "2", "mk",
                    contract.getContractId(), "/mk/contract", "marketing:contract:list",
                    contract.getContractStatus(), "合同管理");
        }
        log.info("合同临期提醒推送完成，共{}条", expiringList.size());
    }

    /** 计算距到期还剩多少天 */
    private int daysBetween(java.util.Date expireDate)
    {
        long diff = expireDate.getTime() - System.currentTimeMillis();
        return (int) Math.ceil(diff / (1000.0 * 60 * 60 * 24));
    }
}

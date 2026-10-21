package com.ruoyi.mk.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.mk.service.IMkContractService;

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

    /**
     * 自动检查并更新过期合同
     * 将已生效但到期日期已过期的合同状态更新为"已过期"
     */
    public void checkExpiredContracts()
    {
        int count = mkContractService.checkExpiredContracts();
        log.info("合同过期检查完成，共更新{}条过期合同", count);
    }

    /**
     * 查询即将到期的合同（30天内到期）
     * 可以配合消息通知使用
     */
    public void checkExpiringContracts()
    {
        int days = 30;
        var expiringList = mkContractService.selectExpiringContracts(days);
        if (!expiringList.isEmpty())
        {
            log.info("发现{}个合同将在{}天内到期", expiringList.size(), days);
            for (var contract : expiringList)
            {
                log.warn("合同[{}]将于{}到期，客户：{}，负责人：{}",
                    contract.getContractNo(), contract.getExpireDate(),
                    contract.getCustomerName(), contract.getUserName());
            }
        }
    }
}

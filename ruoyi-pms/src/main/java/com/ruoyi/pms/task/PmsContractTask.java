package com.ruoyi.pms.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.pms.service.IPmsContractService;

/**
 * 采购合同到期检查定时任务
 *
 * 调用方式：在Quartz定时任务中配置
 * - Bean名称: pmsContractTask
 * - 方法: checkExpiredContracts
 * - cron表达式建议: 0 0 8 * * ? （每天早上8点执行）
 *
 * @author ruoyi
 */
@Component("pmsContractTask")
public class PmsContractTask
{
    private static final Logger log = LoggerFactory.getLogger(PmsContractTask.class);

    @Autowired
    private IPmsContractService contractService;

    /**
     * 检查并标记过期的采购合同
     * 将到期日期已过但状态仍为"已签订"的合同自动标记为"已到期"
     */
    public void checkExpiredContracts()
    {
        int count = contractService.checkExpiredContracts();
        if (count > 0)
        {
            log.warn("采购合同到期检查完成，共发现{}份已到期合同，已自动标记为已到期", count);
        }
        else
        {
            log.info("采购合同到期检查完成，无到期合同");
        }
    }
}

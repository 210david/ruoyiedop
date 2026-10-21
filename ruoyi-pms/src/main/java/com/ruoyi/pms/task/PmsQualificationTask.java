package com.ruoyi.pms.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.pms.service.IPmsSupplierQualificationService;

/**
 * 供应商资质过期检查定时任务
 *
 * 调用方式：在Quartz定时任务中配置
 * - Bean名称: pmsQualificationTask
 * - 方法: checkExpiredQualifications
 * - cron表达式建议: 0 0 8 * * ? （每天早上8点执行）
 *
 * @author ruoyi
 */
@Component("pmsQualificationTask")
public class PmsQualificationTask
{
    private static final Logger log = LoggerFactory.getLogger(PmsQualificationTask.class);

    @Autowired
    private IPmsSupplierQualificationService qualificationService;

    /**
     * 检查并标记过期的供应商资质
     * 将有效期已过但状态仍为有效的资质自动标记为"已过期"
     */
    public void checkExpiredQualifications()
    {
        int count = qualificationService.checkExpiredQualifications();
        if (count > 0)
        {
            log.warn("供应商资质过期检查完成，共发现{}条过期资质，已自动标记为已过期", count);
        }
        else
        {
            log.info("供应商资质过期检查完成，无过期资质");
        }
    }
}

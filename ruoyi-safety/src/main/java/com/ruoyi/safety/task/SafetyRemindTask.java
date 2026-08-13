package com.ruoyi.safety.task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.safety.domain.SafetyCert;
import com.ruoyi.safety.domain.SafetyEmergencyMaterial;
import com.ruoyi.safety.domain.SafetyEmergencyPlan;
import com.ruoyi.safety.domain.SafetyEquipment;
import com.ruoyi.safety.domain.SafetyHazard;
import com.ruoyi.safety.domain.SafetyRemind;
import com.ruoyi.safety.domain.SafetyRiskPoint;
import com.ruoyi.safety.domain.SafetyWorker;
import com.ruoyi.safety.domain.SafetyMaterial;
import com.ruoyi.safety.service.ISafetyCertService;
import com.ruoyi.safety.service.ISafetyEmergencyMaterialService;
import com.ruoyi.safety.service.ISafetyEmergencyPlanService;
import com.ruoyi.safety.service.ISafetyEquipmentService;
import com.ruoyi.safety.service.ISafetyHazardService;
import com.ruoyi.safety.service.ISafetyRemindService;
import com.ruoyi.safety.service.ISafetyRiskPointService;
import com.ruoyi.safety.service.ISafetyWorkerService;
import com.ruoyi.safety.service.ISafetyMaterialService;

/**
 * 安全生产到期提醒定时任务
 *
 * 调用方式：在Quartz定时任务中配置
 * - Bean名称: safetyRemindTask
 * - 方法: scanExpiryRemind
 * - cron表达式建议: 0 0 8 * * ? （每天早上8点执行）
 *
 * @author ruoyi
 */
@Component("safetyRemindTask")
public class SafetyRemindTask
{
    private static final Logger log = LoggerFactory.getLogger(SafetyRemindTask.class);

    /** 提前提醒天数 */
    private static final int REMIND_DAYS = 30;

    @Autowired
    private ISafetyCertService safetyCertService;
    @Autowired
    private ISafetyWorkerService safetyWorkerService;
    @Autowired
    private ISafetyEquipmentService safetyEquipmentService;
    @Autowired
    private ISafetyEmergencyPlanService safetyEmergencyPlanService;
    @Autowired
    private ISafetyEmergencyMaterialService safetyEmergencyMaterialService;
    @Autowired
    private ISafetyHazardService safetyHazardService;
    @Autowired
    private ISafetyRiskPointService safetyRiskPointService;
    @Autowired
    private ISafetyRemindService safetyRemindService;
    @Autowired
    private ISafetyMaterialService safetyMaterialService;

    /**
     * 扫描所有到期提醒
     */
    public void scanExpiryRemind()
    {
        log.info("===== 开始执行安全生产到期提醒扫描 =====");
        int total = 0;
        total += scanCertExpiry();
        total += scanWorkerCertExpiry();
        total += scanEquipmentInspectExpiry();
        total += scanEmergencyPlanExpiry();
        total += scanEmergencyMaterialExpiry();
        total += scanHazardOverdue();
        total += scanRiskReEvaluateExpiry();
        total += scanMaterialStockAlert();
        log.info("===== 到期提醒扫描完成，共生成{}条提醒 =====", total);
    }

    /**
     * 证照到期提醒
     */
    private int scanCertExpiry()
    {
        SafetyCert query = new SafetyCert();
        query.setStatus("0");
        List<SafetyCert> list = safetyCertService.selectSafetyCertList(query);
        int count = 0;
        Date now = DateUtils.getNowDate();
        for (SafetyCert cert : list)
        {
            if (cert.getExpireDate() == null) continue;
            int days = daysBetween(now, cert.getExpireDate());
            if (days >= 0 && days <= REMIND_DAYS)
            {
                createRemind("1", cert.getCertId(), cert.getCertName(),
                        "证照「" + cert.getCertName() + "」将于" + cert.getExpireDate() + "到期，剩余" + days + "天",
                        cert.getExpireDate(), days);
                count++;
            }
        }
        log.info("证照到期提醒：{}条", count);
        return count;
    }

    /**
     * 特种作业人员证书到期提醒
     */
    private int scanWorkerCertExpiry()
    {
        SafetyWorker query = new SafetyWorker();
        query.setStatus("0");
        query.setIsSpecial("1");
        List<SafetyWorker> list = safetyWorkerService.selectSafetyWorkerList(query);
        int count = 0;
        Date now = DateUtils.getNowDate();
        for (SafetyWorker worker : list)
        {
            if (worker.getValidTo() == null) continue;
            int days = daysBetween(now, worker.getValidTo());
            if (days >= 0 && days <= REMIND_DAYS)
            {
                createRemind("2", worker.getWorkerId(), worker.getWorkerName(),
                        "人员「" + worker.getWorkerName() + "」的特种作业证书将于" + worker.getValidTo() + "到期，剩余" + days + "天",
                        worker.getValidTo(), days);
                count++;
            }
            if (worker.getReviewDate() != null)
            {
                int reviewDays = daysBetween(now, worker.getReviewDate());
                if (reviewDays >= 0 && reviewDays <= REMIND_DAYS)
                {
                    createRemind("2", worker.getWorkerId(), worker.getWorkerName(),
                            "人员「" + worker.getWorkerName() + "」的证书复审日期将于" + worker.getReviewDate() + "到期，剩余" + reviewDays + "天",
                            worker.getReviewDate(), reviewDays);
                    count++;
                }
            }
        }
        log.info("特种作业证书到期提醒：{}条", count);
        return count;
    }

    /**
     * 设备检验到期提醒
     */
    private int scanEquipmentInspectExpiry()
    {
        SafetyEquipment query = new SafetyEquipment();
        query.setStatus("0");
        List<SafetyEquipment> list = safetyEquipmentService.selectSafetyEquipmentList(query);
        int count = 0;
        Date now = DateUtils.getNowDate();
        for (SafetyEquipment equipment : list)
        {
            if (equipment.getInspectExpire() == null) continue;
            int days = daysBetween(now, equipment.getInspectExpire());
            if (days >= 0 && days <= REMIND_DAYS)
            {
                createRemind("3", equipment.getEquipmentId(), equipment.getEquipmentName(),
                        "设备「" + equipment.getEquipmentName() + "」的检验将于" + equipment.getInspectExpire() + "到期，剩余" + days + "天",
                        equipment.getInspectExpire(), days);
                count++;
            }
        }
        log.info("设备检验到期提醒：{}条", count);
        return count;
    }

    /**
     * 应急预案修订到期提醒
     */
    private int scanEmergencyPlanExpiry()
    {
        SafetyEmergencyPlan query = new SafetyEmergencyPlan();
        query.setStatus("0");
        List<SafetyEmergencyPlan> list = safetyEmergencyPlanService.selectSafetyEmergencyPlanList(query);
        int count = 0;
        Date now = DateUtils.getNowDate();
        for (SafetyEmergencyPlan plan : list)
        {
            if (plan.getExpireDate() == null) continue;
            int days = daysBetween(now, plan.getExpireDate());
            if (days >= 0 && days <= REMIND_DAYS)
            {
                createRemind("6", plan.getPlanId(), plan.getPlanName(),
                        "应急预案「" + plan.getPlanName() + "」的修订将于" + plan.getExpireDate() + "到期，剩余" + days + "天",
                        plan.getExpireDate(), days);
                count++;
            }
        }
        log.info("应急预案修订到期提醒：{}条", count);
        return count;
    }

    /**
     * 应急物资有效期到期提醒
     */
    private int scanEmergencyMaterialExpiry()
    {
        SafetyEmergencyMaterial query = new SafetyEmergencyMaterial();
        query.setStatus("0");
        List<SafetyEmergencyMaterial> list = safetyEmergencyMaterialService.selectSafetyEmergencyMaterialList(query);
        int count = 0;
        Date now = DateUtils.getNowDate();
        for (SafetyEmergencyMaterial material : list)
        {
            if (material.getEffectiveDate() == null) continue;
            int days = daysBetween(now, material.getEffectiveDate());
            if (days >= 0 && days <= REMIND_DAYS)
            {
                createRemind("4", material.getMaterialId(), material.getMaterialName(),
                        "应急物资「" + material.getMaterialName() + "」将于" + material.getEffectiveDate() + "过期，剩余" + days + "天",
                        material.getEffectiveDate(), days);
                count++;
            }
        }
        log.info("应急物资到期提醒：{}条", count);
        return count;
    }

    /**
     * 隐患超期未整改提醒
     */
    private int scanHazardOverdue()
    {
        SafetyHazard query = new SafetyHazard();
        query.setHazardStatus("1");
        List<SafetyHazard> list = safetyHazardService.selectSafetyHazardList(query);
        int count = 0;
        Date now = DateUtils.getNowDate();
        for (SafetyHazard hazard : list)
        {
            if (hazard.getRectifyDeadline() == null) continue;
            int days = daysBetween(now, hazard.getRectifyDeadline());
            if (days < 0)
            {
                // 已超期
                createRemind("5", hazard.getHazardId(), hazard.getHazardCode(),
                        "隐患「" + hazard.getHazardCode() + "」已超过整改期限" + Math.abs(days) + "天，请立即处理",
                        hazard.getRectifyDeadline(), days);
                // 更新隐患状态为超期未整改
                hazard.setHazardStatus("5");
                safetyHazardService.updateSafetyHazard(hazard);
                count++;
            }
        }
        log.info("隐患超期提醒：{}条", count);
        return count;
    }

    /**
     * 风险点复评到期提醒
     * 规则（规格书 §5.1.1、§9.5）：
     * - 红橙级每年复评、黄蓝级每 3 年复评
     * - 复评日期到期前 30 天触发提醒
     */
    private int scanRiskReEvaluateExpiry()
    {
        SafetyRiskPoint query = new SafetyRiskPoint();
        query.setStatus("0");
        List<SafetyRiskPoint> list = safetyRiskPointService.selectSafetyRiskPointList(query);
        int count = 0;
        Date now = DateUtils.getNowDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (SafetyRiskPoint rp : list)
        {
            if (StringUtils.isEmpty(rp.getReEvaluateDate())) continue;
            try
            {
                Date reEvalDate = sdf.parse(rp.getReEvaluateDate());
                int days = daysBetween(now, reEvalDate);
                if (days >= 0 && days <= REMIND_DAYS)
                {
                    createRemind("7", rp.getRiskPointId(), rp.getRiskName(),
                            "风险点「" + rp.getRiskName() + "」的复评日期将于" + rp.getReEvaluateDate() + "到期，剩余" + days + "天",
                            reEvalDate, days);
                    count++;
                }
            }
            catch (Exception e)
            {
                log.warn("风险点复评日期解析失败：{} - {}", rp.getRiskName(), rp.getReEvaluateDate());
            }
        }
        log.info("风险复评到期提醒：{}条", count);
        return count;
    }

    /**
     * 危化品库存预警扫描
     */
    private int scanMaterialStockAlert()
    {
        List<SafetyMaterial> list = safetyMaterialService.selectStockAlertList();
        int count = 0;
        Date now = DateUtils.getNowDate();
        for (SafetyMaterial m : list)
        {
            String alertType = "";
            if (m.getSafetyStockMax() != null && m.getSafetyStockMax().compareTo(java.math.BigDecimal.ZERO) > 0
                && m.getCurrentStock() != null && m.getCurrentStock().compareTo(m.getSafetyStockMax()) > 0) {
                alertType = "超过安全库存上限";
            } else if (m.getMaxStorage() != null && m.getCurrentStock() != null
                       && m.getCurrentStock().compareTo(m.getMaxStorage()) > 0) {
                alertType = "超过最大储存量";
            } else if (m.getSafetyStockMin() != null && m.getSafetyStockMin().compareTo(java.math.BigDecimal.ZERO) > 0
                       && m.getCurrentStock() != null && m.getCurrentStock().compareTo(m.getSafetyStockMin()) < 0) {
                alertType = "低于安全库存下限";
            }
            if (StringUtils.isNotEmpty(alertType)) {
                createRemind("8", m.getMaterialId(), m.getMaterialName(),
                        "危化品「" + m.getMaterialName() + "」库存" + alertType + "（当前库存：" + m.getCurrentStock() + m.getStorageUnit() + "）",
                        now, 0);
                count++;
            }
        }
        log.info("危化品库存预警提醒：{}条", count);
        return count;
    }

    /**
     * 创建提醒记录
     */
    private void createRemind(String remindType, Long relatedId, String relatedName,
                              String content, Date expireDate, int days)
    {
        SafetyRemind remind = new SafetyRemind();
        remind.setRemindType(remindType);
        remind.setRelatedId(relatedId);
        remind.setRelatedName(relatedName);
        remind.setRemindContent(content);
        remind.setExpireDate(expireDate);
        remind.setRemindDays(days);
        remind.setRemindStatus("0");
        remind.setNotifyLevel(days < 0 ? "2" : (days <= 7 ? "1" : "0"));
        remind.setCreateBy("system");
        safetyRemindService.insertSafetyRemind(remind);
    }

    /**
     * 计算两个日期之间的天数（now - target）
     * 正数表示还没到期，负数表示已过期
     */
    private int daysBetween(Date now, Date target)
    {
        Calendar calNow = Calendar.getInstance();
        calNow.setTime(now);
        calNow.set(Calendar.HOUR_OF_DAY, 0);
        calNow.set(Calendar.MINUTE, 0);
        calNow.set(Calendar.SECOND, 0);
        calNow.set(Calendar.MILLISECOND, 0);

        Calendar calTarget = Calendar.getInstance();
        calTarget.setTime(target);
        calTarget.set(Calendar.HOUR_OF_DAY, 0);
        calTarget.set(Calendar.MINUTE, 0);
        calTarget.set(Calendar.SECOND, 0);
        calTarget.set(Calendar.MILLISECOND, 0);

        long diff = calTarget.getTimeInMillis() - calNow.getTimeInMillis();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }
}
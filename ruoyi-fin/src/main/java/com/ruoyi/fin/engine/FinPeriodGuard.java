package com.ruoyi.fin.engine;

import com.ruoyi.fin.domain.FinPeriod;
import com.ruoyi.fin.exception.FinException;
import com.ruoyi.fin.mapper.FinPeriodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 会计期间守卫。
 *
 * <p>【P0-4 约束落地】所有凭证写操作前必须经本校验，校验点必须在 Service 层，
 * 不可放在 Controller，避免 Quartz 轮询等非 HTTP 入口绕过。</p>
 *
 * <p>规则：</p>
 * <ol>
 *   <li>期间未初始化 → 拒绝（10003）</li>
 *   <li>期间已结账 CLOSED → 拒绝（10002）</li>
 *   <li>已结账期间仅财务经理可解锁后操作（10012）</li>
 *   <li>period 一律由 voucherDate 派生，不允许手工指定</li>
 * </ol>
 *
 * @author ruoyi
 */
@Component
public class FinPeriodGuard
{
    /** 期间状态：未结账 */
    public static final String STATUS_OPEN = "OPEN";

    /** 期间状态：已结账 */
    public static final String STATUS_CLOSED = "CLOSED";

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyyMM");

    @Autowired
    private FinPeriodMapper periodMapper;

    /**
     * 由凭证日期派生会计期间（YYYYMM）。
     *
     * @param voucherDate 凭证日期
     * @return 期间字符串，如 202610
     */
    public String derivePeriod(LocalDate voucherDate)
    {
        return voucherDate.format(YM);
    }

    /**
     * 校验期间是否可写。凭证保存、审核、反审核、结账等所有写操作前必须调用。
     *
     * @param period 会计期间 YYYYMM
     */
    public void checkWritable(String period)
    {
        FinPeriod p = periodMapper.selectByPeriod(period);
        if (p == null)
        {
            throw FinException.periodNotInit(period);
        }
        if (STATUS_CLOSED.equals(p.getStatus()))
        {
            throw FinException.periodClosed(period);
        }
    }

    /**
     * 校验科目表是否已初始化。为空时禁止录凭证。
     */
    public void checkSubjectReady()
    {
        Integer cnt = periodMapper.countSubject();
        if (cnt == null || cnt == 0)
        {
            throw FinException.subjectEmpty();
        }
    }
}

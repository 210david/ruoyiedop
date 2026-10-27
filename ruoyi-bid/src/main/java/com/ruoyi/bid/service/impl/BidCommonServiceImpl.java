package com.ruoyi.bid.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.bid.domain.BidCrawlLog;
import com.ruoyi.bid.domain.BidDeposit;
import com.ruoyi.bid.domain.BidQualification;
import com.ruoyi.bid.mapper.BidCrawlLogMapper;
import com.ruoyi.bid.mapper.BidDepositMapper;
import com.ruoyi.bid.mapper.BidQualificationMapper;
import com.ruoyi.bid.service.IBidCommonService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.utils.MessageHelper;

/**
 * 保证金/资质/日志 通用 Service 实现
 */
@Service
public class BidCommonServiceImpl implements IBidCommonService
{
    private static final Logger log = LoggerFactory.getLogger(BidCommonServiceImpl.class);

    @Autowired
    private BidCrawlLogMapper logMapper;

    @Autowired
    private BidDepositMapper depositMapper;

    @Autowired
    private BidQualificationMapper qualificationMapper;

    @Autowired
    private MessageHelper messageHelper;

    // ===== 抓取日志 =====

    @Override
    public List<BidCrawlLog> selectLogList(BidCrawlLog log)
    {
        return logMapper.selectLogList(log);
    }

    @Override
    public BidCrawlLog selectLogById(Long logId)
    {
        return logMapper.selectLogById(logId);
    }

    @Override
    public List<BidCrawlLog> selectRecentBySourceId(Long sourceId)
    {
        return logMapper.selectRecentBySourceId(sourceId);
    }

    @Override
    public java.util.Map<String, Object> selectLogStatusCounts(BidCrawlLog log)
    {
        java.util.Map<String, Object> counts = new HashMap<>();
        long all = 0;
        for (java.util.Map<String, Object> row : logMapper.selectStatusCounts(log))
        {
            String key = row.get("statusKey") == null ? "" : String.valueOf(row.get("statusKey"));
            long cnt = ((Number) row.get("cnt")).longValue();
            counts.put(key, cnt);
            all += cnt;
        }
        counts.put("all", all);
        return counts;
    }

    // ===== 保证金 =====

    @Override
    public List<BidDeposit> selectDepositList(BidDeposit deposit)
    {
        return depositMapper.selectDepositList(deposit);
    }

    @Override
    public BidDeposit selectDepositById(Long depositId)
    {
        return depositMapper.selectDepositById(depositId);
    }

    @Override
    public List<BidDeposit> selectDepositByProjectId(Long projectId)
    {
        return depositMapper.selectDepositByProjectId(projectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDeposit(BidDeposit deposit)
    {
        validateDeposit(deposit);
        return depositMapper.insertDeposit(deposit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDeposit(BidDeposit deposit)
    {
        if (deposit.getDepositId() == null)
        {
            throw new ServiceException("保证金ID不能为空");
        }
        return depositMapper.updateDeposit(deposit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDepositByIds(Long[] depositIds)
    {
        return depositMapper.deleteDepositByIds(depositIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refundDeposit(Long depositId, java.util.Date actualRefundTime, java.math.BigDecimal refundAmount, String remark)
    {
        BidDeposit deposit = depositMapper.selectDepositById(depositId);
        if (deposit == null)
        {
            throw new ServiceException("保证金记录不存在");
        }
        if (!"0".equals(deposit.getRefundStatus()) && !"2".equals(deposit.getRefundStatus()))
        {
            throw new ServiceException("只有在外/部分退回状态的保证金才能登记退回");
        }
        if (actualRefundTime == null)
        {
            throw new ServiceException("实际退回时间不能为空");
        }
        if (deposit.getPayTime() != null && actualRefundTime.before(deposit.getPayTime()))
        {
            throw new ServiceException("实际退回时间不能早于缴纳时间");
        }
        java.math.BigDecimal refunded = refundAmount == null ? deposit.getAmount()
                : (deposit.getRefundAmount() == null ? java.math.BigDecimal.ZERO : deposit.getRefundAmount()).add(refundAmount);
        if (refunded.compareTo(deposit.getAmount()) > 0)
        {
            throw new ServiceException("累计退回金额不能超过缴纳金额");
        }
        BidDeposit update = new BidDeposit();
        update.setDepositId(depositId);
        update.setActualRefundTime(actualRefundTime);
        update.setRefundAmount(refunded);
        // 累计退回金额 >= 缴纳金额 → 已退回；否则 → 部分退回
        update.setRefundStatus(refunded.compareTo(deposit.getAmount()) >= 0 ? "1" : "2");
        update.setRemark(remark);
        return depositMapper.updateDeposit(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confiscateDeposit(Long depositId, String remark)
    {
        BidDeposit deposit = depositMapper.selectDepositById(depositId);
        if (deposit == null)
        {
            throw new ServiceException("保证金记录不存在");
        }
        if (!"0".equals(deposit.getRefundStatus()) && !"2".equals(deposit.getRefundStatus()))
        {
            throw new ServiceException("只有在外/部分退回状态的保证金才能登记没收");
        }
        BidDeposit update = new BidDeposit();
        update.setDepositId(depositId);
        update.setRefundStatus("3");
        update.setRemark(remark);
        return depositMapper.updateDeposit(update);
    }

    @Override
    public int checkDepositOverdue()
    {
        List<BidDeposit> overdue = depositMapper.selectOverdueDeposits();
        if (overdue == null)
        {
            return 0;
        }
        for (BidDeposit d : overdue)
        {
            messageHelper.sendMessage(
                    "【保证金超期】" + d.getBidNo() + " " + truncate(d.getProjectName(), 40),
                    "投标保证金（金额 " + d.getAmount() + " 元）已超期未退回，应退回时间 "
                            + DateUtils.parseDateToStr("yyyy-MM-dd", d.getExpectRefundTime())
                            + "，请尽快跟进办理退回。",
                    "2", "2", "bid", d.getDepositId(), "/bid/deposit", "bid:deposit:list");
        }
        return overdue.size();
    }

    @Override
    public Map<String, Object> occupancySummary()
    {
        return depositMapper.selectOccupancySummary();
    }

    // ===== 资质 =====

    @Override
    public List<BidQualification> selectQualificationList(BidQualification qualification)
    {
        return qualificationMapper.selectQualificationList(qualification);
    }

    @Override
    public BidQualification selectQualificationById(Long qualificationId)
    {
        return qualificationMapper.selectQualificationById(qualificationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertQualification(BidQualification qualification)
    {
        validateQualification(qualification);
        return qualificationMapper.insertQualification(qualification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateQualification(BidQualification qualification)
    {
        validateQualification(qualification);
        if (qualification.getQualificationId() == null)
        {
            throw new ServiceException("资质ID不能为空");
        }
        return qualificationMapper.updateQualification(qualification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteQualificationByIds(Long[] qualificationIds)
    {
        return qualificationMapper.deleteQualificationByIds(qualificationIds);
    }

    @Override
    public int checkQualificationExpire()
    {
        List<BidQualification> expiring = qualificationMapper.selectExpiringQualifications();
        if (expiring == null)
        {
            return 0;
        }
        for (BidQualification q : expiring)
        {
            messageHelper.sendMessage(
                    "【资质到期提醒】" + truncate(q.getName(), 40),
                    "资质/证书「" + q.getName() + "」（" + q.getCertNo() + "）将于 "
                            + DateUtils.parseDateToStr("yyyy-MM-dd", q.getExpireDate())
                            + " 到期，请及时安排续期，避免影响投标。",
                    "2", "2", "bid", q.getQualificationId(), "/bid/qualification", "bid:qualification:list");
        }
        return expiring.size();
    }

    private void validateDeposit(BidDeposit deposit)
    {
        if (deposit.getProjectId() == null)
        {
            throw new ServiceException("关联投标项目不能为空");
        }
        if (deposit.getAmount() == null || deposit.getAmount().signum() <= 0)
        {
            throw new ServiceException("缴纳金额必须大于 0");
        }
        if (deposit.getPayTime() != null && deposit.getExpectRefundTime() != null
                && deposit.getExpectRefundTime().before(deposit.getPayTime()))
        {
            throw new ServiceException("应退回时间不能早于缴纳时间");
        }
    }

    private void validateQualification(BidQualification qualification)
    {
        if (qualification.getQualType() == null || qualification.getName() == null)
        {
            throw new ServiceException("资料类型和名称不能为空");
        }
        if (qualification.getIssueDate() != null && qualification.getExpireDate() != null
                && qualification.getExpireDate().before(qualification.getIssueDate()))
        {
            throw new ServiceException("有效期至不能早于发证日期");
        }
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

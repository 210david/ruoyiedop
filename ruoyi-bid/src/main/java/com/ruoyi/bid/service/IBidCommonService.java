package com.ruoyi.bid.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.bid.domain.BidCrawlLog;
import com.ruoyi.bid.domain.BidDeposit;
import com.ruoyi.bid.domain.BidQualification;

/**
 * 保证金/资质/日志 等通用 CRUD Service 接口
 */
public interface IBidCommonService
{
    // ===== 抓取日志 =====
    public List<BidCrawlLog> selectLogList(BidCrawlLog log);

    public BidCrawlLog selectLogById(Long logId);

    public List<BidCrawlLog> selectRecentBySourceId(Long sourceId);

    // ===== 保证金 =====
    public List<BidDeposit> selectDepositList(BidDeposit deposit);

    public BidDeposit selectDepositById(Long depositId);

    public List<BidDeposit> selectDepositByProjectId(Long projectId);

    public int insertDeposit(BidDeposit deposit);

    public int updateDeposit(BidDeposit deposit);

    public int deleteDepositByIds(Long[] depositIds);

    /** 登记退回 */
    public int refundDeposit(Long depositId, java.util.Date actualRefundTime, String remark);

    /** 保证金预警扫描（超期未退推送），返回超期条数 */
    public int checkDepositOverdue();

    /** 在外保证金占用统计 */
    public Map<String, Object> occupancySummary();

    // ===== 资质 =====
    public List<BidQualification> selectQualificationList(BidQualification qualification);

    public BidQualification selectQualificationById(Long qualificationId);

    public int insertQualification(BidQualification qualification);

    public int updateQualification(BidQualification qualification);

    public int deleteQualificationByIds(Long[] qualificationIds);

    /** 资质到期预警扫描，返回临期条数 */
    public int checkQualificationExpire();
}

package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.vo.MkDashboardVO;

/**
 * 营销管理统计报表 Service接口
 *
 * @author ruoyi
 */
public interface IMkDashboardService
{
    /** 概览统计 */
    public MkDashboardVO selectOverview();

    /** 线索转化漏斗 */
    public List<MkDashboardVO> selectLeadFunnel();

    /** 商机销售漏斗 */
    public List<MkDashboardVO> selectOpportunityPipeline();

    /** 客户行业分布 */
    public List<MkDashboardVO> selectCustomerByIndustry();

    /** 客户等级分布 */
    public List<MkDashboardVO> selectCustomerByLevel();

    /** 销售业绩排行 */
    public List<MkDashboardVO> selectSalesRanking();

    /** 合同/订单月度趋势 */
    public List<MkDashboardVO> selectRevenueTrend();

    /** 活动效果统计 */
    public List<MkDashboardVO> selectActivityResult();

    /** 赢单输单分析 */
    public List<MkDashboardVO> selectWinLossAnalysis();

    /** 销售预测 */
    public List<MkDashboardVO> selectSalesForecast();

    /** 销售人员综合统计 */
    public List<MkDashboardVO> selectSalespersonOverview();

    /** 销售人员月度业绩趋势 */
    public List<MkDashboardVO> selectSalespersonMonthlyTrend(Long userId);
}

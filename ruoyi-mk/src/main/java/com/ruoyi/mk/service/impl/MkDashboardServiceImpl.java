package com.ruoyi.mk.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mk.domain.vo.MkDashboardVO;
import com.ruoyi.mk.mapper.MkDashboardMapper;
import com.ruoyi.mk.service.IMkDashboardService;

/**
 * 营销管理统计报表 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkDashboardServiceImpl implements IMkDashboardService
{
    @Autowired
    private MkDashboardMapper mkDashboardMapper;

    @Override
    public MkDashboardVO selectOverview()
    {
        return mkDashboardMapper.selectOverview();
    }

    @Override
    public List<MkDashboardVO> selectLeadFunnel()
    {
        return mkDashboardMapper.selectLeadFunnel();
    }

    @Override
    public List<MkDashboardVO> selectOpportunityPipeline()
    {
        return mkDashboardMapper.selectOpportunityPipeline();
    }

    @Override
    public List<MkDashboardVO> selectCustomerByIndustry()
    {
        return mkDashboardMapper.selectCustomerByIndustry();
    }

    @Override
    public List<MkDashboardVO> selectCustomerByLevel()
    {
        return mkDashboardMapper.selectCustomerByLevel();
    }

    @Override
    public List<MkDashboardVO> selectSalesRanking()
    {
        return mkDashboardMapper.selectSalesRanking();
    }

    @Override
    public List<MkDashboardVO> selectRevenueTrend()
    {
        return mkDashboardMapper.selectRevenueTrend();
    }

    @Override
    public List<MkDashboardVO> selectActivityResult()
    {
        return mkDashboardMapper.selectActivityResult();
    }

    @Override
    public List<MkDashboardVO> selectWinLossAnalysis()
    {
        return mkDashboardMapper.selectWinLossAnalysis();
    }

    @Override
    public List<MkDashboardVO> selectSalesForecast()
    {
        return mkDashboardMapper.selectSalesForecast();
    }

    @Override
    public List<MkDashboardVO> selectSalespersonOverview()
    {
        List<MkDashboardVO> list = mkDashboardMapper.selectSalespersonOverview();
        // 计算排名、转化率、平均成交金额等派生字段
        for (int i = 0; i < list.size(); i++)
        {
            MkDashboardVO vo = list.get(i);
            vo.setRank(i + 1);
            // 转化率 = 赢单数 / (赢单数 + 输单数) * 100
            int won = vo.getWonCount() != null ? vo.getWonCount() : 0;
            int lost = vo.getLostCount() != null ? vo.getLostCount() : 0;
            int totalDeals = won + lost;
            vo.setTotalDealCount(totalDeals);
            if (totalDeals > 0)
            {
                vo.setConversionRate(java.math.BigDecimal.valueOf(won).multiply(java.math.BigDecimal.valueOf(100))
                        .divide(java.math.BigDecimal.valueOf(totalDeals), 2, java.math.RoundingMode.HALF_UP));
            }
            else
            {
                vo.setConversionRate(java.math.BigDecimal.ZERO);
            }
            // 平均成交金额 = 赢单金额 / 赢单数
            java.math.BigDecimal wonAmount = vo.getWonAmount() != null ? vo.getWonAmount() : java.math.BigDecimal.ZERO;
            if (won > 0)
            {
                vo.setAvgDealAmount(wonAmount.divide(java.math.BigDecimal.valueOf(won), 2, java.math.RoundingMode.HALF_UP));
            }
            else
            {
                vo.setAvgDealAmount(java.math.BigDecimal.ZERO);
            }
        }
        return list;
    }

    @Override
    public List<MkDashboardVO> selectSalespersonMonthlyTrend(Long userId)
    {
        return mkDashboardMapper.selectSalespersonMonthlyTrend(userId);
    }
}

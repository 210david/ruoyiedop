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

    @Override
    public java.util.Map<String, Object> selectCustomerTypeAnalysis()
    {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        List<MkDashboardVO> stats = mkDashboardMapper.selectCustomerTypeStats();
        // 派生指标：客单价、销售额占比、客户数占比
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        long totalCustomers = 0L;
        for (MkDashboardVO vo : stats)
        {
            totalAmount = totalAmount.add(vo.getOrderAmount() != null ? vo.getOrderAmount() : java.math.BigDecimal.ZERO);
            totalCustomers += vo.getCustomerCount() != null ? vo.getCustomerCount() : 0L;
        }
        for (MkDashboardVO vo : stats)
        {
            java.math.BigDecimal amount = vo.getOrderAmount() != null ? vo.getOrderAmount() : java.math.BigDecimal.ZERO;
            int orderCnt = vo.getOrderCount() != null ? vo.getOrderCount() : 0;
            // 客单价 = 销售额 / 订单数
            if (orderCnt > 0)
            {
                vo.setAvgOrderAmount(amount.divide(java.math.BigDecimal.valueOf(orderCnt), 2, java.math.RoundingMode.HALF_UP));
            }
            else
            {
                vo.setAvgOrderAmount(java.math.BigDecimal.ZERO);
            }
            // 销售额占比 = 该类型销售额 / 总销售额 * 100
            if (totalAmount.compareTo(java.math.BigDecimal.ZERO) > 0)
            {
                vo.setAmountRatio(amount.multiply(java.math.BigDecimal.valueOf(100))
                        .divide(totalAmount, 2, java.math.RoundingMode.HALF_UP));
            }
            else
            {
                vo.setAmountRatio(java.math.BigDecimal.ZERO);
            }
            // 客户数占比
            if (totalCustomers > 0 && vo.getCustomerCount() != null)
            {
                vo.setCustomerRatio(java.math.BigDecimal.valueOf(vo.getCustomerCount())
                        .multiply(java.math.BigDecimal.valueOf(100))
                        .divide(java.math.BigDecimal.valueOf(totalCustomers), 2, java.math.RoundingMode.HALF_UP));
            }
            else
            {
                vo.setCustomerRatio(java.math.BigDecimal.ZERO);
            }
        }
        result.put("stats", stats);
        result.put("trend", mkDashboardMapper.selectCustomerTypeTrend());
        return result;
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> selectPersonalTopCustomers()
    {
        List<java.util.Map<String, Object>> list = mkDashboardMapper.selectPersonalTopCustomers();
        // P2-04 脱敏：后端统一返回脱敏手机号（138****8888），响应中不含明文
        if (list != null)
        {
            for (java.util.Map<String, Object> row : list)
            {
                Object phone = row.get("contactPhone");
                if (phone != null)
                {
                    row.put("contactPhone", maskPhone(String.valueOf(phone)));
                }
            }
        }
        return list;
    }

    /** 手机号脱敏：保留前3后4（幂等，含 * 不重复处理；非11位不处理） */
    private String maskPhone(String phone)
    {
        if (phone == null || phone.isEmpty() || phone.contains("*") || phone.length() != 11)
        {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}

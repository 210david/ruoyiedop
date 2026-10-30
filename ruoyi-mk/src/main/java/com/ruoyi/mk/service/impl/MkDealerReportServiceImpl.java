package com.ruoyi.mk.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mk.domain.MkDealerReport;
import com.ruoyi.mk.mapper.MkDealerReportMapper;
import com.ruoyi.mk.service.IMkDealerReportService;

/**
 * 经销商专项报表 Service 业务层处理
 *
 * 口径说明：
 * - 销量：统计期内有效订单（状态 2已发货/3已签收/4已完成）的销售额与发货数量
 * - 返利：返利结算单累计/已兑现/已冲抵/未结（未全部冲抵余额）
 * - 账龄：回款计划未收金额，逾期=计划回款日已过未收，最长逾期天数按最老未收计划计算
 * - 流向：统计期内分销流向登记条数/数量/下游去重客户数
 * - 返利与账龄为时点指标，不受统计时间范围影响
 *
 * @author ruoyi
 */
@Service
public class MkDealerReportServiceImpl implements IMkDealerReportService
{
    @Autowired
    private MkDealerReportMapper mkDealerReportMapper;

    @Override
    public List<MkDealerReport> selectDealerReportList(MkDealerReport query)
    {
        return mkDealerReportMapper.selectDealerReportList(query);
    }

    @Override
    public Map<String, Object> selectDealerReportTotal(MkDealerReport query)
    {
        return mkDealerReportMapper.selectDealerReportTotal(query);
    }

    @Override
    public Map<String, Object> selectDealerReportCounts()
    {
        return mkDealerReportMapper.selectDealerReportCounts();
    }
}

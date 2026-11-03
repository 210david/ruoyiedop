package com.ruoyi.mk.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mk.domain.MkDealerReport;

/**
 * 经销商专项报表 Service 接口
 *
 * @author ruoyi
 */
public interface IMkDealerReportService
{
    /** 分经销商汇总列表 */
    public List<MkDealerReport> selectDealerReportList(MkDealerReport query);

    /** 汇总合计 */
    public Map<String, Object> selectDealerReportTotal(MkDealerReport query);

    /** 状态页签数量 */
    public Map<String, Object> selectDealerReportCounts();
}

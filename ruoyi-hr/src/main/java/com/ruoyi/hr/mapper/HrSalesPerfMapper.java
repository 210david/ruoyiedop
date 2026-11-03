package com.ruoyi.hr.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hr.domain.HrSalesProfile;
import com.ruoyi.hr.domain.HrPerfManual;
import com.ruoyi.hr.domain.HrPerfMonth;

/**
 * 营销档案/业绩归集 Mapper接口（需求三）
 */
public interface HrSalesPerfMapper {
    // ---- 营销档案 ----
    public List<HrSalesProfile> selectProfileList(HrSalesProfile profile);

    public HrSalesProfile selectProfileById(Long profileId);

    public HrSalesProfile selectProfileByEmployeeId(Long employeeId);

    public int insertProfile(HrSalesProfile profile);

    public int updateProfile(HrSalesProfile profile);

    public int deleteProfileByIds(Long[] profileIds);

    // ---- 手工补录 ----
    public List<HrPerfManual> selectManualList(HrPerfManual manual);

    public HrPerfManual selectManualById(Long manualId);

    public int insertManual(HrPerfManual manual);

    public int updateManual(HrPerfManual manual);

    // ---- 月度归集 ----
    public List<HrPerfMonth> selectPerfList(HrPerfMonth perf);

    public HrPerfMonth selectPerfById(Long perfId);

    public int insertPerf(HrPerfMonth perf);

    public int updatePerf(HrPerfMonth perf);

    /** 聚合：周期内按业务归属人汇总成交金额（mk_order） */
    List<java.util.Map<String, Object>> aggDealAmount(@Param("period") String period);

    /** 聚合：周期内按业务归属人汇总回款金额（mk_payment_record） */
    List<java.util.Map<String, Object>> aggPaymentAmount(@Param("period") String period);

    /** 聚合：周期内新开客户数（建档且完成首笔成交） */
    List<java.util.Map<String, Object>> aggNewCustomer(@Param("period") String period);

    /** 聚合：周期内审批通过的补录合计 */
    List<java.util.Map<String, Object>> aggManualAmount(@Param("period") String period);

    /** 删除指定周期归集快照（重算前清理） */
    int deletePerfByPeriod(@Param("period") String period);

    /** 排名：按周期合计业绩降序 */
    List<HrPerfMonth> selectPerfRank(@Param("period") String period);
}

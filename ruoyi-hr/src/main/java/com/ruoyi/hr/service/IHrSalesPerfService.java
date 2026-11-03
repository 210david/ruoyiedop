package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrSalesProfile;
import com.ruoyi.hr.domain.HrPerfManual;
import com.ruoyi.hr.domain.HrPerfMonth;

/**
 * 营销档案/业绩归集 Service接口（需求三）
 */
public interface IHrSalesPerfService {
    // ---- 营销档案 ----
    public List<HrSalesProfile> selectProfileList(HrSalesProfile profile);

    public HrSalesProfile selectProfileById(Long profileId);

    public int insertProfile(HrSalesProfile profile);

    public int updateProfile(HrSalesProfile profile);

    public int deleteProfileByIds(Long[] profileIds);

    // ---- 手工补录（需审批） ----
    public List<HrPerfManual> selectManualList(HrPerfManual manual);

    public int insertManual(HrPerfManual manual);

    /** 审批：approved=true 通过 / false 驳回 */
    public int approveManual(Long manualId, boolean approved, String opinion);

    // ---- 月度归集与排名 ----
    public List<HrPerfMonth> selectPerfList(HrPerfMonth perf);

    /** 手动/定时归集（幂等：先清理周期快照再重算） */
    public int aggregate(String period);

    /** 业绩排名（按周期合计业绩降序） */
    public List<HrPerfMonth> selectPerfRank(String period);
}

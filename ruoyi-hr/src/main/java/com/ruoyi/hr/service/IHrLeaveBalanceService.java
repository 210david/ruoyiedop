package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.hr.domain.HrLeaveBalance;
import com.ruoyi.hr.domain.dto.HrLeaveBalanceBatchDTO;

public interface IHrLeaveBalanceService {
    public HrLeaveBalance selectHrLeaveBalanceById(Long balanceId);
    public List<HrLeaveBalance> selectHrLeaveBalanceList(HrLeaveBalance hrLeaveBalance);
    public int insertHrLeaveBalance(HrLeaveBalance hrLeaveBalance);
    public int updateHrLeaveBalance(HrLeaveBalance hrLeaveBalance);
    public int deleteHrLeaveBalanceByIds(Long[] balanceIds);
    public int deleteHrLeaveBalanceById(Long balanceId);

    /** 批量生成假期余额 */
    public String batchGenerateBalance(HrLeaveBalanceBatchDTO dto, String operName);

    /** 批量导入假期余额 */
    public AjaxResult importBalance(List<HrLeaveBalance> list, Boolean overwrite, String operName);
}

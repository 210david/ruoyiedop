package com.ruoyi.hr.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.domain.HrLeaveBalance;
import com.ruoyi.hr.domain.dto.HrLeaveBalanceBatchDTO;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.mapper.HrLeaveBalanceMapper;
import com.ruoyi.hr.service.IHrLeaveBalanceService;

@Service
public class HrLeaveBalanceServiceImpl implements IHrLeaveBalanceService
{
    @Autowired
    private HrLeaveBalanceMapper hrLeaveBalanceMapper;
    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;

    @Override
    public HrLeaveBalance selectHrLeaveBalanceById(Long balanceId) { return hrLeaveBalanceMapper.selectHrLeaveBalanceById(balanceId); }

    @Override
    public List<HrLeaveBalance> selectHrLeaveBalanceList(HrLeaveBalance hrLeaveBalance) { return hrLeaveBalanceMapper.selectHrLeaveBalanceList(hrLeaveBalance); }

    @Override
    public int insertHrLeaveBalance(HrLeaveBalance hrLeaveBalance)
    {
        computeRemainingDays(hrLeaveBalance);
        return hrLeaveBalanceMapper.insertHrLeaveBalance(hrLeaveBalance);
    }

    @Override
    public int updateHrLeaveBalance(HrLeaveBalance hrLeaveBalance)
    {
        computeRemainingDays(hrLeaveBalance);
        return hrLeaveBalanceMapper.updateHrLeaveBalance(hrLeaveBalance);
    }

    /** 剩余天数 = 总额度 - 已休天数（服务端兑底） */
    private void computeRemainingDays(HrLeaveBalance balance)
    {
        if (balance.getTotalDays() == null || balance.getUsedDays() == null)
        {
            return;
        }
        balance.setRemainingDays(balance.getTotalDays().subtract(balance.getUsedDays()));
    }

    @Override
    public int deleteHrLeaveBalanceByIds(Long[] balanceIds) { return hrLeaveBalanceMapper.deleteHrLeaveBalanceByIds(balanceIds); }

    @Override
    public int deleteHrLeaveBalanceById(Long balanceId) { return hrLeaveBalanceMapper.deleteHrLeaveBalanceById(balanceId); }

    /**
     * 批量生成假期余额
     * 逻辑：
     * 1. 按部门ID获取所有在职员工
     * 2. 对每个假别，检查员工是否已有该年度的余额记录
     * 3. overwrite=true 则更新已有记录，false 则跳过
     * 4. 新记录批量插入
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String batchGenerateBalance(HrLeaveBalanceBatchDTO dto, String operName)
    {
        if (dto.getYear() == null)
        {
            throw new ServiceException("年度不能为空");
        }
        if (dto.getLeaveTypeDaysList() == null || dto.getLeaveTypeDaysList().isEmpty())
        {
            throw new ServiceException("请至少配置一个假别及额度");
        }

        // 1. 获取目标员工列表
        List<HrEmployee> employees = getEmployeesByDeptIds(dto.getDeptIds());
        if (employees.isEmpty())
        {
            throw new ServiceException("未找到符合条件的在职员工");
        }

        // 构建 假别->总额度 映射
        Map<String, BigDecimal> leaveTypeDaysMap = new HashMap<>();
        for (HrLeaveBalanceBatchDTO.LeaveTypeDays ltd : dto.getLeaveTypeDaysList())
        {
            if (ltd.getLeaveType() != null && ltd.getTotalDays() != null)
            {
                leaveTypeDaysMap.put(ltd.getLeaveType(), ltd.getTotalDays());
            }
        }
        if (leaveTypeDaysMap.isEmpty())
        {
            throw new ServiceException("请至少配置一个假别及额度");
        }

        boolean overwrite = Boolean.TRUE.equals(dto.getOverwrite());
        int insertCount = 0;
        int updateCount = 0;
        int skipCount = 0;

        // 2. 按假别逐个处理
        for (Map.Entry<String, BigDecimal> entry : leaveTypeDaysMap.entrySet())
        {
            String leaveType = entry.getKey();
            BigDecimal totalDays = entry.getValue();

            // 收集所有员工ID
            List<Long> employeeIds = new ArrayList<>();
            for (HrEmployee emp : employees)
            {
                if (emp.getEmployeeId() != null)
                {
                    employeeIds.add(emp.getEmployeeId());
                }
            }

            // 查询已存在的记录
            List<HrLeaveBalance> existing = hrLeaveBalanceMapper.selectExistingBalance(employeeIds, leaveType, dto.getYear());
            Map<Long, HrLeaveBalance> existingMap = new HashMap<>();
            for (HrLeaveBalance b : existing)
            {
                existingMap.put(b.getEmployeeId(), b);
            }

            // 待批量插入的列表
            List<HrLeaveBalance> toInsert = new ArrayList<>();

            for (HrEmployee emp : employees)
            {
                if (emp.getEmployeeId() == null) continue;

                BigDecimal usedDays = BigDecimal.ZERO;
                BigDecimal remainingDays = totalDays;

                if (existingMap.containsKey(emp.getEmployeeId()))
                {
                    if (overwrite)
                    {
                        // 更新已有记录
                        HrLeaveBalance bal = existingMap.get(emp.getEmployeeId());
                        usedDays = bal.getUsedDays() != null ? bal.getUsedDays() : BigDecimal.ZERO;
                        bal.setTotalDays(totalDays);
                        bal.setUsedDays(usedDays);
                        bal.setRemainingDays(totalDays.subtract(usedDays));
                        bal.setUpdateBy(operName);
                        hrLeaveBalanceMapper.updateHrLeaveBalance(bal);
                        updateCount++;
                    }
                    else
                    {
                        skipCount++;
                    }
                }
                else
                {
                    // 新建记录
                    HrLeaveBalance bal = new HrLeaveBalance();
                    bal.setEmployeeId(emp.getEmployeeId());
                    bal.setEmployeeNo(emp.getEmployeeNo());
                    bal.setEmployeeName(emp.getEmployeeName());
                    bal.setDeptId(emp.getOrgId());
                    bal.setDeptName(emp.getOrgName());
                    bal.setLeaveType(leaveType);
                    bal.setYear(dto.getYear());
                    bal.setTotalDays(totalDays);
                    bal.setUsedDays(usedDays);
                    bal.setRemainingDays(remainingDays);
                    bal.setCreateBy(operName);
                    bal.setRemark("批量生成");
                    toInsert.add(bal);
                    insertCount++;
                }
            }

            // 批量插入
            if (!toInsert.isEmpty())
            {
                hrLeaveBalanceMapper.batchInsertHrLeaveBalance(toInsert);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("批量生成完成：新增 ").append(insertCount).append(" 条");
        if (updateCount > 0) sb.append("，更新 ").append(updateCount).append(" 条");
        if (skipCount > 0) sb.append("，跳过 ").append(skipCount).append(" 条（已存在）");
        return sb.toString();
    }

    /**
     * 批量导入假期余额（Excel导入）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult importBalance(List<HrLeaveBalance> list, Boolean overwrite, String operName)
    {
        if (list == null || list.isEmpty())
        {
            return AjaxResult.error("导入数据不能为空");
        }
        boolean ov = Boolean.TRUE.equals(overwrite);
        int successNum = 0;
        int failureNum = 0;
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();

        for (HrLeaveBalance bal : list)
        {
            try
            {
                // 校验必填字段：优先用工号，若工号为空则尝试用 employeeId
                if ((bal.getEmployeeNo() == null || bal.getEmployeeNo().isEmpty()) && bal.getEmployeeId() == null)
                {
                    throw new RuntimeException("工号不能为空");
                }
                if (bal.getLeaveType() == null || bal.getLeaveType().isEmpty())
                {
                    throw new RuntimeException("假别不能为空");
                }
                if (bal.getYear() == null)
                {
                    throw new RuntimeException("年度不能为空");
                }
                if (bal.getTotalDays() == null)
                {
                    throw new RuntimeException("总额度不能为空");
                }

                // 若有工号但无 employeeId，则通过工号查员工
                if (bal.getEmployeeId() == null && bal.getEmployeeNo() != null && !bal.getEmployeeNo().isEmpty())
                {
                    HrEmployee empQuery = new HrEmployee();
                    empQuery.setEmployeeNo(bal.getEmployeeNo());
                    List<HrEmployee> emps = hrEmployeeMapper.selectHrEmployeeList(empQuery);
                    if (emps == null || emps.isEmpty())
                    {
                        throw new RuntimeException("未找到工号为[" + bal.getEmployeeNo() + "]的员工");
                    }
                    HrEmployee emp = emps.get(0);
                    bal.setEmployeeId(emp.getEmployeeId());
                    if (bal.getEmployeeName() == null || bal.getEmployeeName().isEmpty()) bal.setEmployeeName(emp.getEmployeeName());
                    bal.setDeptId(emp.getOrgId());
                    bal.setDeptName(emp.getOrgName());
                }

                // 设置默认值
                if (bal.getUsedDays() == null) bal.setUsedDays(BigDecimal.ZERO);
                computeRemainingDays(bal);

                // 查询是否已存在
                List<Long> empIds = new ArrayList<>();
                empIds.add(bal.getEmployeeId());
                List<HrLeaveBalance> existing = hrLeaveBalanceMapper.selectExistingBalance(empIds, bal.getLeaveType(), bal.getYear());

                if (!existing.isEmpty())
                {
                    if (ov)
                    {
                        HrLeaveBalance old = existing.get(0);
                        bal.setBalanceId(old.getBalanceId());
                        bal.setUpdateBy(operName);
                        hrLeaveBalanceMapper.updateHrLeaveBalance(bal);
                        successNum++;
                        successList.add(buildSuccessItem(bal, "更新成功"));
                    }
                    else
                    {
                        failureNum++;
                        failureList.add(buildFailureItem(bal, "已存在，未勾选覆盖选项"));
                    }
                }
                else
                {
                    bal.setCreateBy(operName);
                    hrLeaveBalanceMapper.insertHrLeaveBalance(bal);
                    successNum++;
                    successList.add(buildSuccessItem(bal, "导入成功"));
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String errMsg = e.getMessage();
                if (errMsg != null && errMsg.length() > 200) { errMsg = errMsg.substring(0, 200) + "..."; }
                failureList.add(buildFailureItem(bal, errMsg != null ? errMsg : "未知错误"));
            }
        }

        // 构建返回结果（结构化明细）
        AjaxResult ajax = AjaxResult.success();
        ajax.put("successNum", successNum);
        ajax.put("failureNum", failureNum);
        ajax.put("successList", successList);
        ajax.put("failureList", failureList);
        if (failureNum > 0 && successNum == 0)
        {
            ajax.put("code", 500);
            ajax.put("msg", "导入失败，共 " + failureNum + " 条数据异常");
        }
        else if (failureNum > 0)
        {
            ajax.put("msg", "部分导入成功：成功 " + successNum + " 条，失败 " + failureNum + " 条");
        }
        else
        {
            ajax.put("msg", "导入成功，共 " + successNum + " 条");
        }
        return ajax;
    }

    /**
     * 构建成功明细项
     */
    private Map<String, Object> buildSuccessItem(HrLeaveBalance row, String result)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("employeeNo", row.getEmployeeNo() != null ? row.getEmployeeNo() : "");
        item.put("employeeName", row.getEmployeeName() != null ? row.getEmployeeName() : "");
        item.put("leaveType", row.getLeaveType() != null ? row.getLeaveType() : "");
        item.put("year", row.getYear() != null ? row.getYear().toString() : "");
        item.put("totalDays", row.getTotalDays() != null ? row.getTotalDays().toString() : "");
        item.put("result", result);
        return item;
    }

    /**
     * 构建失败明细项
     */
    private Map<String, Object> buildFailureItem(HrLeaveBalance row, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("employeeNo", row.getEmployeeNo() != null ? row.getEmployeeNo() : "");
        item.put("employeeName", row.getEmployeeName() != null ? row.getEmployeeName() : "");
        item.put("leaveType", row.getLeaveType() != null ? row.getLeaveType() : "");
        item.put("year", row.getYear() != null ? row.getYear().toString() : "");
        item.put("totalDays", row.getTotalDays() != null ? row.getTotalDays().toString() : "");
        item.put("errorReason", errorReason);
        return item;
    }

    /**
     * 根据部门ID列表获取在职员工
     */
    private List<HrEmployee> getEmployeesByDeptIds(List<Long> deptIds)
    {
        List<HrEmployee> all = new ArrayList<>();
        if (deptIds == null || deptIds.isEmpty())
        {
            // 无部门筛选 → 获取全部在职员工
            HrEmployee query = new HrEmployee();
            query.setEmployeeStatus("1"); // 在职
            all = hrEmployeeMapper.selectHrEmployeeList(query);
            // 也包含状态为0的
            query.setEmployeeStatus("0");
            List<HrEmployee> more = hrEmployeeMapper.selectHrEmployeeList(query);
            all.addAll(more);
        }
        else
        {
            for (Long deptId : deptIds)
            {
                HrEmployee query = new HrEmployee();
                query.setOrgId(deptId);
                List<HrEmployee> emps = hrEmployeeMapper.selectHrEmployeeList(query);
                // 过滤在职（status 0=新入职, 1=在职）
                for (HrEmployee emp : emps)
                {
                    String status = emp.getEmployeeStatus();
                    if (status == null || "0".equals(status) || "1".equals(status))
                    {
                        all.add(emp);
                    }
                }
            }
        }
        return all;
    }
}

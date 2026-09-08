package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrSpecialDeduction;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.mapper.HrSpecialDeductionMapper;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.service.IHrSpecialDeductionService;

@Service
public class HrSpecialDeductionServiceImpl implements IHrSpecialDeductionService
{
    @Autowired
    private HrSpecialDeductionMapper hrSpecialDeductionMapper;
    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;

    @Override
    public HrSpecialDeduction selectHrSpecialDeductionById(Long sdId) { return hrSpecialDeductionMapper.selectHrSpecialDeductionById(sdId); }

    @Override
    public List<HrSpecialDeduction> selectHrSpecialDeductionList(HrSpecialDeduction hrSpecialDeduction) { return hrSpecialDeductionMapper.selectHrSpecialDeductionList(hrSpecialDeduction); }

    @Override
    public int insertHrSpecialDeduction(HrSpecialDeduction hrSpecialDeduction) { return hrSpecialDeductionMapper.insertHrSpecialDeduction(hrSpecialDeduction); }

    @Override
    public int updateHrSpecialDeduction(HrSpecialDeduction hrSpecialDeduction) { return hrSpecialDeductionMapper.updateHrSpecialDeduction(hrSpecialDeduction); }

    @Override
    public int deleteHrSpecialDeductionByIds(Long[] sdIds) { return hrSpecialDeductionMapper.deleteHrSpecialDeductionByIds(sdIds); }

    @Override
    public int deleteHrSpecialDeductionById(Long sdId) { return hrSpecialDeductionMapper.deleteHrSpecialDeductionById(sdId); }

    @Override
    public com.ruoyi.common.core.domain.AjaxResult importSpecialDeduction(java.util.List<HrSpecialDeduction> list, boolean updateSupport, String operName)
    {
        if (list == null || list.isEmpty())
        {
            return com.ruoyi.common.core.domain.AjaxResult.error("导入数据不能为空！");
        }
        int success = 0, updated = 0, skipped = 0;
        StringBuilder failMsg = new StringBuilder();
        int failCount = 0;
        int rowNo = 1;
        for (HrSpecialDeduction sd : list)
        {
            rowNo++;
            try
            {
                // 按工号匹配员工
                if (com.ruoyi.common.utils.StringUtils.isEmpty(sd.getEmployeeNo()))
                {
                    failMsg.append("<br/>").append("第").append(rowNo).append("行：工号不能为空");
                    failCount++;
                    continue;
                }
                HrEmployee emp = hrEmployeeMapper.selectHrEmployeeByEmployeeNo(sd.getEmployeeNo());
                if (emp == null)
                {
                    failMsg.append("<br/>").append("第").append(rowNo).append("行：工号 ").append(sd.getEmployeeNo()).append(" 不存在");
                    failCount++;
                    continue;
                }
                sd.setEmployeeId(emp.getEmployeeId());
                sd.setEmployeeName(emp.getEmployeeName());
                if (sd.getDeductionAmount() == null || sd.getDeductionAmount().signum() < 0)
                {
                    failMsg.append("<br/>").append("第").append(rowNo).append("行：月扣除金额不能为空且不能为负");
                    failCount++;
                    continue;
                }
                // 同员工+同扣除项目+同生效日期 判定已存在
                HrSpecialDeduction q = new HrSpecialDeduction();
                q.setEmployeeId(emp.getEmployeeId());
                q.setDeductionType(sd.getDeductionType());
                if (sd.getEffectiveDate() != null) { q.getParams().put("beginEffectiveDate", new java.text.SimpleDateFormat("yyyy-MM-dd").format(sd.getEffectiveDate())); q.getParams().put("endEffectiveDate", new java.text.SimpleDateFormat("yyyy-MM-dd").format(sd.getEffectiveDate())); }
                List<HrSpecialDeduction> existList = hrSpecialDeductionMapper.selectHrSpecialDeductionList(q);
                HrSpecialDeduction exist = (existList == null || existList.isEmpty()) ? null : existList.get(0);
                if (exist != null)
                {
                    if (updateSupport)
                    {
                        sd.setSdId(exist.getSdId());
                        sd.setUpdateBy(operName);
                        hrSpecialDeductionMapper.updateHrSpecialDeduction(sd);
                        updated++;
                    }
                    else
                    {
                        skipped++;
                    }
                }
                else
                {
                    sd.setCreateBy(operName);
                    hrSpecialDeductionMapper.insertHrSpecialDeduction(sd);
                    success++;
                }
            }
            catch (Exception e)
            {
                failCount++;
                failMsg.append("<br/>").append("第").append(rowNo).append("行：").append(e.getMessage());
            }
        }
        if (failCount > 0)
        {
            return com.ruoyi.common.core.domain.AjaxResult.error("很抱歉，导入失败！共 " + failCount + " 条数据格式不正确，错误如下：" + failMsg);
        }
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("success", success);
        data.put("updated", updated);
        data.put("skipped", skipped);
        return com.ruoyi.common.core.domain.AjaxResult.success("导入完成：新增 " + success + " 条，更新 " + updated + " 条，跳过已存在 " + skipped + " 条", data);
    }
}

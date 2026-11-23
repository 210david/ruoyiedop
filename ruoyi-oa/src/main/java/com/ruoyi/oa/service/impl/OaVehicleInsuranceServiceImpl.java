package com.ruoyi.oa.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.oa.domain.OaVehicle;
import com.ruoyi.oa.domain.OaVehicleInsurance;
import com.ruoyi.oa.mapper.OaVehicleInsuranceMapper;
import com.ruoyi.oa.mapper.OaVehicleMapper;
import com.ruoyi.oa.service.IOaVehicleInsuranceService;

@Service
public class OaVehicleInsuranceServiceImpl implements IOaVehicleInsuranceService
{
    @Autowired
    private OaVehicleInsuranceMapper oaVehicleInsuranceMapper;

    @Autowired
    private OaVehicleMapper oaVehicleMapper;

    @Override
    public OaVehicleInsurance selectOaVehicleInsuranceById(Long insuranceId) { return oaVehicleInsuranceMapper.selectOaVehicleInsuranceById(insuranceId); }

    @Override
    public List<OaVehicleInsurance> selectOaVehicleInsuranceList(OaVehicleInsurance oaVehicleInsurance) { return oaVehicleInsuranceMapper.selectOaVehicleInsuranceList(oaVehicleInsurance); }

    @Override
    public AjaxResult importInsurance(List<OaVehicleInsurance> insuranceList, boolean updateSupport, String operName)
    {
        if (StringUtils.isEmpty(insuranceList))
        {
            throw new ServiceException("导入车辆保险数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();
        for (OaVehicleInsurance insurance : insuranceList)
        {
            try
            {
                // 车牌号必须来源于车辆台账（同时校验非空并回填车辆ID）
                syncVehicle(insurance);
                if (insurance.getStartDate() == null)
                {
                    throw new ServiceException("起保日期不能为空");
                }
                if (insurance.getEndDate() == null)
                {
                    throw new ServiceException("到期日期不能为空");
                }

                if (StringUtils.isEmpty(insurance.getStatus()))
                {
                    insurance.setStatus("0");
                }
                else if (StringUtils.isEmpty(DictUtils.getDictLabel("oa_insurance_status", insurance.getStatus())))
                {
                    throw new ServiceException("保险状态【" + insurance.getStatus() + "】不存在");
                }
                // 保单号为空时不参与重复识别，直接新增
                OaVehicleInsurance exist = StringUtils.isEmpty(insurance.getPolicyNo())
                        ? null : oaVehicleInsuranceMapper.selectOaVehicleInsuranceByPolicyNo(insurance.getPolicyNo());
                if (exist != null)
                {
                    if (!updateSupport)
                    {
                        throw new ServiceException("保单号【" + insurance.getPolicyNo() + "】已存在");
                    }
                    insurance.setInsuranceId(exist.getInsuranceId());
                    insurance.setUpdateBy(operName);
                    oaVehicleInsuranceMapper.updateOaVehicleInsurance(insurance);
                    successNum++;
                    successList.add(buildItem(insurance, "更新成功"));
                }
                else
                {
                    insurance.setCreateBy(operName);
                    oaVehicleInsuranceMapper.insertOaVehicleInsurance(insurance);
                    successNum++;
                    successList.add(buildItem(insurance, "新增成功"));
                }
            }
            catch (Exception e)
            {
                failureNum++;
                failureList.add(buildFailureItem(insurance, e.getMessage() != null ? e.getMessage() : "数据格式错误"));
            }
        }
        return buildResult(successNum, failureNum, successList, failureList);
    }

    private Map<String, Object> buildItem(OaVehicleInsurance insurance, String result)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("policyNo", insurance.getPolicyNo() != null ? insurance.getPolicyNo() : "");
        item.put("plateNo", insurance.getPlateNo() != null ? insurance.getPlateNo() : "");
        item.put("insurer", insurance.getInsurer() != null ? insurance.getInsurer() : "");
        item.put("startDate", insurance.getStartDate() != null ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, insurance.getStartDate()) : "");
        item.put("endDate", insurance.getEndDate() != null ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, insurance.getEndDate()) : "");
        item.put("premium", insurance.getPremium() != null ? insurance.getPremium() : "");
        item.put("contactName", insurance.getContactName() != null ? insurance.getContactName() : "");
        item.put("contactPhone", insurance.getContactPhone() != null ? insurance.getContactPhone() : "");
        item.put("status", dictText("oa_insurance_status", insurance.getStatus()));
        item.put("result", result);
        return item;
    }

    private Map<String, Object> buildFailureItem(OaVehicleInsurance insurance, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("policyNo", insurance.getPolicyNo() != null ? insurance.getPolicyNo() : "");
        item.put("plateNo", insurance.getPlateNo() != null ? insurance.getPlateNo() : "");
        item.put("insurer", insurance.getInsurer() != null ? insurance.getInsurer() : "");
        item.put("startDate", insurance.getStartDate() != null ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, insurance.getStartDate()) : "");
        item.put("endDate", insurance.getEndDate() != null ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, insurance.getEndDate()) : "");
        item.put("premium", insurance.getPremium() != null ? insurance.getPremium() : "");
        item.put("contactName", insurance.getContactName() != null ? insurance.getContactName() : "");
        item.put("contactPhone", insurance.getContactPhone() != null ? insurance.getContactPhone() : "");
        item.put("status", dictText("oa_insurance_status", insurance.getStatus()));
        item.put("errorReason", errorReason);
        return item;
    }

    private String dictText(String dictType, String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return "";
        }
        String label = DictUtils.getDictLabel(dictType, value);
        return StringUtils.isNotEmpty(label) ? label : value;
    }

    private AjaxResult buildResult(int successNum, int failureNum, List<Map<String, Object>> successList, List<Map<String, Object>> failureList)
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("successNum", successNum);
        ajax.put("failureNum", failureNum);
        ajax.put("successList", successList);
        ajax.put("failureList", failureList);
        if (failureNum > 0)
        {
            ajax.put("msg", successNum > 0 ? "部分数据导入失败，成功 " + successNum + " 条，失败 " + failureNum + " 条" : "导入失败，共 " + failureNum + " 条数据存在错误");
        }
        else
        {
            ajax.put("msg", "导入成功，共 " + successNum + " 条数据");
        }
        return ajax;
    }

    @Override
    public int insertOaVehicleInsurance(OaVehicleInsurance oaVehicleInsurance)
    {
        syncVehicle(oaVehicleInsurance);
        syncCoverage(oaVehicleInsurance);
        checkPolicyNoUnique(oaVehicleInsurance);
        return oaVehicleInsuranceMapper.insertOaVehicleInsurance(oaVehicleInsurance);
    }

    @Override
    public int updateOaVehicleInsurance(OaVehicleInsurance oaVehicleInsurance)
    {
        syncVehicle(oaVehicleInsurance);
        syncCoverage(oaVehicleInsurance);
        checkPolicyNoUnique(oaVehicleInsurance);
        return oaVehicleInsuranceMapper.updateOaVehicleInsurance(oaVehicleInsurance);
    }

    /**
     * 校验保单期限：起保日期不得晚于到期日期。
     */
    private void syncCoverage(OaVehicleInsurance insurance)
    {
        Date start = insurance.getStartDate();
        Date end = insurance.getEndDate();
        if (start == null || end == null)
        {
            return;
        }
        if (start.after(end))
        {
            throw new ServiceException("起保日期不能晚于到期日期");
        }
    }

    /**
     * 保单号非必填，但填写时不允许与其他记录重复（空值不参与判重）。
     */
    private void checkPolicyNoUnique(OaVehicleInsurance insurance)
    {
        if (StringUtils.isEmpty(insurance.getPolicyNo()))
        {
            return;
        }
        OaVehicleInsurance exist = oaVehicleInsuranceMapper.selectOaVehicleInsuranceByPolicyNo(insurance.getPolicyNo());
        if (exist != null && !exist.getInsuranceId().equals(insurance.getInsuranceId()))
        {
            throw new ServiceException("保单号【" + insurance.getPolicyNo() + "】已存在");
        }
    }

    /**
     * 校验车牌号必须来源于车辆台账，并回填车辆ID。
     * 车牌号由前端「车辆管理」选择器提供，此处兜底防止直接调用接口写入无效车牌。
     */
    private void syncVehicle(OaVehicleInsurance insurance)
    {
        String plateNo = insurance.getPlateNo();
        if (StringUtils.isEmpty(plateNo))
        {
            throw new ServiceException("车牌号不能为空");
        }
        OaVehicle vehicle = oaVehicleMapper.selectOaVehicleByPlateNo(plateNo);
        if (vehicle == null)
        {
            throw new ServiceException("车牌号【" + plateNo + "】在车辆台账中不存在，请从车辆管理中选择");
        }
        insurance.setVehicleId(vehicle.getVehicleId());
    }

    @Override
    public int deleteOaVehicleInsuranceByIds(Long[] insuranceIds) { return oaVehicleInsuranceMapper.deleteOaVehicleInsuranceByIds(insuranceIds); }

    @Override
    public int deleteOaVehicleInsuranceById(Long insuranceId) { return oaVehicleInsuranceMapper.deleteOaVehicleInsuranceById(insuranceId); }
}

package com.ruoyi.oa.service.impl;

import java.util.ArrayList;
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
import com.ruoyi.oa.domain.OaDriver;
import com.ruoyi.oa.mapper.OaDriverMapper;
import com.ruoyi.oa.service.IOaDriverService;

@Service
public class OaDriverServiceImpl implements IOaDriverService
{
    @Autowired
    private OaDriverMapper oaDriverMapper;

    @Override
    public OaDriver selectOaDriverById(Long driverId) { return oaDriverMapper.selectOaDriverById(driverId); }

    @Override
    public List<OaDriver> selectOaDriverList(OaDriver oaDriver) { return oaDriverMapper.selectOaDriverList(oaDriver); }

    @Override
    public AjaxResult importDriver(List<OaDriver> driverList, boolean updateSupport, String operName)
    {
        if (StringUtils.isEmpty(driverList))
        {
            throw new ServiceException("导入司机数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();
        for (OaDriver driver : driverList)
        {
            try
            {
                if (StringUtils.isEmpty(driver.getDriverName()))
                {
                    throw new ServiceException("司机姓名不能为空");
                }
                if (StringUtils.isEmpty(driver.getStatus()))
                {
                    driver.setStatus("0");
                }
                else if (StringUtils.isEmpty(DictUtils.getDictLabel("oa_driver_status", driver.getStatus())))
                {
                    throw new ServiceException("司机状态【" + driver.getStatus() + "】不存在");
                }
                OaDriver exist = StringUtils.isNotEmpty(driver.getDriverNo()) ? oaDriverMapper.selectOaDriverByNo(driver.getDriverNo()) : null;
                if (exist != null)
                {
                    if (!updateSupport)
                    {
                        throw new ServiceException("工号【" + driver.getDriverNo() + "】已存在");
                    }
                    driver.setDriverId(exist.getDriverId());
                    driver.setUpdateBy(operName);
                    oaDriverMapper.updateOaDriver(driver);
                    successNum++;
                    successList.add(buildItem(driver, "更新成功"));
                }
                else
                {
                    driver.setCreateBy(operName);
                    oaDriverMapper.insertOaDriver(driver);
                    successNum++;
                    successList.add(buildItem(driver, "新增成功"));
                }
            }
            catch (Exception e)
            {
                failureNum++;
                failureList.add(buildFailureItem(driver, e.getMessage() != null ? e.getMessage() : "数据格式错误"));
            }
        }
        return buildResult(successNum, failureNum, successList, failureList);
    }

    private Map<String, Object> buildItem(OaDriver driver, String result)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("driverName", driver.getDriverName() != null ? driver.getDriverName() : "");
        item.put("driverNo", driver.getDriverNo() != null ? driver.getDriverNo() : "");
        item.put("phone", driver.getPhone() != null ? driver.getPhone() : "");
        item.put("licenseType", driver.getLicenseType() != null ? driver.getLicenseType() : "");
        item.put("licenseNo", driver.getLicenseNo() != null ? driver.getLicenseNo() : "");
        item.put("licenseExpire", driver.getLicenseExpire() != null ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, driver.getLicenseExpire()) : "");
        item.put("entryDate", driver.getEntryDate() != null ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, driver.getEntryDate()) : "");
        item.put("ownerDept", driver.getOwnerDept() != null ? driver.getOwnerDept() : "");
        item.put("drivingYears", driver.getDrivingYears() != null ? driver.getDrivingYears() : "");
        item.put("status", dictText("oa_driver_status", driver.getStatus()));
        item.put("result", result);
        return item;
    }

    private Map<String, Object> buildFailureItem(OaDriver driver, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("driverName", driver.getDriverName() != null ? driver.getDriverName() : "");
        item.put("driverNo", driver.getDriverNo() != null ? driver.getDriverNo() : "");
        item.put("phone", driver.getPhone() != null ? driver.getPhone() : "");
        item.put("licenseType", driver.getLicenseType() != null ? driver.getLicenseType() : "");
        item.put("licenseNo", driver.getLicenseNo() != null ? driver.getLicenseNo() : "");
        item.put("licenseExpire", driver.getLicenseExpire() != null ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, driver.getLicenseExpire()) : "");
        item.put("entryDate", driver.getEntryDate() != null ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, driver.getEntryDate()) : "");
        item.put("ownerDept", driver.getOwnerDept() != null ? driver.getOwnerDept() : "");
        item.put("drivingYears", driver.getDrivingYears() != null ? driver.getDrivingYears() : "");
        item.put("status", dictText("oa_driver_status", driver.getStatus()));
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
    public int insertOaDriver(OaDriver oaDriver) { return oaDriverMapper.insertOaDriver(oaDriver); }

    @Override
    public int updateOaDriver(OaDriver oaDriver) { return oaDriverMapper.updateOaDriver(oaDriver); }

    @Override
    public int deleteOaDriverByIds(Long[] driverIds) { return oaDriverMapper.deleteOaDriverByIds(driverIds); }

    @Override
    public int deleteOaDriverById(Long driverId) { return oaDriverMapper.deleteOaDriverById(driverId); }
}

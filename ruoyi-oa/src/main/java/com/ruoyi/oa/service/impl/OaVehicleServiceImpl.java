package com.ruoyi.oa.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.oa.domain.OaVehicle;
import com.ruoyi.oa.mapper.OaVehicleMapper;
import com.ruoyi.oa.service.IOaVehicleService;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.service.ISysDeptService;

@Service
public class OaVehicleServiceImpl implements IOaVehicleService
{
    @Autowired
    private OaVehicleMapper oaVehicleMapper;

    @Autowired
    private ISysDeptService sysDeptService;

    @Override
    public OaVehicle selectOaVehicleById(Long vehicleId) { return oaVehicleMapper.selectOaVehicleById(vehicleId); }

    @Override
    public List<OaVehicle> selectOaVehicleList(OaVehicle oaVehicle) { return oaVehicleMapper.selectOaVehicleList(oaVehicle); }

    @Override
    public AjaxResult importVehicle(List<OaVehicle> vehicleList, boolean updateSupport, String operName)
    {
        if (StringUtils.isEmpty(vehicleList))
        {
            throw new ServiceException("导入车辆数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();
        for (OaVehicle vehicle : vehicleList)
        {
            try
            {
                if (StringUtils.isEmpty(vehicle.getPlateNo()))
                {
                    throw new ServiceException("车牌号不能为空");
                }
                if (StringUtils.isNotEmpty(vehicle.getVehicleType()) && StringUtils.isEmpty(DictUtils.getDictLabel("oa_vehicle_type", vehicle.getVehicleType())))
                {
                    throw new ServiceException("车辆类型【" + vehicle.getVehicleType() + "】不存在");
                }
                if (StringUtils.isEmpty(vehicle.getStatus()))
                {
                    vehicle.setStatus("0");
                }
                else if (StringUtils.isEmpty(DictUtils.getDictLabel("oa_vehicle_status", vehicle.getStatus())))
                {
                    throw new ServiceException("车辆状态【" + vehicle.getStatus() + "】不存在");
                }
                OaVehicle exist = oaVehicleMapper.selectOaVehicleByPlateNo(vehicle.getPlateNo());
                if (exist != null)
                {
                    if (!updateSupport)
                    {
                        throw new ServiceException("车牌号【" + vehicle.getPlateNo() + "】已存在");
                    }
                    vehicle.setVehicleId(exist.getVehicleId());
                    vehicle.setUpdateBy(operName);
                    normalizeOwnerDept(vehicle);
                    oaVehicleMapper.updateOaVehicle(vehicle);
                    successNum++;
                    successList.add(buildItem(vehicle, "更新成功"));
                }
                else
                {
                    vehicle.setCreateBy(operName);
                    normalizeOwnerDept(vehicle);
                    oaVehicleMapper.insertOaVehicle(vehicle);
                    successNum++;
                    successList.add(buildItem(vehicle, "新增成功"));
                }
            }
            catch (Exception e)
            {
                failureNum++;
                failureList.add(buildFailureItem(vehicle, e.getMessage() != null ? e.getMessage() : "数据格式错误"));
            }
        }
        return buildResult(successNum, failureNum, successList, failureList);
    }

    private Map<String, Object> buildItem(OaVehicle vehicle, String result)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("vehicleCode", vehicle.getVehicleCode() != null ? vehicle.getVehicleCode() : "");
        item.put("plateNo", vehicle.getPlateNo() != null ? vehicle.getPlateNo() : "");
        item.put("vehicleType", dictText("oa_vehicle_type", vehicle.getVehicleType()));
        item.put("vehicleModel", vehicle.getVehicleModel() != null ? vehicle.getVehicleModel() : "");
        item.put("vehicleUse", vehicle.getVehicleUse() != null ? vehicle.getVehicleUse() : "");
        item.put("seatCount", vehicle.getSeatCount() != null ? vehicle.getSeatCount() : "");
        item.put("ownerDept", vehicle.getOwnerDept() != null ? vehicle.getOwnerDept() : "");
        item.put("driverName", vehicle.getDriverName() != null ? vehicle.getDriverName() : "");
        item.put("driverPhone", vehicle.getDriverPhone() != null ? vehicle.getDriverPhone() : "");
        item.put("status", dictText("oa_vehicle_status", vehicle.getStatus()));
        item.put("result", result);
        return item;
    }

    private Map<String, Object> buildFailureItem(OaVehicle vehicle, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("vehicleCode", vehicle.getVehicleCode() != null ? vehicle.getVehicleCode() : "");
        item.put("plateNo", vehicle.getPlateNo() != null ? vehicle.getPlateNo() : "");
        item.put("vehicleType", dictText("oa_vehicle_type", vehicle.getVehicleType()));
        item.put("vehicleModel", vehicle.getVehicleModel() != null ? vehicle.getVehicleModel() : "");
        item.put("vehicleUse", vehicle.getVehicleUse() != null ? vehicle.getVehicleUse() : "");
        item.put("seatCount", vehicle.getSeatCount() != null ? vehicle.getSeatCount() : "");
        item.put("ownerDept", vehicle.getOwnerDept() != null ? vehicle.getOwnerDept() : "");
        item.put("driverName", vehicle.getDriverName() != null ? vehicle.getDriverName() : "");
        item.put("driverPhone", vehicle.getDriverPhone() != null ? vehicle.getDriverPhone() : "");
        item.put("status", dictText("oa_vehicle_status", vehicle.getStatus()));
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
    public int insertOaVehicle(OaVehicle oaVehicle)
    {
        normalizeOwnerDept(oaVehicle);
        return oaVehicleMapper.insertOaVehicle(oaVehicle);
    }

    @Override
    public int updateOaVehicle(OaVehicle oaVehicle)
    {
        normalizeOwnerDept(oaVehicle);
        return oaVehicleMapper.updateOaVehicle(oaVehicle);
    }

    /**
     * 所属部门一致性校正：
     * 1) 传了部门ID（弹框选择）→ 以部门表中的名称为准回填 ownerDept；
     * 2) 只传了名称（Excel 导入）→ 按名称唯一匹配部门ID，匹配不到则保留名称、ID 为空。
     */
    private void normalizeOwnerDept(OaVehicle vehicle)
    {
        if (vehicle.getOwnerDeptId() != null)
        {
            SysDept dept = sysDeptService.selectDeptById(vehicle.getOwnerDeptId());
            if (dept != null)
            {
                vehicle.setOwnerDept(dept.getDeptName());
            }
            return;
        }
        String name = StringUtils.trim(vehicle.getOwnerDept());
        if (StringUtils.isEmpty(name))
        {
            return;
        }
        List<Long> matched = oaVehicleMapper.selectDeptIdsByExactName(name);
        if (matched != null && matched.size() == 1)
        {
            vehicle.setOwnerDeptId(matched.get(0));
        }
    }

    @Override
    public int deleteOaVehicleByIds(Long[] vehicleIds) { return oaVehicleMapper.deleteOaVehicleByIds(vehicleIds); }

    @Override
    public int deleteOaVehicleById(Long vehicleId) { return oaVehicleMapper.deleteOaVehicleById(vehicleId); }
}

package com.ruoyi.oa.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.oa.domain.OaVehicle;
import com.ruoyi.oa.service.IOaVehicleService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 车辆管理
 */
@RestController
@RequestMapping("/oa/vehicle")
public class OaVehicleController extends BaseController
{
    @Autowired
    private IOaVehicleService oaVehicleService;

    @PreAuthorize("@ss.hasPermi('oa:vehicle:list')")
    @GetMapping("/list")
    public TableDataInfo list(OaVehicle oaVehicle)
    {
        startPage();
        List<OaVehicle> list = oaVehicleService.selectOaVehicleList(oaVehicle);
        return getDataTable(list);
    }

    /**
     * 流程表单浏览框数据源：可用车辆列表（仅需登录，不要求 OA 菜单权限，供公车使用申请等流程弹框选择）
     */
    @GetMapping("/pickerList")
    public TableDataInfo pickerList(OaVehicle oaVehicle)
    {
        // 可用 + 使用中：占用中的车辆仍可被预约到其他时段
        oaVehicle.getParams().put("statusIn", java.util.Arrays.asList("0", "1"));
        startPage();
        List<OaVehicle> list = oaVehicleService.selectOaVehicleList(oaVehicle);
        return getDataTable(list);
    }

    @Log(title = "车辆管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('oa:vehicle:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, OaVehicle oaVehicle)
    {
        List<OaVehicle> list = oaVehicleService.selectOaVehicleList(oaVehicle);
        ExcelUtil<OaVehicle> util = new ExcelUtil<>(OaVehicle.class);
        util.exportExcel(response, list, "车辆台账");
    }

    @Log(title = "车辆管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('oa:vehicle:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<OaVehicle> util = new ExcelUtil<>(OaVehicle.class);
        List<OaVehicle> vehicleList = util.importExcel(file.getInputStream());
        return oaVehicleService.importVehicle(vehicleList, updateSupport, getUsername());
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<OaVehicle> util = new ExcelUtil<>(OaVehicle.class);
        util.importTemplateExcel(response, "车辆台账");
    }

    @PreAuthorize("@ss.hasPermi('oa:vehicle:query')")
    @GetMapping(value = "/{vehicleId}")
    public AjaxResult getInfo(@PathVariable("vehicleId") Long vehicleId)
    {
        return AjaxResult.success(oaVehicleService.selectOaVehicleById(vehicleId));
    }

    @PreAuthorize("@ss.hasPermi('oa:vehicle:add')")
    @Log(title = "车辆管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody OaVehicle oaVehicle)
    {
        oaVehicle.setCreateBy(getUsername());
        return toAjax(oaVehicleService.insertOaVehicle(oaVehicle));
    }

    @PreAuthorize("@ss.hasPermi('oa:vehicle:edit')")
    @Log(title = "车辆管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody OaVehicle oaVehicle)
    {
        oaVehicle.setUpdateBy(getUsername());
        return toAjax(oaVehicleService.updateOaVehicle(oaVehicle));
    }

    @PreAuthorize("@ss.hasPermi('oa:vehicle:remove')")
    @Log(title = "车辆管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{vehicleIds}")
    public AjaxResult remove(@PathVariable Long[] vehicleIds)
    {
        return toAjax(oaVehicleService.deleteOaVehicleByIds(vehicleIds));
    }
}

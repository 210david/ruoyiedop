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
import com.ruoyi.common.utils.ExportIdFilter;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.oa.domain.OaVehicleInsurance;
import com.ruoyi.oa.service.IOaVehicleInsuranceService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 车辆保险管理
 */
@RestController
@RequestMapping("/oa/insurance")
public class OaVehicleInsuranceController extends BaseController
{
    @Autowired
    private IOaVehicleInsuranceService oaVehicleInsuranceService;

    @PreAuthorize("@ss.hasPermi('oa:insurance:list')")
    @GetMapping("/list")
    public TableDataInfo list(OaVehicleInsurance oaVehicleInsurance)
    {
        startPage();
        List<OaVehicleInsurance> list = oaVehicleInsuranceService.selectOaVehicleInsuranceList(oaVehicleInsurance);
        return getDataTable(list);
    }

    @Log(title = "车辆保险管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('oa:insurance:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, OaVehicleInsurance oaVehicleInsurance)
    {
        List<OaVehicleInsurance> list = oaVehicleInsuranceService.selectOaVehicleInsuranceList(oaVehicleInsurance);
        // 勾选了数据时按勾选导出，未勾选按筛选条件导出全部
        list = ExportIdFilter.filter(list, oaVehicleInsurance);
        ExcelUtil<OaVehicleInsurance> util = new ExcelUtil<>(OaVehicleInsurance.class);
        util.exportExcel(response, list, "车辆保险台账");
    }

    @Log(title = "车辆保险管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('oa:insurance:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<OaVehicleInsurance> util = new ExcelUtil<>(OaVehicleInsurance.class);
        List<OaVehicleInsurance> insuranceList = util.importExcel(file.getInputStream());
        return oaVehicleInsuranceService.importInsurance(insuranceList, updateSupport, getUsername());
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<OaVehicleInsurance> util = new ExcelUtil<>(OaVehicleInsurance.class);
        util.importTemplateExcel(response, "车辆保险台账");
    }

    @PreAuthorize("@ss.hasPermi('oa:insurance:query')")
    @GetMapping(value = "/{insuranceId}")
    public AjaxResult getInfo(@PathVariable("insuranceId") Long insuranceId)
    {
        return AjaxResult.success(oaVehicleInsuranceService.selectOaVehicleInsuranceById(insuranceId));
    }

    @PreAuthorize("@ss.hasPermi('oa:insurance:add')")
    @Log(title = "车辆保险管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody OaVehicleInsurance oaVehicleInsurance)
    {
        oaVehicleInsurance.setCreateBy(getUsername());
        return toAjax(oaVehicleInsuranceService.insertOaVehicleInsurance(oaVehicleInsurance));
    }

    @PreAuthorize("@ss.hasPermi('oa:insurance:edit')")
    @Log(title = "车辆保险管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody OaVehicleInsurance oaVehicleInsurance)
    {
        oaVehicleInsurance.setUpdateBy(getUsername());
        return toAjax(oaVehicleInsuranceService.updateOaVehicleInsurance(oaVehicleInsurance));
    }

    @PreAuthorize("@ss.hasPermi('oa:insurance:remove')")
    @Log(title = "车辆保险管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{insuranceIds}")
    public AjaxResult remove(@PathVariable Long[] insuranceIds)
    {
        return toAjax(oaVehicleInsuranceService.deleteOaVehicleInsuranceByIds(insuranceIds));
    }
}

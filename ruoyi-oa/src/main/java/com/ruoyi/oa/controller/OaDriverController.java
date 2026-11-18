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
import com.ruoyi.oa.domain.OaDriver;
import com.ruoyi.oa.service.IOaDriverService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 司机管理
 */
@RestController
@RequestMapping("/oa/driver")
public class OaDriverController extends BaseController
{
    @Autowired
    private IOaDriverService oaDriverService;

    @PreAuthorize("@ss.hasPermi('oa:driver:list')")
    @GetMapping("/list")
    public TableDataInfo list(OaDriver oaDriver)
    {
        startPage();
        List<OaDriver> list = oaDriverService.selectOaDriverList(oaDriver);
        return getDataTable(list);
    }

    /**
     * 流程表单浏览框数据源：在岗司机列表（仅需登录，不要求 OA 菜单权限，供公车使用申请等流程弹框选择）
     */
    @GetMapping("/pickerList")
    public TableDataInfo pickerList(OaDriver oaDriver)
    {
        // 在岗 + 出车中：占用中的司机仍可被预约到其他时段
        oaDriver.getParams().put("statusIn", java.util.Arrays.asList("0", "3"));
        startPage();
        List<OaDriver> list = oaDriverService.selectOaDriverList(oaDriver);
        return getDataTable(list);
    }

    @Log(title = "司机管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('oa:driver:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, OaDriver oaDriver)
    {
        List<OaDriver> list = oaDriverService.selectOaDriverList(oaDriver);
        ExcelUtil<OaDriver> util = new ExcelUtil<>(OaDriver.class);
        util.exportExcel(response, list, "司机台账");
    }

    @Log(title = "司机管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('oa:driver:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<OaDriver> util = new ExcelUtil<>(OaDriver.class);
        List<OaDriver> driverList = util.importExcel(file.getInputStream());
        return oaDriverService.importDriver(driverList, updateSupport, getUsername());
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<OaDriver> util = new ExcelUtil<>(OaDriver.class);
        util.importTemplateExcel(response, "司机台账");
    }

    @PreAuthorize("@ss.hasPermi('oa:driver:query')")
    @GetMapping(value = "/{driverId}")
    public AjaxResult getInfo(@PathVariable("driverId") Long driverId)
    {
        return AjaxResult.success(oaDriverService.selectOaDriverById(driverId));
    }

    @PreAuthorize("@ss.hasPermi('oa:driver:add')")
    @Log(title = "司机管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody OaDriver oaDriver)
    {
        oaDriver.setCreateBy(getUsername());
        return toAjax(oaDriverService.insertOaDriver(oaDriver));
    }

    @PreAuthorize("@ss.hasPermi('oa:driver:edit')")
    @Log(title = "司机管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody OaDriver oaDriver)
    {
        oaDriver.setUpdateBy(getUsername());
        return toAjax(oaDriverService.updateOaDriver(oaDriver));
    }

    @PreAuthorize("@ss.hasPermi('oa:driver:remove')")
    @Log(title = "司机管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{driverIds}")
    public AjaxResult remove(@PathVariable Long[] driverIds)
    {
        return toAjax(oaDriverService.deleteOaDriverByIds(driverIds));
    }
}

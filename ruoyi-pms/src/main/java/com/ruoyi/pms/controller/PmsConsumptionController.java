package com.ruoyi.pms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.pms.domain.PmsConsumption;
import com.ruoyi.pms.service.IPmsConsumptionService;

/**
 * 零散采购耗用登记Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/consumption")
public class PmsConsumptionController extends BaseController
{
    @Autowired
    private IPmsConsumptionService pmsConsumptionService;

    /**
     * 查询耗用登记列表
     */
    @PreAuthorize("@ss.hasPermi('pms:consumption:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsConsumption consumption)
    {
        startPage();
        List<PmsConsumption> list = pmsConsumptionService.selectConsumptionList(consumption);
        return getDataTable(list);
    }

    /**
     * 获取耗用登记详情
     */
    @PreAuthorize("@ss.hasPermi('pms:consumption:query')")
    @GetMapping("/{consumptionId}")
    public AjaxResult getInfo(@PathVariable Long consumptionId)
    {
        return success(pmsConsumptionService.selectConsumptionById(consumptionId));
    }

    /**
     * 新增耗用登记
     */
    @PreAuthorize("@ss.hasPermi('pms:consumption:add')")
    @Log(title = "耗用登记", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PmsConsumption consumption)
    {
        return toAjax(pmsConsumptionService.insertConsumption(consumption));
    }

    /**
     * 修改耗用登记
     */
    @PreAuthorize("@ss.hasPermi('pms:consumption:edit')")
    @Log(title = "耗用登记", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PmsConsumption consumption)
    {
        return toAjax(pmsConsumptionService.updateConsumption(consumption));
    }

    /**
     * 删除耗用登记
     */
    @PreAuthorize("@ss.hasPermi('pms:consumption:remove')")
    @Log(title = "耗用登记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{consumptionIds}")
    public AjaxResult remove(@PathVariable Long[] consumptionIds)
    {
        return toAjax(pmsConsumptionService.deleteConsumptionByIds(consumptionIds));
    }
}

package com.ruoyi.web.controller.food;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.food.domain.FoodWip;
import com.ruoyi.food.domain.FoodWipEnv;
import com.ruoyi.food.service.IFoodWipService;

/**
 * 在制品台账 Controller
 *
 * 长周期WIP按垛/批跟踪：当前工序位置、入柜出柜时间、温湿度记录、上霉状态
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/food/wip")
public class FoodWipController extends BaseController
{
    @Autowired
    private IFoodWipService wipService;

    @PreAuthorize("@ss.hasPermi('food:wip:list')")
    @GetMapping("/list")
    public TableDataInfo list(FoodWip wip)
    {
        startPage();
        List<FoodWip> list = wipService.selectWipList(wip);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('food:wip:export')")
    @Log(title = "在制品台账", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FoodWip wip)
    {
        List<FoodWip> list = wipService.selectWipList(wip);
        ExcelUtil<FoodWip> util = new ExcelUtil<FoodWip>(FoodWip.class);
        util.exportExcel(response, list, "在制品台账");
    }

    @PreAuthorize("@ss.hasPermi('food:wip:query')")
    @GetMapping("/{wipId}")
    public AjaxResult getInfo(@PathVariable Long wipId)
    {
        return success(wipService.selectWipById(wipId));
    }

    @PreAuthorize("@ss.hasPermi('food:wip:add')")
    @Log(title = "在制品台账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FoodWip wip)
    {
        return toAjax(wipService.insertWip(wip));
    }

    @PreAuthorize("@ss.hasPermi('food:wip:edit')")
    @Log(title = "在制品台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FoodWip wip)
    {
        return toAjax(wipService.updateWip(wip));
    }

    @PreAuthorize("@ss.hasPermi('food:wip:edit')")
    @Log(title = "在制品台账-完成", businessType = BusinessType.UPDATE)
    @PutMapping("/finish/{wipId}")
    public AjaxResult finish(@PathVariable Long wipId)
    {
        return toAjax(wipService.finishWip(wipId));
    }

    @PreAuthorize("@ss.hasPermi('food:wip:edit')")
    @Log(title = "在制品台账-关闭", businessType = BusinessType.UPDATE)
    @PutMapping("/close/{wipId}")
    public AjaxResult close(@PathVariable Long wipId)
    {
        return toAjax(wipService.closeWip(wipId));
    }

    @PreAuthorize("@ss.hasPermi('food:wip:remove')")
    @Log(title = "在制品台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{wipIds}")
    public AjaxResult remove(@PathVariable Long[] wipIds)
    {
        return toAjax(wipService.deleteWipByIds(wipIds));
    }

    @PreAuthorize("@ss.hasPermi('food:wip:query')")
    @GetMapping("/{wipId}/env")
    public AjaxResult envList(@PathVariable Long wipId)
    {
        return success(wipService.selectEnvList(wipId));
    }

    @PreAuthorize("@ss.hasPermi('food:wip:env')")
    @Log(title = "在制品温湿度记录", businessType = BusinessType.INSERT)
    @PostMapping("/env")
    public AjaxResult addEnv(@RequestBody FoodWipEnv env)
    {
        return toAjax(wipService.addEnv(env));
    }

    @PreAuthorize("@ss.hasPermi('food:wip:env')")
    @Log(title = "在制品温湿度记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/env/{envId}")
    public AjaxResult removeEnv(@PathVariable Long envId)
    {
        return toAjax(wipService.deleteEnvById(envId));
    }

    @PreAuthorize("@ss.hasPermi('food:wip:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return success(wipService.dashboard());
    }
}

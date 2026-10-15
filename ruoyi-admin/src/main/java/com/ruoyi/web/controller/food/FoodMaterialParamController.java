package com.ruoyi.web.controller.food;

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
import com.ruoyi.food.domain.FoodMaterialParam;
import com.ruoyi.food.service.IFoodMaterialParamService;

/**
 * 物料行业参数 Controller
 *
 * 配方双模型（产出率/配比）与排产双模式（批次计划/离散日排产）配置
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/food/materialParam")
public class FoodMaterialParamController extends BaseController
{
    @Autowired
    private IFoodMaterialParamService paramService;

    @PreAuthorize("@ss.hasPermi('food:param:list')")
    @GetMapping("/list")
    public TableDataInfo list(FoodMaterialParam param)
    {
        startPage();
        List<FoodMaterialParam> list = paramService.selectParamList(param);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('food:param:query')")
    @GetMapping("/{paramId}")
    public AjaxResult getInfo(@PathVariable Long paramId)
    {
        return success(paramService.selectParamById(paramId));
    }

    @PreAuthorize("@ss.hasPermi('food:param:query')")
    @GetMapping("/material/{materialId}")
    public AjaxResult getByMaterial(@PathVariable Long materialId)
    {
        return success(paramService.selectParamByMaterialId(materialId));
    }

    @PreAuthorize("@ss.hasPermi('food:param:add')")
    @Log(title = "物料行业参数", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FoodMaterialParam param)
    {
        return toAjax(paramService.insertParam(param));
    }

    @PreAuthorize("@ss.hasPermi('food:param:edit')")
    @Log(title = "物料行业参数", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FoodMaterialParam param)
    {
        return toAjax(paramService.updateParam(param));
    }

    @PreAuthorize("@ss.hasPermi('food:param:remove')")
    @Log(title = "物料行业参数", businessType = BusinessType.DELETE)
    @DeleteMapping("/{paramIds}")
    public AjaxResult remove(@PathVariable Long[] paramIds)
    {
        return toAjax(paramService.deleteParamByIds(paramIds));
    }
}

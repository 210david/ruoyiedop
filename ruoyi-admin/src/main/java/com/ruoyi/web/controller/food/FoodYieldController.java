package com.ruoyi.web.controller.food;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.food.domain.FoodYieldRow;
import com.ruoyi.food.service.IFoodYieldService;

/**
 * 产出率损耗核算 Controller
 *
 * 完工分析产出率视图：投入→理论产出→实际产出→产出率/损耗率→分环节损耗→告警
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/food/yield")
public class FoodYieldController extends BaseController
{
    @Autowired
    private IFoodYieldService yieldService;

    @PreAuthorize("@ss.hasPermi('food:yield:list')")
    @GetMapping("/analysis")
    public TableDataInfo analysis(FoodYieldRow query)
    {
        startPage();
        List<FoodYieldRow> list = yieldService.selectYieldAnalysis(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('food:yield:export')")
    @Log(title = "产出率核算", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FoodYieldRow query)
    {
        List<FoodYieldRow> list = yieldService.selectYieldAnalysis(query);
        ExcelUtil<FoodYieldRow> util = new ExcelUtil<FoodYieldRow>(FoodYieldRow.class);
        util.exportExcel(response, list, "产出率损耗核算");
    }
}

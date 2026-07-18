package com.ruoyi.wms.controller;

import java.util.List;
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
import com.ruoyi.wms.domain.vo.WmsStatisticsVO;
import com.ruoyi.wms.service.IWmsStatisticsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 库存预警 Controller
 *
 * 提供库存不足、库存积压、临期预警、过期预警的统一查询入口。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/wms/stockAlert")
public class WmsStockAlertController extends BaseController
{
    @Autowired
    private IWmsStatisticsService wmsStatisticsService;

    /**
     * 库存预警列表（分页，支持按预警类型/物料/仓库筛选）
     */
    @PreAuthorize("@ss.hasPermi('wms:stockAlert:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsStatisticsVO query)
    {
        startPage();
        List<WmsStatisticsVO> list = wmsStatisticsService.selectStockAlertList(query);
        return getDataTable(list);
    }

    /**
     * 库存预警汇总（各类型预警数量）
     */
    @PreAuthorize("@ss.hasPermi('wms:stockAlert:list')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return AjaxResult.success(wmsStatisticsService.selectStockAlertSummary());
    }

    /**
     * 导出库存预警数据
     */
    @Log(title = "库存预警", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('wms:stockAlert:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsStatisticsVO query)
    {
        List<WmsStatisticsVO> list = wmsStatisticsService.selectStockAlertList(query);
        ExcelUtil<WmsStatisticsVO> util = new ExcelUtil<>(WmsStatisticsVO.class);
        util.exportExcel(response, list, "库存预警数据");
    }

    /**
     * 删除库存预警（根据预警类型处理底层数据）
     */
    @Log(title = "库存预警", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('wms:stockAlert:remove')")
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody WmsStatisticsVO alert)
    {
        return toAjax(wmsStatisticsService.deleteStockAlert(alert));
    }
}

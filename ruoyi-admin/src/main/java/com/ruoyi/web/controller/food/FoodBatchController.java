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
import com.ruoyi.food.domain.FoodBatch;
import com.ruoyi.food.domain.FoodBatchGenealogy;
import com.ruoyi.food.domain.FoodBatchReserve;
import com.ruoyi.food.domain.FoodBatchStock;
import com.ruoyi.food.domain.FoodBatchStatusLog;
import com.ruoyi.food.service.IFoodBatchService;

/**
 * 食品批次台账Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/food/batch")
public class FoodBatchController extends BaseController
{
    @Autowired
    private IFoodBatchService batchService;

    /**
     * 查询批次台账
     */
    @PreAuthorize("@ss.hasPermi('food:batch:list')")
    @GetMapping("/list")
    public TableDataInfo list(FoodBatch batch)
    {
        startPage();
        List<FoodBatch> list = batchService.selectBatchList(batch);
        return getDataTable(list);
    }

    /**
     * 批次导出
     */
    @PreAuthorize("@ss.hasPermi('food:batch:export')")
    @Log(title = "食品批次台账", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FoodBatch batch)
    {
        List<FoodBatch> list = batchService.selectBatchList(batch);
        ExcelUtil<FoodBatch> util = new ExcelUtil<FoodBatch>(FoodBatch.class);
        util.exportExcel(response, list, "批次台账");
    }

    /**
     * 批次详情
     */
    @PreAuthorize("@ss.hasPermi('food:batch:query')")
    @GetMapping(value = "/{batchId}")
    public AjaxResult getInfo(@PathVariable("batchId") Long batchId)
    {
        return success(batchService.selectBatchById(batchId));
    }

    /**
     * 新增批次（初始化来源，批次号自动生成）
     */
    @PreAuthorize("@ss.hasPermi('food:batch:add')")
    @Log(title = "食品批次台账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FoodBatch batch)
    {
        batch.setCreateBy(getUsername());
        return toAjax(batchService.insertBatch(batch));
    }

    /**
     * 修改批次（批次号禁改）
     */
    @PreAuthorize("@ss.hasPermi('food:batch:edit')")
    @Log(title = "食品批次台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FoodBatch batch)
    {
        batch.setUpdateBy(getUsername());
        return toAjax(batchService.updateBatch(batch));
    }

    /**
     * 删除批次
     */
    @PreAuthorize("@ss.hasPermi('food:batch:remove')")
    @Log(title = "食品批次台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{batchIds}")
    public AjaxResult remove(@PathVariable Long[] batchIds)
    {
        return toAjax(batchService.deleteBatchByIds(batchIds));
    }

    /**
     * 合格判定
     */
    @PreAuthorize("@ss.hasPermi('food:batch:qualify')")
    @Log(title = "食品批次台账", businessType = BusinessType.UPDATE)
    @PutMapping("/qualify/{batchId}")
    public AjaxResult qualify(@PathVariable Long batchId, @RequestBody(required = false) FoodBatch body)
    {
        return toAjax(batchService.qualifyBatch(batchId, body == null ? null : body.getRemark()));
    }

    /**
     * 冻结
     */
    @PreAuthorize("@ss.hasPermi('food:batch:freeze')")
    @Log(title = "食品批次台账", businessType = BusinessType.UPDATE)
    @PutMapping("/freeze/{batchId}")
    public AjaxResult freeze(@PathVariable Long batchId, @RequestBody(required = false) FoodBatch body)
    {
        return toAjax(batchService.freezeBatch(batchId, body == null ? null : body.getFreezeReason()));
    }

    /**
     * 解冻
     */
    @PreAuthorize("@ss.hasPermi('food:batch:freeze')")
    @Log(title = "食品批次台账", businessType = BusinessType.UPDATE)
    @PutMapping("/unfreeze/{batchId}")
    public AjaxResult unfreeze(@PathVariable Long batchId)
    {
        return toAjax(batchService.unfreezeBatch(batchId));
    }

    /**
     * 报废
     */
    @PreAuthorize("@ss.hasPermi('food:batch:scrap')")
    @Log(title = "食品批次台账", businessType = BusinessType.UPDATE)
    @PutMapping("/scrap/{batchId}")
    public AjaxResult scrap(@PathVariable Long batchId, @RequestBody(required = false) FoodBatch body)
    {
        return toAjax(batchService.scrapBatch(batchId, body == null ? null : body.getFreezeReason()));
    }

    /**
     * 谱系查询（direction: up向上追溯 / down向下追踪）
     */
    @PreAuthorize("@ss.hasPermi('food:batch:query')")
    @GetMapping("/genealogy/{batchId}")
    public AjaxResult genealogy(@PathVariable Long batchId, String direction)
    {
        List<FoodBatchGenealogy> list = batchService.selectGenealogy(batchId, direction);
        return success(list);
    }

    /**
     * 批次库存查询
     */
    @PreAuthorize("@ss.hasPermi('food:batch:query')")
    @GetMapping("/stock/list")
    public AjaxResult stockList(FoodBatchStock stock)
    {
        return success(batchService.selectBatchStockList(stock));
    }

    /**
     * 按物料查批次库存行（含批次状态/效期/仓库，供领料/退料批次弹框选择）
     */
    @PreAuthorize("@ss.hasPermi('food:batch:query')")
    @GetMapping("/stock/listByMaterial/{materialId}")
    public AjaxResult stockListByMaterial(@PathVariable Long materialId)
    {
        return success(batchService.listBatchStocksByMaterial(materialId));
    }

    /**
     * 存量库存迁移：现存库存生成初始批次并补录效期（幂等可重复执行）
     */
    @PreAuthorize("@ss.hasPermi('food:batch:edit')")
    @Log(title = "批次存量迁移", businessType = BusinessType.UPDATE)
    @PostMapping("/migrateStock")
    public AjaxResult migrateStock()
    {
        return success(batchService.migrateLegacyStock());
    }

    /**
     * 批次状态日志查询
     */
    @PreAuthorize("@ss.hasPermi('food:batch:query')")
    @GetMapping("/statusLog/{batchId}")
    public AjaxResult statusLog(@PathVariable Long batchId)
    {
        List<FoodBatchStatusLog> list = batchService.selectStatusLogList(batchId);
        return success(list);
    }

    // ---------------- 预留 ----------------

    /**
     * 预留记录查询
     */
    @PreAuthorize("@ss.hasPermi('food:batch:query')")
    @GetMapping("/reserve/list")
    public TableDataInfo reserveList(FoodBatchReserve reserve)
    {
        startPage();
        List<FoodBatchReserve> list = batchService.selectReserveList(reserve);
        return getDataTable(list);
    }

    /**
     * 新增预留
     */
    @PreAuthorize("@ss.hasPermi('food:batch:edit')")
    @Log(title = "批次预留", businessType = BusinessType.INSERT)
    @PostMapping("/reserve")
    public AjaxResult addReserve(@RequestBody FoodBatchReserve reserve)
    {
        reserve.setCreateBy(getUsername());
        return toAjax(batchService.insertReserve(reserve));
    }

    /**
     * 释放预留
     */
    @PreAuthorize("@ss.hasPermi('food:batch:edit')")
    @Log(title = "批次预留", businessType = BusinessType.UPDATE)
    @PutMapping("/reserve/release/{reserveId}")
    public AjaxResult releaseReserve(@PathVariable Long reserveId)
    {
        return toAjax(batchService.releaseReserve(reserveId));
    }

    /**
     * 删除预留
     */
    @PreAuthorize("@ss.hasPermi('food:batch:remove')")
    @Log(title = "批次预留", businessType = BusinessType.DELETE)
    @DeleteMapping("/reserve/{reserveIds}")
    public AjaxResult removeReserve(@PathVariable Long[] reserveIds)
    {
        return toAjax(batchService.deleteReserveByIds(reserveIds));
    }
}

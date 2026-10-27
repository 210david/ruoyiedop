package com.ruoyi.web.controller.food;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.food.domain.FoodRecall;
import com.ruoyi.food.domain.FoodRecallBatch;
import com.ruoyi.food.service.IFoodRecallService;

/**
 * 食品批次召回隔离 Controller
 *
 * 一键按谱系圈批冻结：GB/T 22005 追溯合规、召回演练核心动作
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/food/recall")
public class FoodRecallController extends BaseController
{
    @Autowired
    private IFoodRecallService recallService;

    @PreAuthorize("@ss.hasPermi('food:recall:list')")
    @GetMapping("/list")
    public TableDataInfo list(FoodRecall recall)
    {
        startPage();
        return getDataTable(recallService.selectRecallList(recall));
    }

    @PreAuthorize("@ss.hasPermi('food:recall:query')")
    @GetMapping("/{recallId}")
    public AjaxResult getInfo(@PathVariable Long recallId)
    {
        return AjaxResult.success(recallService.selectRecallById(recallId));
    }

    /**
     * 圈批预览：按谱系正/反向遍历关联批次（不落库不冻结）
     */
    @PreAuthorize("@ss.hasPermi('food:recall:query')")
    @GetMapping("/circles")
    public AjaxResult circles(@RequestParam Long triggerBatchId,
                              @RequestParam(defaultValue = "3") String scope)
    {
        return AjaxResult.success(recallService.circles(triggerBatchId, scope));
    }

    /**
     * 召回单圈批明细
     */
    @PreAuthorize("@ss.hasPermi('food:recall:query')")
    @GetMapping("/batchList/{recallId}")
    public AjaxResult batchList(@PathVariable Long recallId)
    {
        return AjaxResult.success(recallService.selectRecallBatchList(recallId));
    }

    @Log(title = "批次召回", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('food:recall:add')")
    @PostMapping
    public AjaxResult add(@RequestBody FoodRecall recall)
    {
        return toAjax(recallService.insertRecall(recall));
    }

    /**
     * 执行召回：圈批冻结全部关联批次
     */
    @Log(title = "批次召回执行", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('food:recall:execute')")
    @PostMapping("/execute/{recallId}")
    public AjaxResult execute(@PathVariable Long recallId)
    {
        return AjaxResult.success("召回执行完成，本次冻结 " + recallService.executeRecall(recallId) + " 批");
    }

    @Log(title = "批次召回取消", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('food:recall:cancel')")
    @PostMapping("/cancel/{recallId}")
    public AjaxResult cancel(@PathVariable Long recallId)
    {
        return toAjax(recallService.cancelRecall(recallId));
    }

    @Log(title = "批次召回删除", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('food:recall:remove')")
    @DeleteMapping("/{recallIds}")
    public AjaxResult remove(@PathVariable Long[] recallIds)
    {
        return toAjax(recallService.deleteRecallByIds(recallIds));
    }

    @Log(title = "批次召回导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('food:recall:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FoodRecall recall)
    {
        List<FoodRecall> list = recallService.selectRecallList(recall);
        ExcelUtil<FoodRecall> util = new ExcelUtil<>(FoodRecall.class);
        util.exportExcel(response, list, "批次召回单");
    }

    @Log(title = "召回圈批明细导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('food:recall:export')")
    @PostMapping("/batchExport/{recallId}")
    public void batchExport(HttpServletResponse response, @PathVariable Long recallId)
    {
        List<FoodRecallBatch> list = recallService.selectRecallBatchList(recallId);
        ExcelUtil<FoodRecallBatch> util = new ExcelUtil<>(FoodRecallBatch.class);
        util.exportExcel(response, list, "召回圈批明细");
    }
}

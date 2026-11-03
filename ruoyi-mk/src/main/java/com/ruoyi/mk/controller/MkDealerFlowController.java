package com.ruoyi.mk.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mk.domain.MkDealerSellFlow;
import com.ruoyi.mk.service.IMkDealerSellFlowService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 经销商分销流向登记 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/dealerFlow")
public class MkDealerFlowController extends BaseController
{
    @Autowired
    private IMkDealerSellFlowService mkDealerSellFlowService;

    /** 流向登记列表 */
    @PreAuthorize("@ss.hasPermi('marketing:flow:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkDealerSellFlow flow)
    {
        startPage();
        List<MkDealerSellFlow> list = mkDealerSellFlowService.selectFlowList(flow);
        return getDataTable(list);
    }

    /** 导出流向登记 */
    @Log(title = "经销商流向登记", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:flow:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkDealerSellFlow flow)
    {
        List<MkDealerSellFlow> list = mkDealerSellFlowService.selectFlowList(flow);
        ExcelUtil<MkDealerSellFlow> util = new ExcelUtil<>(MkDealerSellFlow.class);
        util.exportExcel(response, list, "经销商流向登记");
    }

    /** 流向详情 */
    @PreAuthorize("@ss.hasPermi('marketing:flow:query')")
    @GetMapping(value = "/{flowId}")
    public AjaxResult getInfo(@PathVariable("flowId") Long flowId)
    {
        return AjaxResult.success(mkDealerSellFlowService.selectFlowById(flowId));
    }

    /** 按发货单生成流向草稿（不入库） */
    @PreAuthorize("@ss.hasPermi('marketing:flow:add')")
    @GetMapping("/draft/{shipmentId}")
    public AjaxResult draft(@PathVariable("shipmentId") Long shipmentId)
    {
        return AjaxResult.success(mkDealerSellFlowService.generateDraftByShipment(shipmentId));
    }

    /** 手工新增流向 */
    @Log(title = "经销商流向登记", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:flow:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MkDealerSellFlow flow)
    {
        return toAjax(mkDealerSellFlowService.insertFlow(flow));
    }

    /** 批量保存草稿（防重，已登记跳过） */
    @Log(title = "经销商流向批量登记", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:flow:add')")
    @PostMapping("/batchSave")
    public AjaxResult batchSave(@RequestBody List<MkDealerSellFlow> flows)
    {
        int saved = mkDealerSellFlowService.batchSaveFlows(flows, SecurityUtils.getUsername());
        return AjaxResult.success("已保存 " + saved + " 条流向记录（重复项已自动跳过）");
    }

    /** 修改流向 */
    @Log(title = "经销商流向登记", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:flow:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MkDealerSellFlow flow)
    {
        return toAjax(mkDealerSellFlowService.updateFlow(flow));
    }

    /** 删除流向 */
    @Log(title = "经销商流向登记", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:flow:remove')")
    @DeleteMapping("/{flowIds}")
    public AjaxResult remove(@PathVariable Long[] flowIds)
    {
        return toAjax(mkDealerSellFlowService.deleteFlowByIds(flowIds));
    }
}

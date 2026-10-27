package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsBatchTrace;
import com.ruoyi.mms.service.IMmsBatchTraceService;
import com.ruoyi.qms.domain.QmsLotGenealogy;
import com.ruoyi.qms.service.IQmsTraceService;
import com.ruoyi.qms.domain.vo.TraceFallbackVO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 批次追溯 Controller
 *
 * 列表数据为实时计算（齐套明细×工单×退料）；
 * 正反向追溯树复用 QMS 谱系引擎（qms_lot_genealogy），权限挂在 mms:trace:* 下，
 * 生产用户无需授予质量模块权限。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/trace")
public class MmsBatchTraceController extends BaseController
{
    @Autowired
    private IMmsBatchTraceService mmsBatchTraceService;

    @Autowired
    private IQmsTraceService qmsTraceService;

    @PreAuthorize("@ss.hasPermi('mms:trace:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsBatchTrace trace)
    {
        startPage();
        List<MmsBatchTrace> list = mmsBatchTraceService.selectBatchTraceList(trace);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mms:trace:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsBatchTrace trace)
    {
        List<MmsBatchTrace> list = mmsBatchTraceService.selectBatchTraceList(trace);
        list = com.ruoyi.common.utils.ExportIdFilter.filter(list, trace);
        ExcelUtil<MmsBatchTrace> util = new ExcelUtil<>(MmsBatchTrace.class);
        util.exportExcel(response, list, "批次追溯数据");
    }

    @PreAuthorize("@ss.hasPermi('mms:trace:query')")
    @GetMapping(value = "/{traceId}")
    public AjaxResult getInfo(@PathVariable("traceId") Long traceId)
    {
        return AjaxResult.success(mmsBatchTraceService.selectBatchTraceById(traceId));
    }

    /**
     * 正向追溯（物料批次 → 下游工单/成品/客户），复用 QMS 谱系引擎
     */
    @PreAuthorize("@ss.hasPermi('mms:trace:query')")
    @GetMapping("/forward/{batchNo}")
    public AjaxResult forwardTrace(@PathVariable String batchNo)
    {
        return AjaxResult.success(qmsTraceService.forwardTrace(batchNo));
    }

    /**
     * 反向追溯（成品批次 → 上游物料/供应商/检验/NCR），复用 QMS 谱系引擎
     */
    @PreAuthorize("@ss.hasPermi('mms:trace:query')")
    @GetMapping("/backward/{batchNo}")
    public AjaxResult backwardTrace(@PathVariable String batchNo)
    {
        return AjaxResult.success(qmsTraceService.backwardTrace(batchNo));
    }

    /**
     * 降级追溯：谱系无数据时按批次号从业务单据（检验/NCR/库存/收货/发货/客诉）辅助关联
     */
    @PreAuthorize("@ss.hasPermi('mms:trace:query')")
    @GetMapping("/fallback/{batchNo}")
    public AjaxResult fallbackTrace(@PathVariable String batchNo,
            @RequestParam(value = "direction", defaultValue = "backward") String direction)
    {
        return AjaxResult.success(qmsTraceService.fallbackTrace(batchNo, direction));
    }
}

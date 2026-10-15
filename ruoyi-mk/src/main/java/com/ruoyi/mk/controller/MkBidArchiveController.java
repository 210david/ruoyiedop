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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mk.domain.MkBidArchive;
import com.ruoyi.mk.service.IMkBidArchiveService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 招投标档案 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/bid")
public class MkBidArchiveController extends BaseController
{
    @Autowired
    private IMkBidArchiveService mkBidArchiveService;

    @PreAuthorize("@ss.hasPermi('marketing:bid:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkBidArchive bid)
    {
        startPage();
        List<MkBidArchive> list = mkBidArchiveService.selectBidList(bid);
        return getDataTable(list);
    }

    @Log(title = "招投标档案", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:bid:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkBidArchive bid)
    {
        List<MkBidArchive> list = mkBidArchiveService.selectBidList(bid);
        list = com.ruoyi.common.utils.ExportIdFilter.filter(list, bid);
        ExcelUtil<MkBidArchive> util = new ExcelUtil<>(MkBidArchive.class);
        util.exportExcel(response, list, "招投标档案数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:bid:query')")
    @GetMapping("/{bidId}")
    public AjaxResult getInfo(@PathVariable Long bidId)
    {
        return AjaxResult.success(mkBidArchiveService.selectBidById(bidId));
    }

    @Log(title = "招投标档案", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:bid:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MkBidArchive bid)
    {
        return toAjax(mkBidArchiveService.insertBid(bid));
    }

    @Log(title = "招投标档案", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:bid:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MkBidArchive bid)
    {
        return toAjax(mkBidArchiveService.updateBid(bid));
    }

    @Log(title = "招投标档案", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:bid:remove')")
    @DeleteMapping("/{bidIds}")
    public AjaxResult remove(@PathVariable Long[] bidIds)
    {
        return toAjax(mkBidArchiveService.deleteBidByIds(bidIds));
    }

    @Log(title = "招投标开标结果", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:bid:win')")
    @PutMapping("/markResult")
    public AjaxResult markResult(@RequestBody MkBidArchive body)
    {
        return toAjax(mkBidArchiveService.markResult(body.getBidId(), body.getBidStatus(),
                body.getWinAmount(), body.getResultDate()));
    }

    @Log(title = "投标生成合同", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:bid:contract')")
    @PutMapping("/createContract/{bidId}")
    public AjaxResult createContract(@PathVariable Long bidId)
    {
        return AjaxResult.success(mkBidArchiveService.createContractFromBid(bidId));
    }
}

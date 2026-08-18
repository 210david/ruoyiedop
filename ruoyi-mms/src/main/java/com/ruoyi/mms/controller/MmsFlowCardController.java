package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsFlowCard;
import com.ruoyi.mms.service.IMmsFlowCardService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 流转卡 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/flowcard")
public class MmsFlowCardController extends BaseController
{
    @Autowired
    private IMmsFlowCardService mmsFlowCardService;

    // ========== 标准 CRUD ==========

    /**
     * 查询流转卡列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('mms:flowcard:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsFlowCard flowCard)
    {
        startPage();
        List<MmsFlowCard> list = mmsFlowCardService.selectFlowCardList(flowCard);
        return getDataTable(list);
    }

    /**
     * 导出 Excel
     */
    @Log(title = "流转卡", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:flowcard:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsFlowCard flowCard)
    {
        List<MmsFlowCard> list = mmsFlowCardService.selectFlowCardList(flowCard);
        ExcelUtil<MmsFlowCard> util = new ExcelUtil<>(MmsFlowCard.class);
        util.exportExcel(response, list, "流转卡数据");
    }

    /**
     * 根据ID获取流转卡详情
     */
    @PreAuthorize("@ss.hasPermi('mms:flowcard:query')")
    @GetMapping(value = "/{cardId}")
    public AjaxResult getInfo(@PathVariable("cardId") Long cardId)
    {
        return AjaxResult.success(mmsFlowCardService.selectFlowCardById(cardId));
    }

    /**
     * 新增流转卡
     */
    @Log(title = "流转卡", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:flowcard:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MmsFlowCard flowCard)
    {
        return toAjax(mmsFlowCardService.insertFlowCard(flowCard));
    }

    /**
     * 修改流转卡
     */
    @Log(title = "流转卡", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:flowcard:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MmsFlowCard flowCard)
    {
        return toAjax(mmsFlowCardService.updateFlowCard(flowCard));
    }

    /**
     * 删除流转卡（软删除）
     */
    @Log(title = "流转卡", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:flowcard:remove')")
    @DeleteMapping("/{cardIds}")
    public AjaxResult remove(@PathVariable Long[] cardIds)
    {
        return toAjax(mmsFlowCardService.deleteFlowCardByIds(cardIds));
    }

    // ========== 业务操作 ==========

    /**
     * 打印流转卡（增加打印次数）
     */
    @Log(title = "流转卡-打印", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:flowcard:print')")
    @PutMapping("/print/{cardId}")
    public AjaxResult print(@PathVariable("cardId") Long cardId)
    {
        return toAjax(mmsFlowCardService.printFlowCard(cardId));
    }

    /**
     * 扫码定位（返回当前工序与历史）
     */
    @PreAuthorize("@ss.hasPermi('mms:flowcard:scan')")
    @GetMapping("/scan/{cardCode}")
    public AjaxResult scan(@PathVariable("cardCode") String cardCode)
    {
        return AjaxResult.success(mmsFlowCardService.scanFlowCard(cardCode));
    }
}

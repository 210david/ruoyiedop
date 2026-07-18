package com.ruoyi.dms.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.dms.domain.DmsRepairFaq;
import com.ruoyi.dms.service.IDmsAiService;
import com.ruoyi.dms.service.IDmsRepairFaqService;

@RestController
@RequestMapping("/dms/ai")
public class DmsAiController extends BaseController
{
    @Autowired
    private IDmsRepairFaqService faqService;
    @Autowired
    private IDmsAiService dmsAiService;

    // ===== FAQ CRUD =====

    @PreAuthorize("@ss.hasPermi('dms:ai:faq:list')")
    @GetMapping("/faq/list")
    public TableDataInfo faqList(DmsRepairFaq faq)
    {
        startPage();
        List<DmsRepairFaq> list = faqService.selectFaqList(faq);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('dms:ai:faq:query')")
    @GetMapping("/faq/{faqId}")
    public AjaxResult getFaq(@PathVariable Long faqId)
    {
        return AjaxResult.success(faqService.selectFaqById(faqId));
    }

    @Log(title = "维修FAQ", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('dms:ai:faq:add')")
    @PostMapping("/faq")
    public AjaxResult addFaq(@Validated @RequestBody DmsRepairFaq faq)
    {
        return toAjax(faqService.insertFaq(faq));
    }

    @Log(title = "维修FAQ", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:ai:faq:edit')")
    @PutMapping("/faq")
    public AjaxResult editFaq(@Validated @RequestBody DmsRepairFaq faq)
    {
        return toAjax(faqService.updateFaq(faq));
    }

    @Log(title = "维修FAQ", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:ai:faq:remove')")
    @DeleteMapping("/faq/{faqIds}")
    public AjaxResult removeFaq(@PathVariable Long[] faqIds)
    {
        return toAjax(faqService.deleteFaqByIds(faqIds));
    }

    // ===== AI 智能报修助手 =====

    @PostMapping("/repairAssistant")
    public AjaxResult repairAssistant(@RequestBody Map<String, String> body)
    {
        String input = body.get("input");
        if (input == null || input.trim().isEmpty())
        {
            return AjaxResult.error("请输入故障描述");
        }
        return AjaxResult.success(dmsAiService.repairAssistant(input));
    }

    // ===== AI 智能派工推荐 =====

    @GetMapping("/dispatchRecommend/{orderId}")
    public AjaxResult dispatchRecommend(@PathVariable Long orderId)
    {
        return AjaxResult.success(dmsAiService.dispatchRecommend(orderId));
    }

    // ===== AI 维修知识问答 =====

    @PostMapping("/knowledgeQA")
    public AjaxResult knowledgeQA(@RequestBody Map<String, String> body)
    {
        String question = body.get("question");
        if (question == null || question.trim().isEmpty())
        {
            return AjaxResult.error("请输入问题");
        }
        return AjaxResult.success(dmsAiService.knowledgeQA(question));
    }
}

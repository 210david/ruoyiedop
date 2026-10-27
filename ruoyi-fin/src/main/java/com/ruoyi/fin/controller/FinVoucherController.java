package com.ruoyi.fin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fin.domain.FinVoucher;
import com.ruoyi.fin.domain.dto.VoucherDTO;
import com.ruoyi.fin.service.IFinVoucherService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 记账凭证 Controller
 *
 * <p>【内控红线】</p>
 * <ul>
 *   <li>借贷平衡由 Service 强制校验（{@code fin:gl:entry:add}）</li>
 *   <li>审核与制单分离：会计无 audit 权限，且 Service 层再校验 create_by != audit_by</li>
 *   <li>已审核凭证只能红冲，不允许直接改</li>
 * </ul>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fin/voucher")
public class FinVoucherController extends BaseController
{
    @Autowired
    private IFinVoucherService voucherService;

    /**
     * 查询凭证列表
     */
    @PreAuthorize("@ss.hasPermi('fin:gl:query')")
    @GetMapping("/list")
    public TableDataInfo list(FinVoucher voucher)
    {
        startPage();
        List<FinVoucher> list = voucherService.selectVoucherList(voucher);
        return getDataTable(list);
    }

    /**
     * 查询凭证详情（含分录与借贷合计）
     */
    @PreAuthorize("@ss.hasPermi('fin:gl:query')")
    @GetMapping("/{voucherId}")
    public AjaxResult getInfo(@PathVariable("voucherId") Long voucherId)
    {
        return AjaxResult.success(voucherService.selectVoucherDetail(voucherId));
    }

    /**
     * 新增凭证
     *
     * <p>借贷不平衡、期间已结账、科目非明细 均会被 Service 拒绝。</p>
     */
    @Log(title = "记账凭证", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:gl:entry:add')")
    @PostMapping
    public AjaxResult add(@RequestBody VoucherDTO dto)
    {
        return AjaxResult.success(voucherService.insertVoucher(dto));
    }

    /**
     * 修改凭证（仅草稿）
     */
    @Log(title = "记账凭证", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:gl:voucher:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody VoucherDTO dto)
    {
        return toAjax(voucherService.updateVoucher(dto));
    }

    /**
     * 删除凭证（仅草稿）
     */
    @Log(title = "记账凭证", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('fin:gl:voucher:remove')")
    @DeleteMapping("/{voucherIds}")
    public AjaxResult remove(@PathVariable Long[] voucherIds)
    {
        return toAjax(voucherService.deleteVoucherByIds(voucherIds));
    }

    /**
     * 审核凭证
     */
    @Log(title = "记账凭证审核", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:gl:voucher:audit')")
    @PutMapping("/audit/{voucherId}")
    public AjaxResult audit(@PathVariable Long voucherId,
                            @RequestParam(required = false) String auditOpinion)
    {
        return toAjax(voucherService.auditVoucher(voucherId, auditOpinion));
    }

    /**
     * 反审核（仅财务经理）
     */
    @Log(title = "记账凭证反审核", businessType = BusinessType.FORCE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:gl:voucher:cancel')")
    @PutMapping("/cancelAudit/{voucherId}")
    public AjaxResult cancelAudit(@PathVariable Long voucherId)
    {
        return toAjax(voucherService.cancelAudit(voucherId));
    }

    /**
     * 试算平衡表
     */
    @PreAuthorize("@ss.hasPermi('fin:report:trial')")
    @GetMapping("/trialBalance")
    public AjaxResult trialBalance(@RequestParam(required = false) String period)
    {
        Map<String, Object> result = voucherService.trialBalance(period);
        return AjaxResult.success(result);
    }

    /**
     * 科目余额表
     */
    @PreAuthorize("@ss.hasPermi('fin:report:balance')")
    @GetMapping("/subjectBalance")
    public AjaxResult subjectBalance(@RequestParam String period)
    {
        return AjaxResult.success(voucherService.subjectBalance(period));
    }

    /**
     * 导出凭证
     */
    @Log(title = "记账凭证", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:gl:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FinVoucher voucher)
    {
        List<FinVoucher> list = voucherService.selectVoucherList(voucher);
        ExcelUtil<FinVoucher> util = new ExcelUtil<>(FinVoucher.class);
        util.exportExcel(response, list, "记账凭证数据");
    }

    /**
     * 红字冲销：对已审核凭证生成负数冲销凭证（草稿，须人工审核）
     */
    @Log(title = "凭证红字冲销", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:gl:voucher:redflush')")
    @PostMapping("/redflush/{voucherId}")
    public AjaxResult redFlush(@PathVariable Long voucherId)
    {
        return AjaxResult.success(voucherService.redFlush(voucherId));
    }
}

package com.ruoyi.pdm.controller;

import java.util.List;
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
import com.ruoyi.pdm.domain.PdmChangeOrder;
import com.ruoyi.pdm.service.IPdmChangeOrderService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 技术变更单 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pdm/change")
public class PdmChangeOrderController extends BaseController
{
    @Autowired
    private IPdmChangeOrderService changeService;

    /**
     * 查询变更单列表（变更台账）
     */
    @PreAuthorize("@ss.hasPermi('pdm:change:list')")
    @GetMapping("/list")
    public TableDataInfo list(PdmChangeOrder change)
    {
        startPage();
        List<PdmChangeOrder> list = changeService.selectChangeList(change);
        return getDataTable(list);
    }

    /**
     * 导出变更台账
     */
    @Log(title = "技术变更单", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pdm:change:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PdmChangeOrder change)
    {
        List<PdmChangeOrder> list = changeService.selectChangeList(change);
        ExcelUtil<PdmChangeOrder> util = new ExcelUtil<>(PdmChangeOrder.class);
        util.exportExcel(response, list, "技术变更台账");
    }

    /**
     * 列出产品当前生效资料清单（供变更单勾选涉及资料）
     */
    @PreAuthorize("@ss.hasPermi('pdm:change:query')")
    @GetMapping("/effectiveDocs/{productId}")
    public AjaxResult effectiveDocs(@PathVariable("productId") Long productId)
    {
        return success(changeService.listEffectiveDocs(productId));
    }

    /**
     * 获取变更单详情（含涉及资料明细）
     */
    @PreAuthorize("@ss.hasPermi('pdm:change:query')")
    @GetMapping(value = "/{changeId}")
    public AjaxResult getInfo(@PathVariable("changeId") Long changeId)
    {
        return success(changeService.selectChangeById(changeId));
    }

    /**
     * 新增变更单（草稿）
     */
    @Log(title = "技术变更单", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pdm:change:add')")
    @PostMapping
    public AjaxResult add(@RequestBody PdmChangeOrder change)
    {
        return toAjax(changeService.insertChange(change));
    }

    /**
     * 修改变更单（仅草稿/已驳回）
     */
    @Log(title = "技术变更单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:change:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody PdmChangeOrder change)
    {
        return toAjax(changeService.updateChange(change));
    }

    /**
     * 删除变更单（仅草稿；进入流程后记录不可删除）
     */
    @Log(title = "技术变更单", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pdm:change:remove')")
    @DeleteMapping("/{changeIds}")
    public AjaxResult remove(@PathVariable Long[] changeIds)
    {
        return toAjax(changeService.deleteChangeByIds(changeIds));
    }

    /**
     * 提交审批
     */
    @Log(title = "变更提交审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:change:submit')")
    @PutMapping("/submit/{changeId}")
    public AjaxResult submit(@PathVariable Long changeId)
    {
        return toAjax(changeService.submitChange(changeId));
    }

    /**
     * 变更审批（通过→待执行 / 驳回→已驳回）
     */
    @Log(title = "变更审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:change:audit')")
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody PdmChangeOrder params)
    {
        if (params.getApproved() == null)
        {
            return error("请明确审批结论（通过/驳回）");
        }
        return toAjax(changeService.auditChange(params.getChangeId(), params.getApproved(), params.getAuditOpinion()));
    }

    /**
     * 执行明细：按变更单对资料升版生效
     */
    @Log(title = "变更执行升版", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:change:execute')")
    @PutMapping("/execute")
    public AjaxResult execute(@RequestBody PdmChangeOrder params)
    {
        return toAjax(changeService.executeItem(params.getChangeId(), params.getItemId()));
    }

    /**
     * 关闭变更单（校验涉及资料全部升版生效）
     */
    @Log(title = "变更关闭", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:change:close')")
    @PutMapping("/close/{changeId}")
    public AjaxResult close(@PathVariable Long changeId)
    {
        return toAjax(changeService.closeChange(changeId));
    }
}

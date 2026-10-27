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
import com.ruoyi.common.utils.ExportIdFilter;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.food.domain.FoodCcpRecord;
import com.ruoyi.food.domain.FoodCcpTemplate;
import com.ruoyi.food.service.IFoodCcpService;

/**
 * HACCP CCP电子台账 Controller
 *
 * 关键限值校验、超限自动联动不合格品控制（NCR）与批次冻结；
 * 监控记录不可删除（验厂留痕），支持按时段导出应对验厂。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/food/ccp")
public class FoodCcpController extends BaseController
{
    @Autowired
    private IFoodCcpService ccpService;

    // ================= 模板（配置驱动） =================

    /** 模板列表（录入下拉与模板设置共用） */
    @GetMapping("/template/list")
    public TableDataInfo templateList(FoodCcpTemplate template)
    {
        startPage();
        List<FoodCcpTemplate> list = ccpService.selectTemplateList(template);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('food:ccp:template')")
    @Log(title = "CCP控制点模板", businessType = BusinessType.INSERT)
    @PostMapping("/template")
    public AjaxResult addTemplate(@RequestBody FoodCcpTemplate template)
    {
        return toAjax(ccpService.insertTemplate(template));
    }

    @PreAuthorize("@ss.hasPermi('food:ccp:template')")
    @Log(title = "CCP控制点模板", businessType = BusinessType.UPDATE)
    @PutMapping("/template")
    public AjaxResult editTemplate(@RequestBody FoodCcpTemplate template)
    {
        return toAjax(ccpService.updateTemplate(template));
    }

    @PreAuthorize("@ss.hasPermi('food:ccp:template')")
    @Log(title = "CCP控制点模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/template/{templateIds}")
    public AjaxResult removeTemplate(@PathVariable Long[] templateIds)
    {
        return toAjax(ccpService.deleteTemplateByIds(templateIds));
    }

    // ================= 监控记录（不可删除，验厂留痕） =================

    @PreAuthorize("@ss.hasPermi('food:ccp:list')")
    @GetMapping("/list")
    public TableDataInfo list(FoodCcpRecord record)
    {
        startPage();
        List<FoodCcpRecord> list = ccpService.selectRecordList(record);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('food:ccp:export')")
    @Log(title = "CCP电子台账", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FoodCcpRecord record)
    {
        List<FoodCcpRecord> list = ccpService.selectRecordList(record);
        list = ExportIdFilter.filter(list, record);
        ExcelUtil<FoodCcpRecord> util = new ExcelUtil<FoodCcpRecord>(FoodCcpRecord.class);
        util.exportExcel(response, list, "CCP电子台账");
    }

    @PreAuthorize("@ss.hasPermi('food:ccp:query')")
    @GetMapping("/{recordId}")
    public AjaxResult getInfo(@PathVariable Long recordId)
    {
        return success(ccpService.selectRecordById(recordId));
    }

    @PreAuthorize("@ss.hasPermi('food:ccp:add')")
    @Log(title = "CCP电子台账-录入", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FoodCcpRecord record)
    {
        return toAjax(ccpService.insertCcpRecord(record));
    }

    /** 补充记录：仅允许补充偏差说明/纠正措施 */
    @PreAuthorize("@ss.hasPermi('food:ccp:add')")
    @Log(title = "CCP电子台账-补充记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FoodCcpRecord record)
    {
        return toAjax(ccpService.updateRecord(record));
    }

    /** 台账汇总（记录数/合格/超限） */
    @PreAuthorize("@ss.hasPermi('food:ccp:list')")
    @GetMapping("/stats")
    public AjaxResult stats(FoodCcpRecord record)
    {
        return success(ccpService.statsSummary(record));
    }
}

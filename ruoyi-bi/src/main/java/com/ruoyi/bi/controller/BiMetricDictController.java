package com.ruoyi.bi.controller;

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
import com.ruoyi.bi.domain.BiMetricAlert;
import com.ruoyi.bi.domain.BiMetricCheckLog;
import com.ruoyi.bi.domain.BiMetricDict;
import com.ruoyi.bi.domain.BiMetricDictLog;
import com.ruoyi.bi.service.IBiMetricService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 指标字典管理 Controller
 *
 * 口径定义中心的管理入口：所有增改删均写 bi_metric_dict_log 留痕（PRD v1.2 F1.2），
 * 口径变更必须可追溯。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/bi/metric")
public class BiMetricDictController extends BaseController
{
    @Autowired
    private IBiMetricService metricService;

    /**
     * 指标字典分页列表
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:list')")
    @GetMapping("/list")
    public TableDataInfo list(BiMetricDict biMetricDict)
    {
        startPage();
        List<BiMetricDict> list = metricService.selectDictList(biMetricDict);
        return getDataTable(list);
    }

    /**
     * 指标字典导出
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:export')")
    @Log(title = "指标字典", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(jakarta.servlet.http.HttpServletResponse response, BiMetricDict biMetricDict)
    {
        List<BiMetricDict> list = metricService.selectDictList(biMetricDict);
        ExcelUtil<BiMetricDict> util = new ExcelUtil<BiMetricDict>(BiMetricDict.class);
        util.exportExcel(response, list, "指标字典数据");
    }

    /**
     * 指标字典详情
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(metricService.selectDictById(id));
    }

    /**
     * 新增指标字典
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:add')")
    @Log(title = "指标字典", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BiMetricDict biMetricDict)
    {
        if (!metricService.checkMetricCodeUnique(biMetricDict))
        {
            return error("新增失败，指标编码 " + biMetricDict.getMetricCode() + " 已存在");
        }
        biMetricDict.setCreateBy(getUsername());
        return toAjax(metricService.insertDict(biMetricDict));
    }

    /**
     * 修改指标字典（口径变更逐字段留痕）
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:edit')")
    @Log(title = "指标字典", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BiMetricDict biMetricDict)
    {
        if (!metricService.checkMetricCodeUnique(biMetricDict))
        {
            return error("修改失败，指标编码 " + biMetricDict.getMetricCode() + " 已存在");
        }
        biMetricDict.setUpdateBy(getUsername());
        return toAjax(metricService.updateDict(biMetricDict));
    }

    /**
     * 删除指标字典（逻辑删除 + 留痕）
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:remove')")
    @Log(title = "指标字典", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(metricService.deleteDictByIds(ids, getUsername()));
    }

    /**
     * 指标口径变更留痕查询
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:query')")
    @GetMapping("/changeLog/{metricCode}")
    public AjaxResult changeLog(@PathVariable String metricCode)
    {
        List<BiMetricDictLog> list = metricService.selectLogByMetricCode(metricCode);
        return success(list);
    }

    /**
     * 对账日志查询（仅授权角色可见）
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:check')")
    @GetMapping("/checkLog")
    public TableDataInfo checkLog(BiMetricCheckLog biMetricCheckLog)
    {
        startPage();
        List<BiMetricCheckLog> list = metricService.selectCheckLogList(biMetricCheckLog);
        return getDataTable(list);
    }

    /**
     * F7 预警记录查询（预警历史/推送状态/抑制原因/恢复状态）
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:check')")
    @GetMapping("/alert/list")
    public TableDataInfo alertList(BiMetricAlert biMetricAlert)
    {
        startPage();
        List<BiMetricAlert> list = metricService.selectAlertList(biMetricAlert);
        return getDataTable(list);
    }
}

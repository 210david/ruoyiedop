package com.ruoyi.flowable.controller;

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
import com.ruoyi.flowable.domain.FlowUrgencyLevel;
import com.ruoyi.flowable.service.IFlowUrgencyLevelService;

/**
 * 紧急程度配置 Controller（后台：系统管理→流程管理→紧急程度）
 *
 * 管理系统内置字段 urgency 的可选档位，支持自定义名称/排序/封存。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/urgency")
public class FlowUrgencyLevelController extends BaseController
{
    @Autowired
    private IFlowUrgencyLevelService urgencyLevelService;

    /**
     * 查询紧急程度列表（后台管理）
     */
    @PreAuthorize("@ss.hasPermi('flowable:urgency:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowUrgencyLevel query)
    {
        List<FlowUrgencyLevel> list = urgencyLevelService.selectFlowUrgencyLevelList(query);
        return getDataTable(list);
    }

    /**
     * 可选档位（前端发起页下拉使用，无需权限）
     */
    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(urgencyLevelService.selectSelectableList());
    }

    /**
     * 获取详情
     */
    @PreAuthorize("@ss.hasPermi('flowable:urgency:query')")
    @GetMapping("/{levelId}")
    public AjaxResult getInfo(@PathVariable Long levelId)
    {
        return success(urgencyLevelService.selectFlowUrgencyLevelById(levelId));
    }

    /**
     * 新增紧急程度
     */
    @Log(title = "紧急程度", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('flowable:urgency:add')")
    @PostMapping
    public AjaxResult add(@RequestBody FlowUrgencyLevel level)
    {
        return toAjax(urgencyLevelService.insertFlowUrgencyLevel(level));
    }

    /**
     * 修改紧急程度
     */
    @Log(title = "紧急程度", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:urgency:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody FlowUrgencyLevel level)
    {
        return toAjax(urgencyLevelService.updateFlowUrgencyLevel(level));
    }

    /**
     * 删除紧急程度
     */
    @Log(title = "紧急程度", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('flowable:urgency:remove')")
    @DeleteMapping("/{levelIds}")
    public AjaxResult remove(@PathVariable Long[] levelIds)
    {
        return toAjax(urgencyLevelService.deleteFlowUrgencyLevelByIds(levelIds));
    }
}

package com.ruoyi.flowable.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.flowable.service.FlowOrgPickerService;

/**
 * 流程人员选择 Controller：给流程办理页（转办 / 加签 / 抄送）提供「组织全量」的人员与部门。
 *
 * <p>不复用 {@code /system/user/list}、{@code /system/user/deptTree}，
 * 因为那两个接口带数据范围过滤，普通员工会看不到其他部门的同事。</p>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/picker")
public class FlowPickerController extends BaseController
{
    @Autowired
    private FlowOrgPickerService flowOrgPickerService;

    /**
     * 组织全量人员（不受数据权限限制）
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:complete')")
    @GetMapping("/users")
    public AjaxResult users(@RequestParam(value = "keyword", required = false) String keyword)
    {
        List<Map<String, Object>> list = flowOrgPickerService.selectOrgUsers(keyword);
        return success(list);
    }

    /**
     * 组织全量部门树（不受数据权限限制）
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:complete')")
    @GetMapping("/depts")
    public AjaxResult depts()
    {
        return success(flowOrgPickerService.selectOrgDeptTree());
    }
}

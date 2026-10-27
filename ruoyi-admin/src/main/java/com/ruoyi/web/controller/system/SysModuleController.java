package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.SysModule;
import com.ruoyi.system.service.ISysModuleService;

/**
 * 系统模块开关 Controller
 *
 * 提供业务模块的启停状态查询与级联切换能力，仅超级管理员可用。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/module")
public class SysModuleController extends BaseController
{
    @Autowired
    private ISysModuleService moduleService;

    /**
     * 查询模块开关列表（含关联角色/任务统计）
     */
    @PreAuthorize("@ss.hasPermi('system:module:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysModule module)
    {
        startPage();
        List<SysModule> list = moduleService.selectModuleList(module);
        return getDataTable(list);
    }

    /**
     * 查询全部启用模块的标识列表（登录用户即可访问，用于前端全局功能按模块状态显隐，如AI助手入口）
     */
    @GetMapping("/enabled")
    public AjaxResult enabledKeys()
    {
        SysModule query = new SysModule();
        query.setStatus("0");
        List<String> keys = moduleService.selectModuleList(query).stream()
                .map(SysModule::getModuleKey).collect(java.util.stream.Collectors.toList());
        return success(keys);
    }

    /**
     * 模块详情
     */
    @PreAuthorize("@ss.hasPermi('system:module:list')")
    @GetMapping(value = "/{moduleId}")
    public AjaxResult getInfo(@PathVariable("moduleId") Long moduleId)
    {
        return success(moduleService.selectModuleById(moduleId));
    }

    /**
     * 切换模块启停状态（级联停用/启用菜单、角色、定时任务，并强制在线用户重新登录）
     */
    @PreAuthorize("@ss.hasPermi('system:module:edit')")
    @Log(title = "模块开关", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysModule module)
    {
        return toAjax(moduleService.changeStatus(module.getModuleId(), module.getStatus(), getUsername()));
    }

    /**
     * 一键恢复全部已停用模块
     */
    @PreAuthorize("@ss.hasPermi('system:module:edit')")
    @Log(title = "模块开关", businessType = BusinessType.UPDATE)
    @PutMapping("/enableAll")
    public AjaxResult enableAll()
    {
        int count = moduleService.enableAll(getUsername());
        return success("已恢复 " + count + " 个模块");
    }
}

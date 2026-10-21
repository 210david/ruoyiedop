package com.ruoyi.web.controller.system;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.SysTableConfig;
import com.ruoyi.system.service.ISysTableConfigService;

/**
 * 表格列配置 信息操作处理（列宽 + 列显隐）
 *
 * 功能说明：
 * - 管理员设置列宽/列显隐 → 保存为全局配置，对所有用户生效
 * - 普通用户设置列宽/列显隐 → 保存为个人配置，仅对自己生效
 * - 加载时优先级：个人配置 > 全局配置 > 默认值
 *
 * 无需权限校验，所有登录用户均可使用（仅管理员可设置全局配置）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/tableConfig")
public class SysTableConfigController extends BaseController
{
    @Autowired
    private ISysTableConfigService tableConfigService;

    /**
     * 获取某页面的合并列配置（个人覆盖全局）
     * 返回列宽映射 widths 和列显隐映射 columns
     *
     * @param tableKey 页面标识
     * @return { widths: { colProp: width }, columns: { colProp: visible } }
     */
    @GetMapping("/{tableKey}")
    public AjaxResult getWidths(@PathVariable String tableKey)
    {
        Long userId = getUserId();
        Map<String, Integer> widths = tableConfigService.getMergedWidths(tableKey, userId);
        Map<String, Boolean> columns = tableConfigService.getMergedVisibility(tableKey, userId);
        Map<String, Integer> orders = tableConfigService.getMergedOrders(tableKey, userId);
        AjaxResult result = AjaxResult.success();
        result.put("widths", widths);
        result.put("columns", columns);
        result.put("orders", orders);
        return result;
    }

    /**
     * 保存单列列宽配置
     * 请求体: { "tableKey": "pms_eval_index", "colProp": "supplierName", "colWidth": 200 }
     *
     * 管理员 → 保存为全局配置（G）
     * 普通用户 → 保存为个人配置（U）
     *
     * @param config 列宽配置
     * @return 结果
     */
    @PostMapping
    public AjaxResult save(@RequestBody SysTableConfig config)
    {
        Long userId = getUserId();
        String username = getUsername();
        tableConfigService.saveColumnWidth(
            config.getTableKey(),
            config.getColProp(),
            config.getColWidth(),
            userId,
            username
        );
        return success();
    }

    /**
     * 保存单列列显隐配置
     * 请求体: { "tableKey": "sys_user", "colProp": "postName", "colVisible": false }
     *
     * 管理员 → 保存为全局配置（G）
     * 普通用户 → 保存为个人配置（U）
     *
     * @param config 列显隐配置
     * @return 结果
     */
    @PostMapping("/visible")
    public AjaxResult saveVisible(@RequestBody SysTableConfig config)
    {
        Long userId = getUserId();
        String username = getUsername();
        tableConfigService.saveColumnVisible(
            config.getTableKey(),
            config.getColProp(),
            config.getColVisible(),
            userId,
            username
        );
        return success();
    }

    /**
     * 保存单列列顺序配置
     * 请求体: { "tableKey": "system_post_index", "colProp": "postName", "colOrder": 2 }
     *
     * 管理员 → 保存为全局配置（G）
     * 普通用户 → 保存为个人配置（U）
     *
     * @param config 列顺序配置
     * @return 结果
     */
    @PostMapping("/order")
    public AjaxResult saveOrder(@RequestBody SysTableConfig config)
    {
        Long userId = getUserId();
        String username = getUsername();
        tableConfigService.saveColumnOrder(
            config.getTableKey(),
            config.getColProp(),
            config.getColOrder(),
            userId,
            username
        );
        return success();
    }

    /**
     * 根据主键删除
     */
    @DeleteMapping("/{configId}")
    public AjaxResult remove(@PathVariable Long configId)
    {
        return toAjax(tableConfigService.deleteConfigById(configId));
    }
}

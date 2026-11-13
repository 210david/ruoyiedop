package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysUserHomeLayoutMapper;
import com.ruoyi.web.service.HomeSnapshotService;

/**
 * 平台首页聚合数据 Controller
 *
 * 汇总仓库管理(WMS)、设备管理(DMS)、采购管理(PMS)、营销管理(MK)、
 * 生产管控(MMS)、质量管理(QMS)、安全生产管理(Safety)、人力资源(HR)
 * 各业务模块的关键指标，为平台首页提供统一的概览数据；
 * 同时提供用户级门户首页模块显示布局配置。
 *
 * 取数约定（决策支持模块阶段一重构，PRD F1.4）：
 * 1. 取数全部委托 HomeSnapshotService（纯搬家，逻辑一行不改），
 *    实现"HomeController 代码中不再 import 任何业务模块的 Mapper"（PRD F1.4 验收）；
 * 2. 对外接口与响应结构保持不变，前端零改动。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/home")
public class HomeController extends BaseController
{
    /** 用户级门户首页布局（system 模块基础能力，非业务模块 Mapper） */
    @Autowired
    private SysUserHomeLayoutMapper homeLayoutMapper;

    /** 首页跨模块数据聚合（PRD F1.4：业务 Mapper 依赖收敛至本 Service） */
    @Autowired
    private HomeSnapshotService homeSnapshotService;

    /**
     * 首页聚合数据：欢迎信息 + 预警汇总 + 模块概览 + 待办 + WMS 出入库趋势
     */
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return AjaxResult.success(homeSnapshotService.buildDashboardData(
                SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    /**
     * 获取当前用户门户首页模块显示配置
     */
    @GetMapping("/layout")
    public AjaxResult getLayout()
    {
        Long userId = SecurityUtils.getUserId();
        String codes = null;
        try
        {
            codes = homeLayoutMapper.selectModuleCodesByUserId(userId);
        }
        catch (Exception e)
        {
            codes = null;
        }
        return AjaxResult.success(codes == null ? "" : codes);
    }

    /**
     * 保存当前用户门户首页模块显示配置
     */
    @PutMapping("/layout")
    public AjaxResult saveLayout(@RequestBody java.util.Map<String, Object> body)
    {
        Long userId = SecurityUtils.getUserId();
        String codes = body.get("moduleCodes") == null ? "" : String.valueOf(body.get("moduleCodes"));
        if (codes.length() > 500)
        {
            return error("配置内容过长");
        }
        homeLayoutMapper.saveHomeLayout(userId, codes, SecurityUtils.getUsername());
        return success();
    }
}

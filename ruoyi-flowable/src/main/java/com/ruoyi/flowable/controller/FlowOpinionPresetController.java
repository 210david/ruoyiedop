package com.ruoyi.flowable.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.flowable.domain.dto.FlowOpinionPresetDTO;
import com.ruoyi.flowable.service.IFlowOpinionPresetService;

/**
 * 签字意见-个人常用意见 Controller
 *
 * 对标泛微「签字意见常用语」：办理人在办理页可维护自己的快捷短语。
 * 所有接口以当前登录人为数据边界，不校验菜单权限（普通办理人也需要用自己的常用意见）。
 *
 * 系统默认短语由字典 flow_opinion_preset 维护（管理员，见「系统管理-字典管理」）；
 * 个人未配置时接口返回系统默认，前端无需再区分。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/opinionPreset")
public class FlowOpinionPresetController extends BaseController
{
    @Autowired
    private IFlowOpinionPresetService flowOpinionPresetService;

    /**
     * 我的常用意见（current=当前生效短语，defaults=系统默认，personalized=是否自定义过）
     */
    @GetMapping("/list")
    public AjaxResult list()
    {
        return AjaxResult.success(flowOpinionPresetService.selectMyPresets());
    }

    /**
     * 保存我的常用意见（整体覆盖，提交顺序即展示顺序）
     */
    @Log(title = "签字意见常用语", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    public AjaxResult save(@RequestBody FlowOpinionPresetDTO dto)
    {
        return AjaxResult.success(flowOpinionPresetService.saveMyPresets(dto == null ? null : dto.getPresets()));
    }

    /**
     * 恢复系统默认（清空个人配置）
     */
    @Log(title = "签字意见常用语", businessType = BusinessType.UPDATE)
    @DeleteMapping("/reset")
    public AjaxResult reset()
    {
        return AjaxResult.success(flowOpinionPresetService.resetMyPresets());
    }
}

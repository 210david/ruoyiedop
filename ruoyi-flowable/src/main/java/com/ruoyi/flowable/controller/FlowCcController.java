package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.flowable.domain.FlowCc;
import com.ruoyi.flowable.service.IFlowCcService;

/**
 * 流程抄送（待阅）Controller
 *
 * 对标泛微「待阅」：流程节点上配置的抄送人，在节点到达 / 办理完成时收到一条待阅记录，
 * 在「待办流程-待阅」页签可见，可查看流程详情并标记已读。
 *
 * 说明：所有接口都以当前登录人为数据边界，不校验菜单权限（普通用户也需要看自己的待阅）。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/cc")
public class FlowCcController extends BaseController
{
    @Autowired
    private IFlowCcService flowCcService;

    /**
     * 我的待阅列表（query.readFlag=0 只看未读）
     */
    @GetMapping("/list")
    public TableDataInfo list(FlowCc query)
    {
        startPage();
        List<FlowCc> list = flowCcService.selectMyCcList(query);
        return getDataTable(list);
    }

    /**
     * 某个流程实例的抄送记录（办理页 / 流程详情页展示「抄送」信息）
     *
     * 与列表接口同口径：不校验菜单权限（办理人在待办页也需要看到抄送过谁）；
     * 抄送人本身不属于敏感信息，流程可见性已由待办/详情入口保证。
     */
    @GetMapping("/byInstance/{instanceId}")
    public AjaxResult byInstance(@PathVariable("instanceId") String instanceId)
    {
        return AjaxResult.success(flowCcService.selectByInstance(instanceId));
    }

    /**
     * 我的未读待阅数量
     */
    @GetMapping("/unreadCount")
    public AjaxResult unreadCount()
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("count", flowCcService.countUnread());
        return ajax;
    }

    /**
     * 标记单条已读（已读时幂等返回成功）
     */
    @Log(title = "流程待阅", businessType = BusinessType.UPDATE)
    @PutMapping("/read/{ccId}")
    public AjaxResult read(@PathVariable Long ccId)
    {
        flowCcService.markRead(ccId);
        return AjaxResult.success();
    }

    /**
     * 全部标记已读（无未读记录时也返回成功）
     */
    @Log(title = "流程待阅", businessType = BusinessType.UPDATE)
    @PutMapping("/readAll")
    public AjaxResult readAll()
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("updated", flowCcService.markAllRead());
        return ajax;
    }
}

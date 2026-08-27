package com.ruoyi.web.controller.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysMessage;
import com.ruoyi.system.service.ISysMessageService;

/**
 * 消息中心 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/message")
public class SysMessageController extends BaseController
{
    @Autowired
    private ISysMessageService messageService;

    /**
     * 获取消息列表（管理端，带当前用户已读状态）
     * 按当前用户的角色权限过滤可见消息
     */
    @PreAuthorize("@ss.hasPermi('system:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysMessage message)
    {
        // 禁用PageHelper自动COUNT：消息列表SQL包含bizStatusExpr复杂子查询，
        // PageHelper生成的COUNT子查询通过Druid filter链时性能极差（16秒+），
        // 改用手动查询简单基表COUNT
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        if (pageNum == null) pageNum = 1;
        if (pageSize == null) pageSize = 10;
        PageHelper.startPage(pageNum, pageSize, false);
        Long userId = getUserId();
        List<String> roleKeys = getCurrentUserPermissions();
        List<SysMessage> list = messageService.selectMessageListWithReadStatus(message, userId, roleKeys);
        // 手动查询总数（简单COUNT，不走派生表子查询）
        long total = messageService.selectMessageListCount(message, userId, roleKeys);
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(200);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(total);
        return rspData;
    }

    /**
     * 根据消息ID获取详细信息
     */
    @GetMapping(value = "/{messageId}")
    public AjaxResult getInfo(@PathVariable Long messageId)
    {
        return success(messageService.selectMessageById(messageId));
    }

    /**
     * 新增消息
     */
    @PreAuthorize("@ss.hasPermi('system:message:add')")
    @Log(title = "消息中心", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysMessage message)
    {
        message.setCreateBy(getUsername());
        return toAjax(messageService.insertMessage(message));
    }

    /**
     * 首页顶部消息列表（带当前用户已读状态，最多10条）
     * 按当前用户的角色权限过滤可见消息
     */
    @GetMapping("/listTop")
    @ResponseBody
    public AjaxResult listTop()
    {
        Long userId = getUserId();
        List<String> roleKeys = getCurrentUserPermissions();
        List<SysMessage> list = messageService.selectMessageListTop(userId, roleKeys, 10);
        int unreadCount = messageService.selectUnreadCount(userId, roleKeys);
        AjaxResult result = AjaxResult.success(list);
        result.put("unreadCount", unreadCount);
        return result;
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/markRead")
    @ResponseBody
    public AjaxResult markRead(Long messageId)
    {
        Long userId = getUserId();
        messageService.markRead(messageId, userId);
        return success();
    }

    /**
     * 批量标记已读
     */
    @PostMapping("/markReadAll")
    @ResponseBody
    public AjaxResult markReadAll(String ids)
    {
        Long userId = getUserId();
        Long[] messageIds = com.ruoyi.common.core.text.Convert.toLongArray(ids);
        messageService.markReadBatch(userId, messageIds);
        return success();
    }

    /**
     * 获取当前登录用户的权限标识列表
     * 用于消息的角色权限过滤
     * admin用户（拥有*:*:*通配符权限）返回null，表示不做权限过滤
     */
    private List<String> getCurrentUserPermissions()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            return new ArrayList<>();
        }
        Set<String> permissions = loginUser.getPermissions();
        if (permissions == null || permissions.isEmpty())
        {
            return new ArrayList<>();
        }
        // 如果用户拥有通配符权限（admin），返回null表示不做权限过滤
        if (permissions.contains("*:*:*"))
        {
            return null;
        }
        // 转为List供MyBatis使用
        return new ArrayList<>(permissions);
    }
}

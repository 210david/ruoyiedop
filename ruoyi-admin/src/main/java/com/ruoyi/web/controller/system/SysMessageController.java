package com.ruoyi.web.controller.system;

import java.util.List;
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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
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
     */
    @PreAuthorize("@ss.hasPermi('system:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysMessage message)
    {
        startPage();
        Long userId = getUserId();
        message.setRecipientId(userId);
        List<SysMessage> list = messageService.selectMessageListWithReadStatus(message, userId);
        return getDataTable(list);
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
     */
    @GetMapping("/listTop")
    @ResponseBody
    public AjaxResult listTop()
    {
        Long userId = getUserId();
        List<SysMessage> list = messageService.selectMessageListTop(userId, 10);
        int unreadCount = messageService.selectUnreadCount(userId);
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
}

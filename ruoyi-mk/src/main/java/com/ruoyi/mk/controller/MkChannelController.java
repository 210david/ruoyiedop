package com.ruoyi.mk.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mk.domain.MkChannel;
import com.ruoyi.mk.service.IMkChannelService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 渠道账号 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/channel")
public class MkChannelController extends BaseController
{
    @Autowired
    private IMkChannelService mkChannelService;

    @PreAuthorize("@ss.hasPermi('marketing:channel:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkChannel channel)
    {
        startPage();
        List<MkChannel> list = mkChannelService.selectChannelList(channel);
        return getDataTable(list);
    }

    /** 全部正常渠道（选择器用，不分页） */
    @PreAuthorize("@ss.hasPermi('marketing:channel:list')")
    @GetMapping("/all")
    public AjaxResult all()
    {
        return AjaxResult.success(mkChannelService.selectAllChannels());
    }

    @Log(title = "渠道账号", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:channel:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkChannel channel)
    {
        List<MkChannel> list = mkChannelService.selectChannelList(channel);
        ExcelUtil<MkChannel> util = new ExcelUtil<>(MkChannel.class);
        util.exportExcel(response, list, "渠道账号数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:channel:query')")
    @GetMapping(value = "/{channelId}")
    public AjaxResult getInfo(@PathVariable("channelId") Long channelId)
    {
        return AjaxResult.success(mkChannelService.selectChannelById(channelId));
    }

    @Log(title = "渠道账号", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:channel:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkChannel channel)
    {
        return toAjax(mkChannelService.insertChannel(channel));
    }

    @Log(title = "渠道账号", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:channel:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkChannel channel)
    {
        return toAjax(mkChannelService.updateChannel(channel));
    }

    @Log(title = "渠道账号", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:channel:remove')")
    @DeleteMapping("/{channelIds}")
    public AjaxResult remove(@PathVariable Long[] channelIds)
    {
        return toAjax(mkChannelService.deleteChannelByIds(channelIds));
    }
}

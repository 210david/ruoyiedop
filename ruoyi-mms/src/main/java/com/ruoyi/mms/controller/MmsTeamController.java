package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsTeam;
import com.ruoyi.mms.service.IMmsTeamService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 班组 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/team")
public class MmsTeamController extends BaseController
{
    @Autowired
    private IMmsTeamService mmsTeamService;

    @PreAuthorize("@ss.hasPermi('mms:team:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsTeam team)
    {
        startPage();
        List<MmsTeam> list = mmsTeamService.selectTeamList(team);
        return getDataTable(list);
    }

    @Log(title = "班组管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:team:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsTeam team)
    {
        List<MmsTeam> list = mmsTeamService.selectTeamList(team);
        ExcelUtil<MmsTeam> util = new ExcelUtil<>(MmsTeam.class);
        util.exportExcel(response, list, "班组管理");
    }

    @PreAuthorize("@ss.hasPermi('mms:team:query')")
    @GetMapping(value = "/{teamId}")
    public AjaxResult getInfo(@PathVariable("teamId") Long teamId)
    {
        return AjaxResult.success(mmsTeamService.selectTeamById(teamId));
    }

    @Log(title = "班组管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:team:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MmsTeam team)
    {
        return toAjax(mmsTeamService.insertTeam(team));
    }

    @Log(title = "班组管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:team:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MmsTeam team)
    {
        return toAjax(mmsTeamService.updateTeam(team));
    }

    @Log(title = "班组管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:team:remove')")
    @DeleteMapping("/{teamIds}")
    public AjaxResult remove(@PathVariable Long[] teamIds)
    {
        return toAjax(mmsTeamService.deleteTeamByIds(teamIds));
    }
}

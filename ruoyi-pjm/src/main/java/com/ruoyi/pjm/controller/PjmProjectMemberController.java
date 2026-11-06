package com.ruoyi.pjm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.pjm.domain.PjmProjectMember;
import com.ruoyi.pjm.service.IPjmProjectMemberService;

@RestController
@RequestMapping("/pjm/member")
public class PjmProjectMemberController extends BaseController
{
    @Autowired
    private IPjmProjectMemberService memberService;

    @PreAuthorize("@ss.hasPermi('pjm:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(PjmProjectMember member)
    {
        startPage();
        return getDataTable(memberService.selectMemberList(member));
    }

    @PreAuthorize("@ss.hasPermi('pjm:member:query')")
    @GetMapping(value = "/{memberId}")
    public AjaxResult getInfo(@PathVariable Long memberId) { return AjaxResult.success(memberService.selectMemberById(memberId)); }

    @PreAuthorize("@ss.hasPermi('pjm:member:add')")
    @Log(title = "项目团队", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PjmProjectMember member) { return toAjax(memberService.insertMember(member)); }

    @PreAuthorize("@ss.hasPermi('pjm:member:edit')")
    @Log(title = "项目团队", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PjmProjectMember member) { return toAjax(memberService.updateMember(member)); }

    @PreAuthorize("@ss.hasPermi('pjm:member:remove')")
    @Log(title = "项目团队", businessType = BusinessType.DELETE)
    @DeleteMapping("/{memberIds}")
    public AjaxResult remove(@PathVariable Long[] memberIds) { return toAjax(memberService.deleteMemberByIds(memberIds)); }
}

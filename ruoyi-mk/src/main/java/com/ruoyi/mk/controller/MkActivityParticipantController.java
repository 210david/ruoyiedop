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
import com.ruoyi.mk.domain.MkActivityParticipant;
import com.ruoyi.mk.service.IMkActivityParticipantService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 活动参与人 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/participant")
public class MkActivityParticipantController extends BaseController
{
    @Autowired
    private IMkActivityParticipantService mkActivityParticipantService;

    @PreAuthorize("@ss.hasPermi('marketing:participant:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkActivityParticipant participant)
    {
        startPage();
        List<MkActivityParticipant> list = mkActivityParticipantService.selectParticipantList(participant);
        return getDataTable(list);
    }

    @Log(title = "活动参与人", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:participant:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkActivityParticipant participant)
    {
        List<MkActivityParticipant> list = mkActivityParticipantService.selectParticipantList(participant);
        ExcelUtil<MkActivityParticipant> util = new ExcelUtil<>(MkActivityParticipant.class);
        util.exportExcel(response, list, "活动参与人数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:participant:query')")
    @GetMapping(value = "/{participantId}")
    public AjaxResult getInfo(@PathVariable("participantId") Long participantId)
    {
        return AjaxResult.success(mkActivityParticipantService.selectParticipantById(participantId));
    }

    @Log(title = "活动参与人", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:participant:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkActivityParticipant participant)
    {
        return toAjax(mkActivityParticipantService.insertParticipant(participant));
    }

    @Log(title = "活动参与人", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:participant:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkActivityParticipant participant)
    {
        return toAjax(mkActivityParticipantService.updateParticipant(participant));
    }

    @Log(title = "活动参与人", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:participant:remove')")
    @DeleteMapping("/{participantIds}")
    public AjaxResult remove(@PathVariable Long[] participantIds)
    {
        return toAjax(mkActivityParticipantService.deleteParticipantByIds(participantIds));
    }

    // ==================== P0-3: 按活动ID查询参与人 ====================

    /** P0-3: 按活动ID查询参与人列表 */
    @PreAuthorize("@ss.hasPermi('marketing:participant:list')")
    @GetMapping("/byActivity/{activityId}")
    public AjaxResult byActivity(@PathVariable Long activityId)
    {
        return AjaxResult.success(mkActivityParticipantService.selectByActivityId(activityId));
    }

    // ==================== P1-5: 签到 & 转线索 ====================

    /** P1-5: 签到 */
    @Log(title = "参与人签到", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:participant:edit')")
    @PutMapping("/signIn/{participantId}")
    public AjaxResult signIn(@PathVariable Long participantId)
    {
        return toAjax(mkActivityParticipantService.signIn(participantId));
    }

    /** P1-5: 转线索 */
    @Log(title = "参与人转线索", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:participant:convert')")
    @PostMapping("/convertToLead/{participantId}")
    public AjaxResult convertToLead(@PathVariable Long participantId)
    {
        Long leadId = mkActivityParticipantService.convertToLead(participantId);
        AjaxResult result = AjaxResult.success("转线索成功");
        result.put("leadId", leadId);
        return result;
    }
}

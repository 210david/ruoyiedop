package com.ruoyi.mk.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mk.domain.MkLead;
import com.ruoyi.mk.service.IMkLeadService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 线索 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/lead")
public class MkLeadController extends BaseController
{
    @Autowired
    private IMkLeadService mkLeadService;

    @PreAuthorize("@ss.hasPermi('marketing:lead:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkLead lead)
    {
        startPage();
        List<MkLead> list = mkLeadService.selectLeadList(lead);
        return getDataTable(list);
    }

    @Log(title = "线索管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:lead:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkLead lead)
    {
        List<MkLead> list = mkLeadService.selectLeadList(lead);
        ExcelUtil<MkLead> util = new ExcelUtil<>(MkLead.class);
        util.exportExcel(response, list, "线索数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:lead:query')")
    @GetMapping(value = "/{leadId}")
    public AjaxResult getInfo(@PathVariable("leadId") Long leadId)
    {
        return AjaxResult.success(mkLeadService.selectLeadById(leadId));
    }

    @Log(title = "线索管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:lead:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkLead lead)
    {
        return toAjax(mkLeadService.insertLead(lead));
    }

    @Log(title = "线索管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:lead:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkLead lead)
    {
        return toAjax(mkLeadService.updateLead(lead));
    }

    @Log(title = "线索管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:lead:remove')")
    @DeleteMapping("/{leadIds}")
    public AjaxResult remove(@PathVariable Long[] leadIds)
    {
        return toAjax(mkLeadService.deleteLeadByIds(leadIds));
    }

    @Log(title = "线索领取", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:lead:receive')")
    @PutMapping("/receive/{leadId}")
    public AjaxResult receive(@PathVariable Long leadId, @RequestBody MkLead lead)
    {
        lead.setLeadId(leadId);
        lead.setUserId(getUserId());
        lead.setDeptId(getDeptId());
        return toAjax(mkLeadService.receiveLead(lead));
    }

    @Log(title = "领取审批通过", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:lead:assign')")
    @PutMapping("/approve/{leadId}")
    public AjaxResult approveReceive(@PathVariable Long leadId, @RequestBody MkLead lead)
    {
        lead.setLeadId(leadId);
        lead.setDeptId(getDeptId());
        return toAjax(mkLeadService.approveReceive(lead));
    }

    @Log(title = "领取审批拒绝", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:lead:assign')")
    @PutMapping("/reject/{leadId}")
    public AjaxResult rejectReceive(@PathVariable Long leadId, @RequestBody MkLead lead)
    {
        lead.setLeadId(leadId);
        return toAjax(mkLeadService.rejectReceive(lead));
    }

    @Log(title = "线索分配", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:lead:assign')")
    @PutMapping("/assign/{leadId}")
    public AjaxResult assign(@PathVariable Long leadId, @RequestBody MkLead lead)
    {
        lead.setLeadId(leadId);
        return toAjax(mkLeadService.assignLead(lead));
    }

    @Log(title = "线索转化", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:lead:edit')")
    @PutMapping("/convert/{leadId}")
    public AjaxResult convert(@PathVariable Long leadId, @RequestBody MkLead lead)
    {
        lead.setLeadId(leadId);
        mkLeadService.convertLead(lead);
        AjaxResult result = AjaxResult.success("转化成功");
        result.put("convertCustomerId", lead.getConvertCustomerId());
        return result;
    }

    // ====== P1: 批量操作 + 退回公海 + 导入 ======

    /** 批量分配线索 */
    @Log(title = "批量分配线索", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:lead:assign')")
    @PutMapping("/batchAssign")
    public AjaxResult batchAssign(@RequestBody BatchAssignRequest request)
    {
        return toAjax(mkLeadService.batchAssign(request.getLeadIds(), request.getUserId(), request.getDeptId(), request.getUserName(), request.getDeptName()));
    }

    /** 批量变更线索状态 */
    @Log(title = "批量变更线索状态", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:lead:edit')")
    @PutMapping("/batchStatus")
    public AjaxResult batchUpdateStatus(@RequestBody BatchStatusRequest request)
    {
        return toAjax(mkLeadService.batchUpdateStatus(request.getLeadIds(), request.getLeadStatus()));
    }

    /** 释放线索到公海 */
    @Log(title = "释放线索到公海", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:lead:edit')")
    @PutMapping("/release/{leadId}")
    public AjaxResult release(@PathVariable Long leadId)
    {
        return toAjax(mkLeadService.releaseToPool(leadId));
    }

    /** 线索导入 */
    @Log(title = "线索导入", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('marketing:lead:add')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<MkLead> util = new ExcelUtil<>(MkLead.class);
        List<MkLead> leadList = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getUsername();
        String message = mkLeadService.importLead(leadList, updateSupport, operName);
        return AjaxResult.success(message);
    }

    /** 线索导入模板下载 */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<MkLead> util = new ExcelUtil<>(MkLead.class);
        util.importTemplateExcel(response, "线索数据");
    }

    // ====== P2: 无效线索 + 跟进时间 ======

    /** 标记线索无效 */
    @Log(title = "标记线索无效", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:lead:edit')")
    @PutMapping("/invalidate/{leadId}")
    public AjaxResult invalidate(@PathVariable Long leadId, @RequestBody MkLead lead)
    {
        lead.setLeadId(leadId);
        return toAjax(mkLeadService.invalidateLead(lead));
    }

    /** 更新最后跟进时间 */
    @Log(title = "更新跟进时间", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:lead:edit')")
    @PutMapping("/followTime/{leadId}")
    public AjaxResult updateFollowTime(@PathVariable Long leadId)
    {
        return toAjax(mkLeadService.updateLastFollowTime(leadId, new java.util.Date()));
    }

    // ====== P3: 查重 ======

    /** 线索查重 */
    @PreAuthorize("@ss.hasPermi('marketing:lead:list')")
    @GetMapping("/checkDuplicate")
    public AjaxResult checkDuplicate(@RequestParam(required = false) String companyName, @RequestParam(required = false) String contactPhone)
    {
        return AjaxResult.success(mkLeadService.checkDuplicate(companyName, contactPhone));
    }

    // ====== 请求体内部类 ======
    public static class BatchAssignRequest
    {
        private Long[] leadIds;
        private Long userId;
        private Long deptId;
        private String userName;
        private String deptName;

        public Long[] getLeadIds() { return leadIds; }
        public void setLeadIds(Long[] leadIds) { this.leadIds = leadIds; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }
    }

    public static class BatchStatusRequest
    {
        private Long[] leadIds;
        private String leadStatus;

        public Long[] getLeadIds() { return leadIds; }
        public void setLeadIds(Long[] leadIds) { this.leadIds = leadIds; }
        public String getLeadStatus() { return leadStatus; }
        public void setLeadStatus(String leadStatus) { this.leadStatus = leadStatus; }
    }
}

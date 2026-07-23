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
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.service.IMkCustomerService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业客户 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/customer")
public class MkCustomerController extends BaseController
{
    @Autowired
    private IMkCustomerService mkCustomerService;

    @PreAuthorize("@ss.hasPermi('marketing:customer:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkCustomer customer)
    {
        startPage();
        List<MkCustomer> list = mkCustomerService.selectCustomerList(customer);
        return getDataTable(list);
    }

    /** 公海客户列表 */
    @PreAuthorize("@ss.hasAnyPermi('marketing:customer:pool,marketing:customer:list')")
    @GetMapping("/publicPool")
    public TableDataInfo publicPool(MkCustomer customer)
    {
        startPage();
        List<MkCustomer> list = mkCustomerService.selectPublicPoolList(customer);
        return getDataTable(list);
    }

    @Log(title = "企业客户", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:customer:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkCustomer customer)
    {
        List<MkCustomer> list = mkCustomerService.selectCustomerList(customer);
        ExcelUtil<MkCustomer> util = new ExcelUtil<>(MkCustomer.class);
        util.exportExcel(response, list, "企业客户数据");
    }

    @Log(title = "企业客户", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('marketing:customer:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<MkCustomer> util = new ExcelUtil<>(MkCustomer.class);
        List<MkCustomer> customerList = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getUsername();
        String message = mkCustomerService.importCustomer(customerList, updateSupport, operName);
        return AjaxResult.success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<MkCustomer> util = new ExcelUtil<>(MkCustomer.class);
        util.importTemplateExcel(response, "企业客户数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:customer:query')")
    @GetMapping(value = "/{customerId}")
    public AjaxResult getInfo(@PathVariable("customerId") Long customerId)
    {
        return AjaxResult.success(mkCustomerService.selectCustomerById(customerId));
    }

    @Log(title = "企业客户", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:customer:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkCustomer customer)
    {
        return toAjax(mkCustomerService.insertCustomer(customer));
    }

    @Log(title = "企业客户", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:customer:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkCustomer customer)
    {
        return toAjax(mkCustomerService.updateCustomer(customer));
    }

    @Log(title = "企业客户", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:customer:remove')")
    @DeleteMapping("/{customerIds}")
    public AjaxResult remove(@PathVariable Long[] customerIds)
    {
        return toAjax(mkCustomerService.deleteCustomerByIds(customerIds));
    }

    // ====== 第二阶段：客户状态生命周期管理 ======

    /** 变更客户状态 */
    @Log(title = "客户状态变更", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:customer:edit')")
    @PutMapping("/status/{customerId}")
    public AjaxResult changeStatus(@PathVariable("customerId") Long customerId, @RequestParam String customerStatus)
    {
        return toAjax(mkCustomerService.changeStatus(customerId, customerStatus));
    }

    /** 批量变更客户状态 */
    @Log(title = "批量变更客户状态", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:customer:edit')")
    @PutMapping("/batchStatus")
    public AjaxResult batchUpdateStatus(@RequestBody BatchStatusRequest request)
    {
        return toAjax(mkCustomerService.batchUpdateStatus(request.getCustomerIds(), request.getCustomerStatus()));
    }

    // ====== 第三阶段：公海机制 + 分配/转移 ======

    /** 分配客户负责人 */
    @Log(title = "分配客户", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:customer:assign')")
    @PutMapping("/assign/{customerId}")
    public AjaxResult assign(@PathVariable("customerId") Long customerId, @RequestBody AssignRequest request)
    {
        return toAjax(mkCustomerService.assignCustomer(customerId, request.getUserId(), request.getDeptId(), request.getUserName(), request.getDeptName()));
    }

    /** 释放客户到公海 */
    @Log(title = "释放客户到公海", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:customer:edit')")
    @PutMapping("/release/{customerId}")
    public AjaxResult release(@PathVariable("customerId") Long customerId)
    {
        return toAjax(mkCustomerService.releaseToPool(customerId));
    }

    /** 从公海领取客户 */
    @Log(title = "领取公海客户", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:customer:claim')")
    @PutMapping("/claim/{customerId}")
    public AjaxResult claim(@PathVariable("customerId") Long customerId)
    {
        Long userId = SecurityUtils.getUserId();
        String userName = SecurityUtils.getUsername();
        // 通过userId获取deptId - 使用getLoginUser
        Long deptId = SecurityUtils.getLoginUser().getDeptId();
        return toAjax(mkCustomerService.claimCustomer(customerId, userId, deptId, userName, null));
    }

    /** 批量分配客户 */
    @Log(title = "批量分配客户", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:customer:assign')")
    @PutMapping("/batchAssign")
    public AjaxResult batchAssign(@RequestBody BatchAssignRequest request)
    {
        return toAjax(mkCustomerService.batchAssign(request.getCustomerIds(), request.getUserId(), request.getDeptId(), request.getUserName(), request.getDeptName()));
    }

    // ====== 请求体内部类 ======
    public static class AssignRequest
    {
        private Long userId;
        private Long deptId;
        private String userName;
        private String deptName;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }
    }

    public static class BatchAssignRequest
    {
        private Long[] customerIds;
        private Long userId;
        private Long deptId;
        private String userName;
        private String deptName;

        public Long[] getCustomerIds() { return customerIds; }
        public void setCustomerIds(Long[] customerIds) { this.customerIds = customerIds; }
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
        private Long[] customerIds;
        private String customerStatus;

        public Long[] getCustomerIds() { return customerIds; }
        public void setCustomerIds(Long[] customerIds) { this.customerIds = customerIds; }
        public String getCustomerStatus() { return customerStatus; }
        public void setCustomerStatus(String customerStatus) { this.customerStatus = customerStatus; }
    }
}

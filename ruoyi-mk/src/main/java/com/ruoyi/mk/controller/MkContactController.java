package com.ruoyi.mk.controller;

import java.util.List;
import java.util.Map;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mk.domain.MkContact;
import com.ruoyi.mk.service.IMkContactService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 联系人 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/contact")
public class MkContactController extends BaseController
{
    @Autowired
    private IMkContactService mkContactService;

    @PreAuthorize("@ss.hasPermi('marketing:contact:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkContact contact)
    {
        startPage();
        List<MkContact> list = mkContactService.selectContactList(contact);
        return getDataTable(list);
    }

    @Log(title = "联系人管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:contact:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkContact contact)
    {
        List<MkContact> list = mkContactService.selectContactList(contact);
        ExcelUtil<MkContact> util = new ExcelUtil<>(MkContact.class);
        util.exportExcel(response, list, "联系人数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:contact:query')")
    @GetMapping(value = "/{contactId}")
    public AjaxResult getInfo(@PathVariable("contactId") Long contactId)
    {
        return AjaxResult.success(mkContactService.selectContactById(contactId));
    }

    @Log(title = "联系人管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:contact:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkContact contact)
    {
        return toAjax(mkContactService.insertContact(contact));
    }

    @Log(title = "联系人管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:contact:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkContact contact)
    {
        return toAjax(mkContactService.updateContact(contact));
    }

    @Log(title = "联系人管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:contact:remove')")
    @DeleteMapping("/{contactIds}")
    public AjaxResult remove(@PathVariable Long[] contactIds)
    {
        return toAjax(mkContactService.deleteContactByIds(contactIds));
    }

    // ==================== P1-7: 主要联系人管理 ====================

    /** P1-7: 设为主要联系人 */
    @Log(title = "设为主要联系人", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:contact:edit')")
    @PutMapping("/setPrimary/{contactId}")
    public AjaxResult setPrimary(@PathVariable Long contactId)
    {
        return toAjax(mkContactService.setPrimary(contactId));
    }

    /** P1-7: 批量设置关键联系人 */
    @Log(title = "批量设置关键联系人", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:contact:edit')")
    @PutMapping("/batchSetKey")
    public AjaxResult batchSetKey(@RequestBody Map<String, Object> body)
    {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("contactIds");
        String isKey = (String) body.get("isKey");
        Long[] contactIds = ids.stream().map(Long::valueOf).toArray(Long[]::new);
        return toAjax(mkContactService.batchSetKey(contactIds, isKey));
    }

    // ==================== P2-8: 联系人去重合并 ====================

    /** P2-8: 检查重复联系人 */
    @PreAuthorize("@ss.hasPermi('marketing:contact:list')")
    @GetMapping("/checkDuplicate")
    public AjaxResult checkDuplicate(@RequestParam(required = false) String phone, @RequestParam(required = false) String email)
    {
        return AjaxResult.success(mkContactService.checkDuplicate(phone, email));
    }

    /** P2-8: 合并联系人 */
    @Log(title = "联系人合并", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:contact:merge')")
    @PutMapping("/merge")
    public AjaxResult merge(@RequestBody Map<String, Long> body)
    {
        Long masterId = body.get("masterId");
        Long slaveId = body.get("slaveId");
        return toAjax(mkContactService.mergeContacts(masterId, slaveId));
    }

    // ==================== P2-10: 联系人分配/转移 ====================

    /** P2-10: 分配/转移联系人 */
    @Log(title = "联系人分配", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:contact:assign')")
    @PutMapping("/assign/{contactId}")
    public AjaxResult assign(@PathVariable Long contactId, @RequestBody Map<String, Long> body)
    {
        Long ownerUserId = body.get("ownerUserId");
        return toAjax(mkContactService.assignContact(contactId, ownerUserId));
    }

    /** P2-10: 释放到公海 */
    @Log(title = "释放联系人到公海", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:contact:assign')")
    @PutMapping("/release/{contactId}")
    public AjaxResult release(@PathVariable Long contactId)
    {
        return toAjax(mkContactService.releaseToPool(contactId));
    }

    // ==================== P3-13: 联系人导入 ====================

    /** P3-13: 导入联系人模板下载 */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<MkContact> util = new ExcelUtil<>(MkContact.class);
        util.importTemplateExcel(response, "联系人数据");
    }

    /** P3-13: 导入联系人 */
    @Log(title = "联系人导入", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('marketing:contact:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<MkContact> util = new ExcelUtil<>(MkContact.class);
        List<MkContact> contactList = util.importExcel(file.getInputStream());
        String message = mkContactService.importContacts(contactList, updateSupport, getUsername());
        return AjaxResult.success(message);
    }

    // ==================== P3-14: 跟进提醒 ====================

    /** P3-14: 查询待跟进联系人 */
    @PreAuthorize("@ss.hasPermi('marketing:contact:list')")
    @GetMapping("/followUpReminders")
    public AjaxResult followUpReminders()
    {
        return AjaxResult.success(mkContactService.selectFollowUpReminders(getUserId()));
    }
}

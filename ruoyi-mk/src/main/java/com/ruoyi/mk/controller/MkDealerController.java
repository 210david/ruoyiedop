package com.ruoyi.mk.controller;

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
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.domain.MkDealerProfile;
import com.ruoyi.mk.service.IMkDealerProfileService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 经销商档案 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/dealer")
public class MkDealerController extends BaseController
{
    @Autowired
    private IMkDealerProfileService mkDealerProfileService;

    /** 经销商档案列表 */
    @PreAuthorize("@ss.hasPermi('marketing:dealer:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkDealerProfile profile)
    {
        startPage();
        List<MkDealerProfile> list = mkDealerProfileService.selectDealerProfileList(profile);
        return getDataTable(list);
    }

    /** 经销商层级树（按上级经销商关系构树） */
    @PreAuthorize("@ss.hasPermi('marketing:dealer:list')")
    @GetMapping("/tree")
    public AjaxResult tree(MkCustomer query)
    {
        return AjaxResult.success(mkDealerProfileService.selectDealerTree(query));
    }

    /** 导出经销商档案 */
    @Log(title = "经销商档案", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:dealer:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkDealerProfile profile)
    {
        List<MkDealerProfile> list = mkDealerProfileService.selectDealerProfileList(profile);
        ExcelUtil<MkDealerProfile> util = new ExcelUtil<>(MkDealerProfile.class);
        util.exportExcel(response, list, "经销商档案");
    }

    /** 档案详情 */
    @PreAuthorize("@ss.hasPermi('marketing:dealer:query')")
    @GetMapping(value = "/{profileId}")
    public AjaxResult getInfo(@PathVariable("profileId") Long profileId)
    {
        return AjaxResult.success(mkDealerProfileService.selectDealerProfileByProfileId(profileId));
    }

    /** 按客户ID查档案（详情页联动） */
    @PreAuthorize("@ss.hasPermi('marketing:dealer:query')")
    @GetMapping(value = "/customer/{customerId}")
    public AjaxResult getByCustomer(@PathVariable("customerId") Long customerId)
    {
        return AjaxResult.success(mkDealerProfileService.selectDealerProfileByCustomerId(customerId));
    }

    /** 新增经销商档案 */
    @Log(title = "经销商档案", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:dealer:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MkDealerProfile profile)
    {
        return toAjax(mkDealerProfileService.insertDealerProfile(profile));
    }

    /** 修改经销商档案 */
    @Log(title = "经销商档案", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:dealer:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MkDealerProfile profile)
    {
        return toAjax(mkDealerProfileService.updateDealerProfile(profile));
    }

    /** 删除经销商档案 */
    @Log(title = "经销商档案", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:dealer:remove')")
    @DeleteMapping("/{profileIds}")
    public AjaxResult remove(@PathVariable Long[] profileIds)
    {
        return toAjax(mkDealerProfileService.deleteDealerProfileByProfileIds(profileIds));
    }
}

package com.ruoyi.mk.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mk.domain.MkPromotionData;
import com.ruoyi.mk.service.IMkPromotionDataService;

/**
 * 推广数据 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/promotionData")
public class MkPromotionDataController extends BaseController
{
    @Autowired
    private IMkPromotionDataService mkPromotionDataService;

    @PreAuthorize("@ss.hasPermi('marketing:promotionData:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkPromotionData data)
    {
        startPage();
        List<MkPromotionData> list = mkPromotionDataService.selectDataList(data);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('marketing:promotionData:query')")
    @GetMapping(value = "/{dataId}")
    public AjaxResult getInfo(@PathVariable("dataId") Long dataId)
    {
        return AjaxResult.success(mkPromotionDataService.selectDataById(dataId));
    }

    /** 新增/修改（按内容包+日期 upsert） */
    @Log(title = "推广数据", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:promotionData:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MkPromotionData data)
    {
        return toAjax(mkPromotionDataService.saveData(data));
    }

    @Log(title = "推广数据", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:promotionData:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MkPromotionData data)
    {
        return toAjax(mkPromotionDataService.saveData(data));
    }

    /** Excel批量导入（勾选更新时按 内容包+日期 覆盖更新） */
    @Log(title = "推广数据导入", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('marketing:promotionData:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<MkPromotionData> util = new ExcelUtil<>(MkPromotionData.class);
        List<MkPromotionData> list = util.importExcel(file.getInputStream());
        // 非模板文件校验（规范 4.3.10.10）
        if (list == null || list.isEmpty() || list.stream().allMatch(java.util.Objects::isNull))
        {
            return AjaxResult.error("导入失败！请使用系统提供的标准模板填写数据，不要使用非模板格式的文件。");
        }
        list.removeIf(java.util.Objects::isNull);
        return mkPromotionDataService.importData(list, updateSupport, getUsername());
    }

    /** 导入模板下载 */
    @PostMapping("/importTemplate")
    public void importTemplate(jakarta.servlet.http.HttpServletResponse response)
    {
        ExcelUtil<MkPromotionData> util = new ExcelUtil<>(MkPromotionData.class);
        util.importTemplateExcel(response, "推广数据");
    }

    @Log(title = "推广数据", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:promotionData:remove')")
    @DeleteMapping("/{dataIds}")
    public AjaxResult remove(@PathVariable Long[] dataIds)
    {
        return toAjax(mkPromotionDataService.deleteDataByIds(dataIds));
    }
}

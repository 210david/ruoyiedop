package com.ruoyi.pdm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.pdm.domain.PdmDesignFile;
import com.ruoyi.pdm.service.IPdmDesignFileService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 图纸技术文件 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pdm/designFile")
public class PdmDesignFileController extends BaseController
{
    @Autowired
    private IPdmDesignFileService fileService;

    /**
     * 查询图纸技术文件列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('pdm:designFile:list')")
    @GetMapping("/list")
    public TableDataInfo list(PdmDesignFile file)
    {
        startPage();
        List<PdmDesignFile> list = fileService.selectFileList(file);
        return getDataTable(list);
    }

    /**
     * 导出图纸技术文件
     */
    @Log(title = "图纸技术文件", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pdm:designFile:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PdmDesignFile file)
    {
        List<PdmDesignFile> list = fileService.selectFileList(file);
        ExcelUtil<PdmDesignFile> util = new ExcelUtil<>(PdmDesignFile.class);
        util.exportExcel(response, list, "图纸技术文件数据");
    }

    /**
     * 获取文件详情
     */
    @PreAuthorize("@ss.hasPermi('pdm:designFile:query')")
    @GetMapping(value = "/{fileId}")
    public AjaxResult getInfo(@PathVariable("fileId") Long fileId)
    {
        return success(fileService.selectFileById(fileId));
    }

    /**
     * 上传资料（上传即纳入版本管理）
     */
    @Log(title = "图纸技术文件", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pdm:designFile:add')")
    @PostMapping
    public AjaxResult add(@RequestBody PdmDesignFile file)
    {
        return toAjax(fileService.insertFile(file));
    }

    /**
     * 修改资料（文件替换自动升版）
     */
    @Log(title = "图纸技术文件", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:designFile:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody PdmDesignFile file)
    {
        return toAjax(fileService.updateFile(file));
    }

    /**
     * 文件生效（版本生效 + 置为受控状态）
     */
    @Log(title = "文件生效", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:designFile:effective')")
    @PutMapping("/effective")
    public AjaxResult effective(@RequestBody PdmDesignFile params)
    {
        return toAjax(fileService.effectiveFile(params.getFileId(), params.getVersionId(), params.getRemark()));
    }

    /**
     * 手工升版（生成新版本记录）
     */
    @Log(title = "文件升版", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:designFile:edit')")
    @PutMapping("/upgrade")
    public AjaxResult upgrade(@RequestBody PdmDesignFile params)
    {
        return toAjax(fileService.upgradeFile(params.getFileId(), params.getRemark(), Boolean.TRUE.equals(params.getMajorUpgrade())));
    }

    /**
     * 删除资料（受控文件不允许删除）
     */
    @Log(title = "图纸技术文件", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pdm:designFile:remove')")
    @DeleteMapping("/{fileIds}")
    public AjaxResult remove(@PathVariable Long[] fileIds)
    {
        return toAjax(fileService.deleteFileByIds(fileIds));
    }
}

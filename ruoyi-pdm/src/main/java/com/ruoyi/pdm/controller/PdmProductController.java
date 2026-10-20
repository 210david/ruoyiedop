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
import com.ruoyi.pdm.domain.PdmProduct;
import com.ruoyi.pdm.service.IPdmProductService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 产品档案 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pdm/product")
public class PdmProductController extends BaseController
{
    @Autowired
    private IPdmProductService productService;

    /**
     * 查询产品档案列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('pdm:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(PdmProduct product)
    {
        startPage();
        List<PdmProduct> list = productService.selectProductList(product);
        return getDataTable(list);
    }

    /**
     * 产品档案列表（全量，供选择器）
     */
    @PreAuthorize("@ss.hasPermi('pdm:product:list')")
    @GetMapping("/listAll")
    public AjaxResult listAll(PdmProduct product)
    {
        return success(productService.selectProductList(product));
    }

    /**
     * 导出产品档案
     */
    @Log(title = "产品档案", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pdm:product:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PdmProduct product)
    {
        List<PdmProduct> list = productService.selectProductList(product);
        ExcelUtil<PdmProduct> util = new ExcelUtil<>(PdmProduct.class);
        util.exportExcel(response, list, "产品档案数据");
    }

    /**
     * 获取产品档案详情（一物一档聚合入口）
     */
    @PreAuthorize("@ss.hasPermi('pdm:product:query')")
    @GetMapping(value = "/{productId}")
    public AjaxResult getInfo(@PathVariable("productId") Long productId)
    {
        return success(productService.selectProductById(productId));
    }

    /**
     * 新增产品档案（产品编码=物料编码）
     */
    @Log(title = "产品档案", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pdm:product:add')")
    @PostMapping
    public AjaxResult add(@RequestBody PdmProduct product)
    {
        return toAjax(productService.insertProduct(product));
    }

    /**
     * 修改产品档案
     */
    @Log(title = "产品档案", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:product:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody PdmProduct product)
    {
        return toAjax(productService.updateProduct(product));
    }

    /**
     * 产品停产（已定型 → 已停产；生命周期终止动作，不走变更单）
     */
    @Log(title = "产品停产", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:product:discontinue')")
    @PutMapping("/discontinue/{productId}")
    public AjaxResult discontinue(@PathVariable Long productId, @RequestBody(required = false) PdmProduct params)
    {
        return toAjax(productService.discontinueProduct(productId, params != null ? params.getRemark() : null));
    }

    /**
     * 产品复产（已停产 → 已定型）
     */
    @Log(title = "产品复产", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:product:resume')")
    @PutMapping("/resume/{productId}")
    public AjaxResult resume(@PathVariable Long productId)
    {
        return toAjax(productService.resumeProduct(productId));
    }

    /**
     * 删除产品档案（仅限"设计中"且无下游引用）
     */
    @Log(title = "产品档案", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pdm:product:remove')")
    @DeleteMapping("/{productIds}")
    public AjaxResult remove(@PathVariable Long[] productIds)
    {
        return toAjax(productService.deleteProductByIds(productIds));
    }
}

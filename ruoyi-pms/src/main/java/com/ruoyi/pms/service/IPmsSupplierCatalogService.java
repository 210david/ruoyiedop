package com.ruoyi.pms.service;

import java.util.List;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.pms.domain.PmsSupplierCatalog;

/**
 * 供应商供货清单 Service接口
 *
 * @author ruoyi
 */
public interface IPmsSupplierCatalogService
{
    public List<PmsSupplierCatalog> selectCatalogList(PmsSupplierCatalog catalog);
    public PmsSupplierCatalog selectCatalogById(Long catalogId);
    public int insertCatalog(PmsSupplierCatalog catalog);
    public int updateCatalog(PmsSupplierCatalog catalog);
    public int deleteCatalogByIds(Long[] catalogIds);
    public AjaxResult importCatalog(List<PmsSupplierCatalog> catalogList, Boolean isUpdateSupport, String operName);
}

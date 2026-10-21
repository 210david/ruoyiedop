package com.ruoyi.pms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.pms.domain.PmsSupplierCatalog;

/**
 * 供应商供货清单 Mapper
 *
 * @author ruoyi
 */
public interface PmsSupplierCatalogMapper
{
    public List<PmsSupplierCatalog> selectCatalogList(PmsSupplierCatalog catalog);
    public PmsSupplierCatalog selectCatalogById(Long catalogId);
    public PmsSupplierCatalog selectCatalogBySupplierAndMaterial(@Param("supplierId") Long supplierId, @Param("materialId") Long materialId);
    public int insertCatalog(PmsSupplierCatalog catalog);
    public int updateCatalog(PmsSupplierCatalog catalog);
    public int deleteCatalogByIds(Long[] catalogIds);
}

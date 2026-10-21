package com.ruoyi.wms.mapper;

import java.util.List;
import com.ruoyi.wms.domain.WmsSupplier;

public interface WmsSupplierMapper
{
    public List<WmsSupplier> selectSupplierList(WmsSupplier supplier);
    public WmsSupplier selectSupplierById(Long supplierId);
    public int insertSupplier(WmsSupplier supplier);
    public int updateSupplier(WmsSupplier supplier);
    public int deleteSupplierByIds(Long[] supplierIds);
}

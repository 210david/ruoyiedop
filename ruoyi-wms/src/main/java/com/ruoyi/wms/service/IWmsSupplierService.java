package com.ruoyi.wms.service;

import java.util.List;
import com.ruoyi.wms.domain.WmsSupplier;

public interface IWmsSupplierService
{
    public List<WmsSupplier> selectSupplierList(WmsSupplier supplier);
    public WmsSupplier selectSupplierById(Long supplierId);
    public int insertSupplier(WmsSupplier supplier);
    public int updateSupplier(WmsSupplier supplier);
    public int deleteSupplierByIds(Long[] supplierIds);
}

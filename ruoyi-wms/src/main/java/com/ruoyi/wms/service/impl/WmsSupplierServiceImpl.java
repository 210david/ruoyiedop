package com.ruoyi.wms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.mapper.WmsSupplierMapper;
import com.ruoyi.wms.service.IWmsSupplierService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class WmsSupplierServiceImpl implements IWmsSupplierService
{
    @Autowired
    private WmsSupplierMapper wmsSupplierMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<WmsSupplier> selectSupplierList(WmsSupplier supplier)
    {
        return wmsSupplierMapper.selectSupplierList(supplier);
    }

    @Override
    public WmsSupplier selectSupplierById(Long supplierId)
    {
        return wmsSupplierMapper.selectSupplierById(supplierId);
    }

    @Override
    public int insertSupplier(WmsSupplier supplier)
    {
        supplier.setDelFlag("0");
        if (StringUtils.isEmpty(supplier.getSupplierCode()))
        {
            supplier.setSupplierCode(mkNumberRuleService.generateNumber("wms_supplier"));
        }
        return wmsSupplierMapper.insertSupplier(supplier);
    }

    @Override
    public int updateSupplier(WmsSupplier supplier)
    {
        return wmsSupplierMapper.updateSupplier(supplier);
    }

    @Override
    public int deleteSupplierByIds(Long[] supplierIds)
    {
        return wmsSupplierMapper.deleteSupplierByIds(supplierIds);
    }
}

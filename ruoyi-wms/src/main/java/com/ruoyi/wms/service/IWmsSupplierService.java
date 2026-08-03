package com.ruoyi.wms.service;

import java.util.List;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wms.domain.WmsSupplier;

public interface IWmsSupplierService
{
    public List<WmsSupplier> selectSupplierList(WmsSupplier supplier);
    public WmsSupplier selectSupplierById(Long supplierId);
    public int insertSupplier(WmsSupplier supplier);
    public int updateSupplier(WmsSupplier supplier);
    public int deleteSupplierByIds(Long[] supplierIds);

    /**
     * 导入供应商数据
     *
     * @param supplierList 供应商数据列表
     * @param isUpdateSupport 是否更新已存在的供应商
     * @param updateKey 更新匹配字段（supplierName=名称, unifiedCreditCode=统一信用代码, supplierCode=编码）
     * @param operName 操作人
     * @return 导入结果（含成功/失败明细）
     */
    public AjaxResult importSupplier(List<WmsSupplier> supplierList, Boolean isUpdateSupport, String updateKey, String operName);
}

package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsPurchaseRequest;
import com.ruoyi.pms.domain.PmsPurchaseRequestDetail;

/**
 * 采购申请 Mapper
 *
 * @author ruoyi
 */
public interface PmsPurchaseRequestMapper
{
    public List<PmsPurchaseRequest> selectPurchaseRequestList(PmsPurchaseRequest request);
    public PmsPurchaseRequest selectPurchaseRequestById(Long requestId);
    public int insertPurchaseRequest(PmsPurchaseRequest request);
    public int updatePurchaseRequest(PmsPurchaseRequest request);
    public int deletePurchaseRequestByIds(Long[] requestIds);
    public List<PmsPurchaseRequestDetail> selectRequestDetailByRequestId(Long requestId);
    public int insertRequestDetail(PmsPurchaseRequestDetail detail);
    public int deleteRequestDetailByRequestId(Long requestId);
}

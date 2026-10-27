package com.ruoyi.pms.service;

import java.util.List;
import com.ruoyi.pms.domain.PmsPurchaseRequest;

/**
 * 采购申请 Service接口
 *
 * @author ruoyi
 */
public interface IPmsPurchaseRequestService
{
    public List<PmsPurchaseRequest> selectPurchaseRequestList(PmsPurchaseRequest request);
    public PmsPurchaseRequest selectPurchaseRequestById(Long requestId);
    public int insertPurchaseRequest(PmsPurchaseRequest request);
    public int updatePurchaseRequest(PmsPurchaseRequest request);
    public int deletePurchaseRequestByIds(Long[] requestIds);
    public int submitPurchaseRequest(Long requestId);
    public int auditPurchaseRequest(Long requestId, String status, String auditOpinion);
}

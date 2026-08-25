package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsReturnMaterial;
import com.ruoyi.mms.domain.MmsReturnDetail;

/**
 * 退料 Service接口
 *
 * @author ruoyi
 */
public interface IMmsReturnMaterialService
{
    public List<MmsReturnMaterial> selectReturnMaterialList(MmsReturnMaterial returnMaterial);
    public MmsReturnMaterial selectReturnMaterialById(Long ReturnId);
    public int insertReturnMaterial(MmsReturnMaterial returnMaterial);
    public int updateReturnMaterial(MmsReturnMaterial returnMaterial);
    public int deleteReturnMaterialByIds(Long[] ReturnIds);

    /**
     * 确认退料：状态 0(待退料) → 1(已退料)，记录退料时间
     */
    public int confirmReturnMaterial(MmsReturnMaterial returnMaterial);

    /**
     * 查询某个领料单已退料的明细（按物料汇总退料数量）
     * 只查状态为已退料(1)的退料单中的明细
     */
    public List<MmsReturnDetail> selectReturnedQtyByIssueId(Long issueId);

    /**
     * 按工单号查询退料明细（按物料拆分，每条明细一行）
     */
    public List<java.util.Map<String, Object>> selectReturnDetailListByWorkOrderNo(String workOrderNo);
}

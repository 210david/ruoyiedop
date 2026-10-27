package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsNcr;

/**
 * 不合格品报告 Service接口
 *
 * @author ruoyi
 */
public interface IQmsNcrService
{
    public List<QmsNcr> selectNcrList(QmsNcr ncr);
    public QmsNcr selectNcrById(Long ncrId);
    public int insertNcr(QmsNcr ncr);
    public int updateNcr(QmsNcr ncr);
    public int deleteNcrByIds(Long[] ncrIds);
    public int verifyNcr(QmsNcr ncr);
    public int closeNcr(Long ncrId);

    /** 提交MRB评审：已登记(0) → 评审中(1)，根据缺陷等级确定审批节点 */
    public int submitReview(Long ncrId);

    /** 审批通过：推进到下一节点，全部通过后进入处置中(2) */
    public int approveNode(Long ncrId, String approveOpinion);

    /** 审批驳回：退回到已登记(0) */
    public int rejectNode(Long ncrId, String approveOpinion);

    /** 提交处置结论：处置中(2) → 待验证(3) */
    public int submitDisposition(QmsNcr ncr);
}

package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsFinishReceipt;

/**
 * 完工入库 Service接口
 *
 * @author ruoyi
 */
public interface IMmsFinishReceiptService
{
    public List<MmsFinishReceipt> selectFinishReceiptList(MmsFinishReceipt finishReceipt);
    public MmsFinishReceipt selectFinishReceiptById(Long FinishId);
    public int insertFinishReceipt(MmsFinishReceipt finishReceipt);
    public int updateFinishReceipt(MmsFinishReceipt finishReceipt);
    public int deleteFinishReceiptByIds(Long[] FinishIds);
}

package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsFinishReceipt;

public interface MmsFinishReceiptMapper {
    public List<MmsFinishReceipt> selectFinishReceiptList(MmsFinishReceipt fr);
    public MmsFinishReceipt selectFinishReceiptById(Long finishId);
    public int insertFinishReceipt(MmsFinishReceipt fr);
    public int updateFinishReceipt(MmsFinishReceipt fr);
    public int deleteFinishReceiptByIds(Long[] finishIds);
}
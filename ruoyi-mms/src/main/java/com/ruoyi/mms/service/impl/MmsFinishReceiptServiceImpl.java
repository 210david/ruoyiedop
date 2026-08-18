package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsFinishReceipt;
import com.ruoyi.mms.mapper.MmsFinishReceiptMapper;
import com.ruoyi.mms.service.IMmsFinishReceiptService;

/**
 * 完工入库 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsFinishReceiptServiceImpl implements IMmsFinishReceiptService
{
    @Autowired
    private MmsFinishReceiptMapper finishReceiptMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsFinishReceipt> selectFinishReceiptList(MmsFinishReceipt finishReceipt)
    {
        return finishReceiptMapper.selectFinishReceiptList(finishReceipt);
    }

    @Override
    public MmsFinishReceipt selectFinishReceiptById(Long FinishId)
    {
        return finishReceiptMapper.selectFinishReceiptById(FinishId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertFinishReceipt(MmsFinishReceipt finishReceipt)
    {
        // 自动生成完工入库单号
        if (StringUtils.isEmpty(finishReceipt.getFinishNo()))
        {
            finishReceipt.setFinishNo(mkNumberRuleService.generateNumber("mms_finish_receipt"));
        }
        finishReceipt.setDelFlag("0");
        return finishReceiptMapper.insertFinishReceipt(finishReceipt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateFinishReceipt(MmsFinishReceipt finishReceipt)
    {
        return finishReceiptMapper.updateFinishReceipt(finishReceipt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFinishReceiptByIds(Long[] FinishIds)
    {
        return finishReceiptMapper.deleteFinishReceiptByIds(FinishIds);
    }
}

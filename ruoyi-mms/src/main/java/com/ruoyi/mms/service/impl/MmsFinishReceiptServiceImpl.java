package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
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
        finishReceipt.setStatus("0");
        finishReceipt.setCreateBy(SecurityUtils.getUsername());
        finishReceipt.setCreateTime(DateUtils.getNowDate());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmFinishReceipt(MmsFinishReceipt finishReceipt)
    {
        // 校验：只有待入库状态才能确认入库
        MmsFinishReceipt existing = finishReceiptMapper.selectFinishReceiptById(finishReceipt.getFinishId());
        if (existing == null)
        {
            throw new ServiceException("完工入库单不存在");
        }
        if (!"0".equals(existing.getStatus()))
        {
            throw new ServiceException("只有待入库状态的完工入库单才能进行入库确认");
        }
        // 设置入库信息
        finishReceipt.setStatus("1");
        finishReceipt.setInboundTime(finishReceipt.getInboundTime() != null ? finishReceipt.getInboundTime() : DateUtils.getNowDate());
        finishReceipt.setInboundBy(SecurityUtils.getUsername());
        finishReceipt.setUpdateBy(SecurityUtils.getUsername());
        finishReceipt.setUpdateTime(DateUtils.getNowDate());
        // 如果前端传了批次号，同步更新
        return finishReceiptMapper.updateFinishReceipt(finishReceipt);
    }
}

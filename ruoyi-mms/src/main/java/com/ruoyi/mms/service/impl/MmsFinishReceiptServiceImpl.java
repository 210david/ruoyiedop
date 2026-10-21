package com.ruoyi.mms.service.impl;

import com.ruoyi.common.annotation.DataScope;
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
import com.ruoyi.mms.service.IMmsGenealogySyncService;

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

    @Autowired
    private IMmsGenealogySyncService genealogySyncService;

    @Override
    @DataScope(deptAlias = "mms_finish_receipt")
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
        // 批次号强制必填：产出批次是追溯谱系的目的地（complete 事件），缺失将导致整条链路无产出节点
        String effBatchNo = StringUtils.isNotEmpty(finishReceipt.getBatchNo()) ? finishReceipt.getBatchNo() : existing.getBatchNo();
        if (StringUtils.isEmpty(effBatchNo))
        {
            throw new ServiceException("完工入库单[" + existing.getFinishNo() + "]批次号必填，请填写完工批次后重新提交");
        }
        // 设置入库信息
        finishReceipt.setStatus("1");
        finishReceipt.setInboundTime(finishReceipt.getInboundTime() != null ? finishReceipt.getInboundTime() : DateUtils.getNowDate());
        finishReceipt.setInboundBy(SecurityUtils.getUsername());
        finishReceipt.setUpdateBy(SecurityUtils.getUsername());
        finishReceipt.setUpdateTime(DateUtils.getNowDate());
        // 如果前端传了批次号，同步更新
        int rows = finishReceiptMapper.updateFinishReceipt(finishReceipt);

        // ===== 谱系联动：完工入库确认 → 写入完工谱系（complete）并回填该工单领料谱系产出批次（幂等，失败不阻断） =====
        MmsFinishReceipt merged = finishReceiptMapper.selectFinishReceiptById(finishReceipt.getFinishId());
        if (merged != null)
        {
            genealogySyncService.syncComplete(
                    merged.getFinishNo(), merged.getWorkOrderNo(),
                    merged.getProductId(), merged.getProductCode(), merged.getProductName(),
                    merged.getBatchNo(), merged.getFinishQty(),
                    merged.getFinishBy(), merged.getFinishTime() != null ? merged.getFinishTime() : merged.getInboundTime());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFinishReceiptByIds(Long[] FinishIds)
    {
        // ===== 谱系联动：撤销对应 complete 谱系（失败不阻断） =====
        for (Long id : FinishIds)
        {
            MmsFinishReceipt receipt = finishReceiptMapper.selectFinishReceiptById(id);
            if (receipt != null && "1".equals(receipt.getStatus()))
            {
                genealogySyncService.revoke("complete", receipt.getFinishNo());
            }
        }
        return finishReceiptMapper.deleteFinishReceiptByIds(FinishIds);
    }
}

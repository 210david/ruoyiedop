package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.domain.MkDealerSellFlow;
import com.ruoyi.mk.mapper.MkCustomerMapper;
import com.ruoyi.mk.mapper.MkDealerSellFlowMapper;
import com.ruoyi.mk.service.IMkDealerSellFlowService;

/**
 * 经销商分销流向登记 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkDealerSellFlowServiceImpl implements IMkDealerSellFlowService
{
    @Autowired
    private MkDealerSellFlowMapper mkDealerSellFlowMapper;

    @Autowired
    private MkCustomerMapper mkCustomerMapper;

    @Override
    public List<MkDealerSellFlow> selectFlowList(MkDealerSellFlow flow)
    {
        return mkDealerSellFlowMapper.selectFlowList(flow);
    }

    @Override
    public MkDealerSellFlow selectFlowById(Long flowId)
    {
        return mkDealerSellFlowMapper.selectFlowById(flowId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertFlow(MkDealerSellFlow flow)
    {
        validateFlow(flow);
        checkDuplicate(flow);
        flow.setCreateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());
        return mkDealerSellFlowMapper.insertFlow(flow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateFlow(MkDealerSellFlow flow)
    {
        if (mkDealerSellFlowMapper.selectFlowById(flow.getFlowId()) == null)
        {
            throw new ServiceException("流向记录不存在或已删除");
        }
        if (flow.getQuantity() != null && flow.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("流向数量必须大于0");
        }
        flow.setUpdateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());
        return mkDealerSellFlowMapper.updateFlow(flow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFlowByIds(Long[] flowIds)
    {
        return mkDealerSellFlowMapper.deleteFlowByIds(flowIds);
    }

    @Override
    public List<MkDealerSellFlow> generateDraftByShipment(Long shipmentId)
    {
        List<MkDealerSellFlow> drafts = mkDealerSellFlowMapper.selectDraftItemsByShipmentId(shipmentId);
        if (drafts == null || drafts.isEmpty())
        {
            throw new ServiceException("发货单不存在或无明细，无法生成流向草稿");
        }
        for (MkDealerSellFlow draft : drafts)
        {
            draft.setFlowDate(new Date());
            draft.setBatchNo(null);
            draft.setDownstreamType("4");
            draft.setDownstreamName(null);
        }
        return drafts;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSaveFlows(List<MkDealerSellFlow> flows, String operName)
    {
        if (flows == null || flows.isEmpty())
        {
            throw new ServiceException("流向草稿数据不能为空");
        }
        int saved = 0;
        for (MkDealerSellFlow flow : flows)
        {
            // 唯一键防重：同发货单同物料只登记一次
            if (flow.getShipmentId() != null && flow.getMaterialId() != null
                    && mkDealerSellFlowMapper.selectByShipmentAndMaterial(flow.getShipmentId(), flow.getMaterialId()) != null)
            {
                continue;
            }
            validateFlow(flow);
            flow.setCreateBy(operName);
            saved += mkDealerSellFlowMapper.insertFlow(flow);
        }
        return saved;
    }

    /** 基础校验：经销商类型 + 日期 + 数量 */
    private void validateFlow(MkDealerSellFlow flow)
    {
        MkCustomer dealer = mkCustomerMapper.selectCustomerTypeById(flow.getDealerId());
        if (dealer == null || !MkCustomer.TYPE_DEALER.equals(dealer.getCustomerType()))
        {
            throw new ServiceException("流向必须登记在「经销商」类型的客户名下");
        }
        if (flow.getFlowDate() == null)
        {
            throw new ServiceException("流向日期不能为空");
        }
        if (flow.getMaterialId() == null)
        {
            throw new ServiceException("物料不能为空");
        }
        if (flow.getQuantity() == null || flow.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("流向数量必须大于0");
        }
    }

    /** 同发货单同物料防重（唯一键前置校验，给出友好提示） */
    private void checkDuplicate(MkDealerSellFlow flow)
    {
        if (flow.getShipmentId() != null && flow.getMaterialId() != null
                && mkDealerSellFlowMapper.selectByShipmentAndMaterial(flow.getShipmentId(), flow.getMaterialId()) != null)
        {
            throw new ServiceException("该发货单下此物料已登记流向，请勿重复登记");
        }
    }
}

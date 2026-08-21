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
import com.ruoyi.mms.domain.MmsFlowCard;
import com.ruoyi.mms.mapper.MmsFlowCardMapper;
import com.ruoyi.mms.service.IMmsFlowCardService;

/**
 * 流转卡 Service实现
 *
 * 流转卡状态：
 * 0(在制) → 1(完工) / 2(报废)
 *
 * @author ruoyi
 */
@Service
public class MmsFlowCardServiceImpl implements IMmsFlowCardService
{
    @Autowired
    private MmsFlowCardMapper flowCardMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    // ========== 标准 CRUD ==========

    @Override
    public List<MmsFlowCard> selectFlowCardList(MmsFlowCard flowCard)
    {
        return flowCardMapper.selectFlowCardList(flowCard);
    }

    @Override
    public MmsFlowCard selectFlowCardById(Long cardId)
    {
        return flowCardMapper.selectFlowCardById(cardId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertFlowCard(MmsFlowCard flowCard)
    {
        if (StringUtils.isEmpty(flowCard.getStatus()))
        {
            flowCard.setStatus("0");
        }
        flowCard.setDelFlag("0");
        if (StringUtils.isEmpty(flowCard.getCardCode()))
        {
            flowCard.setCardCode(mkNumberRuleService.generateNumber("FC"));
        }
        if (flowCard.getPrintCount() == null)
        {
            flowCard.setPrintCount(0);
        }
        flowCard.setCreateBy(SecurityUtils.getUsername());
        flowCard.setCreateTime(DateUtils.getNowDate());
        return flowCardMapper.insertFlowCard(flowCard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateFlowCard(MmsFlowCard flowCard)
    {
        flowCard.setUpdateBy(SecurityUtils.getUsername());
        return flowCardMapper.updateFlowCard(flowCard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFlowCardByIds(Long[] cardIds)
    {
        return flowCardMapper.deleteFlowCardByIds(cardIds);
    }

    // ========== 业务操作 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int printFlowCard(Long cardId)
    {
        MmsFlowCard card = getAndCheckFlowCard(cardId);
        card.setPrintCount(card.getPrintCount() == null ? 1 : card.getPrintCount() + 1);
        card.setUpdateBy(SecurityUtils.getUsername());
        return flowCardMapper.updateFlowCard(card);
    }

    @Override
    public MmsFlowCard scanFlowCard(String cardCode)
    {
        MmsFlowCard query = new MmsFlowCard();
        query.setCardCode(cardCode);
        List<MmsFlowCard> list = flowCardMapper.selectFlowCardList(query);
        if (list == null || list.isEmpty())
        {
            throw new ServiceException("流转卡[" + cardCode + "]不存在");
        }
        return list.get(0);
    }

    // ========== 私有辅助方法 ==========

    private MmsFlowCard getAndCheckFlowCard(Long cardId)
    {
        MmsFlowCard card = flowCardMapper.selectFlowCardById(cardId);
        if (card == null)
        {
            throw new ServiceException("流转卡不存在或已删除");
        }
        return card;
    }
}

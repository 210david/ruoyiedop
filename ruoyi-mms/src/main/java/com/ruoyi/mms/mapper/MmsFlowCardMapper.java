package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsFlowCard;

public interface MmsFlowCardMapper
{
    public List<MmsFlowCard> selectFlowCardList(MmsFlowCard flowCard);
    public MmsFlowCard selectFlowCardById(Long cardId);
    public int insertFlowCard(MmsFlowCard flowCard);
    public int updateFlowCard(MmsFlowCard flowCard);
    public int deleteFlowCardByIds(Long[] cardIds);
}

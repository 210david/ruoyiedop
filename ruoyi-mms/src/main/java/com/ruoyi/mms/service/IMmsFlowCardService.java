package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsFlowCard;

/**
 * 流转卡 Service接口
 *
 * 流转卡状态：
 * 0(在制) → 1(完工) / 2(报废)
 *
 * @author ruoyi
 */
public interface IMmsFlowCardService
{
    /** 标准CRUD */
    public List<MmsFlowCard> selectFlowCardList(MmsFlowCard flowCard);
    public MmsFlowCard selectFlowCardById(Long cardId);
    public int insertFlowCard(MmsFlowCard flowCard);
    public int updateFlowCard(MmsFlowCard flowCard);
    public int deleteFlowCardByIds(Long[] cardIds);

    /** 打印流转卡（增加打印次数） */
    public int printFlowCard(Long cardId);

    /** 扫码定位（返回当前工序与历史） */
    public MmsFlowCard scanFlowCard(String cardCode);
}

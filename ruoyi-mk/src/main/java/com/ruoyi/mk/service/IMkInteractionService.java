package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkInteraction;

/**
 * 互动记录 Service接口
 *
 * @author ruoyi
 */
public interface IMkInteractionService
{
    public List<MkInteraction> selectInteractionList(MkInteraction interaction);
    public MkInteraction selectInteractionById(Long recordId);
    public int insertInteraction(MkInteraction interaction);
    public int updateInteraction(MkInteraction interaction);
    public int deleteInteractionByIds(Long[] recordIds);
}

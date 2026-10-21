package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkInteraction;

public interface MkInteractionMapper
{
    public List<MkInteraction> selectInteractionList(MkInteraction interaction);
    public MkInteraction selectInteractionById(Long recordId);
    public int insertInteraction(MkInteraction interaction);
    public int updateInteraction(MkInteraction interaction);
    public int deleteInteractionByIds(Long[] recordIds);
}

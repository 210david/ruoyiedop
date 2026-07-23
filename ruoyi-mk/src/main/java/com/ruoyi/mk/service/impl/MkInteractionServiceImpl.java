package com.ruoyi.mk.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mk.domain.MkInteraction;
import com.ruoyi.mk.mapper.MkInteractionMapper;
import com.ruoyi.mk.service.IMkInteractionService;

/**
 * 互动记录 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkInteractionServiceImpl implements IMkInteractionService
{
    @Autowired
    private MkInteractionMapper mkInteractionMapper;

    @Override
    public List<MkInteraction> selectInteractionList(MkInteraction interaction)
    {
        return mkInteractionMapper.selectInteractionList(interaction);
    }

    @Override
    public MkInteraction selectInteractionById(Long recordId)
    {
        return mkInteractionMapper.selectInteractionById(recordId);
    }

    @Override
    public int insertInteraction(MkInteraction interaction)
    {
        interaction.setDelFlag("0");
        return mkInteractionMapper.insertInteraction(interaction);
    }

    @Override
    public int updateInteraction(MkInteraction interaction)
    {
        return mkInteractionMapper.updateInteraction(interaction);
    }

    @Override
    public int deleteInteractionByIds(Long[] recordIds)
    {
        return mkInteractionMapper.deleteInteractionByIds(recordIds);
    }
}

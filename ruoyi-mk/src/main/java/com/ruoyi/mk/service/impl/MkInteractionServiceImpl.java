package com.ruoyi.mk.service.impl;

import com.ruoyi.common.annotation.DataScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mk.domain.MkInteraction;
import com.ruoyi.mk.mapper.MkInteractionMapper;
import com.ruoyi.mk.mapper.MkContactMapper;
import com.ruoyi.mk.mapper.MkCustomerMapper;
import com.ruoyi.mk.domain.MkContact;
import com.ruoyi.mk.domain.MkCustomer;
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

    @Autowired
    private MkContactMapper mkContactMapper;

    @Autowired
    private MkCustomerMapper mkCustomerMapper;

    @Override
    @DataScope(userAlias = "i")
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
        // 归属补全：客户为空但选了联系人时，由联系人反推客户，避免记录游离在任何客户卡片之外
        if (interaction.getCustomerId() == null && interaction.getContactId() != null)
        {
            MkContact contact = mkContactMapper.selectContactById(interaction.getContactId());
            if (contact != null)
            {
                interaction.setCustomerId(contact.getCustomerId());
            }
        }
        // 归属补全：负责人/部门为空时继承客户负责人，避免数据权限下不可见
        if (interaction.getCustomerId() != null && interaction.getUserId() == null)
        {
            MkCustomer owner = mkCustomerMapper.selectCustomerById(interaction.getCustomerId());
            if (owner != null)
            {
                interaction.setUserId(owner.getUserId());
            }
        }
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

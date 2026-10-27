package com.ruoyi.mk.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mk.domain.MkCustomerTag;
import com.ruoyi.mk.mapper.MkCustomerTagMapper;
import com.ruoyi.mk.service.IMkCustomerTagService;

/**
 * 客户标签 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkCustomerTagServiceImpl implements IMkCustomerTagService
{
    @Autowired
    private MkCustomerTagMapper mkCustomerTagMapper;

    @Override
    public List<MkCustomerTag> selectTagList(MkCustomerTag tag)
    {
        return mkCustomerTagMapper.selectTagList(tag);
    }

    @Override
    public MkCustomerTag selectTagById(Long tagId)
    {
        return mkCustomerTagMapper.selectTagById(tagId);
    }

    @Override
    public int insertTag(MkCustomerTag tag)
    {
        if (tag.getStatus() == null)
        {
            tag.setStatus("0");
        }
        if (tag.getTagType() == null)
        {
            tag.setTagType("0");
        }
        if (tag.getSort() == null)
        {
            tag.setSort(0);
        }
        return mkCustomerTagMapper.insertTag(tag);
    }

    @Override
    public int updateTag(MkCustomerTag tag)
    {
        return mkCustomerTagMapper.updateTag(tag);
    }

    @Override
    public int deleteTagByIds(Long[] tagIds)
    {
        return mkCustomerTagMapper.deleteTagByIds(tagIds);
    }
}

package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkCustomerTag;

public interface MkCustomerTagMapper
{
    public List<MkCustomerTag> selectTagList(MkCustomerTag tag);
    public MkCustomerTag selectTagById(Long tagId);
    public int insertTag(MkCustomerTag tag);
    public int updateTag(MkCustomerTag tag);
    public int deleteTagByIds(Long[] tagIds);
}

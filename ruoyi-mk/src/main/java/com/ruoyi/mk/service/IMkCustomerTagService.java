package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkCustomerTag;

/**
 * 客户标签 Service接口
 *
 * @author ruoyi
 */
public interface IMkCustomerTagService
{
    public List<MkCustomerTag> selectTagList(MkCustomerTag tag);
    public MkCustomerTag selectTagById(Long tagId);
    public int insertTag(MkCustomerTag tag);
    public int updateTag(MkCustomerTag tag);
    public int deleteTagByIds(Long[] tagIds);
}

package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkContent;

/**
 * 内容主档 数据层
 *
 * @author ruoyi
 */
public interface MkContentMapper
{
    public MkContent selectContentById(Long contentId);

    public List<MkContent> selectContentList(MkContent content);

    public int insertContent(MkContent content);

    public int updateContent(MkContent content);

    public int deleteContentByIds(Long[] contentIds);
}

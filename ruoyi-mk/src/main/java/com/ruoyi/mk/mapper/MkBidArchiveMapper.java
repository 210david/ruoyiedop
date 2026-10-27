package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkBidArchive;
import com.ruoyi.mk.domain.MkBidItem;

/**
 * 招投标档案 Mapper
 *
 * @author ruoyi
 */
public interface MkBidArchiveMapper
{
    public List<MkBidArchive> selectBidList(MkBidArchive bid);

    public MkBidArchive selectBidById(Long bidId);

    public int insertBid(MkBidArchive bid);

    public int updateBid(MkBidArchive bid);

    public int deleteBidByIds(Long[] bidIds);

    public List<MkBidItem> selectBidItems(Long bidId);

    public int insertBidItem(MkBidItem item);

    public int deleteBidItemsByBidId(Long bidId);
}

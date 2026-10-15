package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkBidArchive;

/**
 * 招投标档案 Service接口
 *
 * @author ruoyi
 */
public interface IMkBidArchiveService
{
    public List<MkBidArchive> selectBidList(MkBidArchive bid);

    public MkBidArchive selectBidById(Long bidId);

    public int insertBid(MkBidArchive bid);

    public int updateBid(MkBidArchive bid);

    public int deleteBidByIds(Long[] bidIds);

    /** 标记中标/未中标/废标 */
    public int markResult(Long bidId, String bidStatus, java.math.BigDecimal winAmount, java.util.Date resultDate);

    /** 中标一键生成合同草稿 */
    public MkBidArchive createContractFromBid(Long bidId);
}

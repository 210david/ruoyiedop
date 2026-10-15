package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkBidArchive;
import com.ruoyi.mk.domain.MkBidItem;
import com.ruoyi.mk.domain.MkContract;
import com.ruoyi.mk.mapper.MkBidArchiveMapper;
import com.ruoyi.mk.service.IMkBidArchiveService;
import com.ruoyi.mk.service.IMkContractService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 招投标档案 Service实现
 *
 * 状态流转：0编制 → 投标 → 1已投标 → 开标 → 2中标/3未中标/4废标
 * 中标后可一键生成合同草稿（关联合同后不可重复生成）
 *
 * @author ruoyi
 */
@Service
public class MkBidArchiveServiceImpl implements IMkBidArchiveService
{
    @Autowired
    private MkBidArchiveMapper mkBidArchiveMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private IMkContractService mkContractService;

    @Override
    public List<MkBidArchive> selectBidList(MkBidArchive bid)
    {
        return mkBidArchiveMapper.selectBidList(bid);
    }

    @Override
    public MkBidArchive selectBidById(Long bidId)
    {
        MkBidArchive bid = mkBidArchiveMapper.selectBidById(bidId);
        if (bid != null)
        {
            bid.setItemList(mkBidArchiveMapper.selectBidItems(bidId));
        }
        return bid;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBid(MkBidArchive bid)
    {
        bid.setDelFlag("0");
        if (bid.getBidStatus() == null)
        {
            bid.setBidStatus("0");
        }
        if (StringUtils.isEmpty(bid.getBidNo()))
        {
            bid.setBidNo(mkNumberRuleService.generateNumber("mk_bid"));
        }
        // 投标金额为空时按明细合计
        if (bid.getBidAmount() == null)
        {
            bid.setBidAmount(sumItems(bid));
        }
        int rows = mkBidArchiveMapper.insertBid(bid);
        insertItems(bid);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBid(MkBidArchive bid)
    {
        MkBidArchive existing = mkBidArchiveMapper.selectBidById(bid.getBidId());
        if (existing == null)
        {
            throw new ServiceException("投标档案不存在");
        }
        if ("2".equals(existing.getBidStatus()) && existing.getContractId() != null)
        {
            throw new ServiceException("中标档案已关联合同，不允许修改");
        }
        if (bid.getBidAmount() == null)
        {
            bid.setBidAmount(sumItems(bid));
        }
        mkBidArchiveMapper.deleteBidItemsByBidId(bid.getBidId());
        insertItems(bid);
        return mkBidArchiveMapper.updateBid(bid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBidByIds(Long[] bidIds)
    {
        for (Long bidId : bidIds)
        {
            MkBidArchive bid = mkBidArchiveMapper.selectBidById(bidId);
            if (bid != null && "2".equals(bid.getBidStatus()) && bid.getContractId() != null)
            {
                throw new ServiceException("投标【" + bid.getBidNo() + "】已关联合同，不允许删除");
            }
        }
        for (Long bidId : bidIds)
        {
            mkBidArchiveMapper.deleteBidItemsByBidId(bidId);
        }
        return mkBidArchiveMapper.deleteBidByIds(bidIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markResult(Long bidId, String bidStatus, BigDecimal winAmount, Date resultDate)
    {
        MkBidArchive bid = mkBidArchiveMapper.selectBidById(bidId);
        if (bid == null)
        {
            throw new ServiceException("投标档案不存在");
        }
        if (!"1".equals(bid.getBidStatus()) && !"0".equals(bid.getBidStatus()))
        {
            throw new ServiceException("只有编制/已投标状态的档案才能登记开标结果");
        }
        if (!"2".equals(bidStatus) && !"3".equals(bidStatus) && !"4".equals(bidStatus))
        {
            throw new ServiceException("开标结果只允许中标/未中标/废标");
        }
        if ("2".equals(bidStatus) && winAmount == null)
        {
            winAmount = bid.getBidAmount();
        }
        MkBidArchive update = new MkBidArchive();
        update.setBidId(bidId);
        update.setBidStatus(bidStatus);
        update.setWinAmount(winAmount);
        update.setResultDate(resultDate != null ? resultDate : new Date());
        update.setUpdateBy(SecurityUtils.getUsername());
        return mkBidArchiveMapper.updateBid(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MkBidArchive createContractFromBid(Long bidId)
    {
        MkBidArchive bid = mkBidArchiveMapper.selectBidById(bidId);
        if (bid == null)
        {
            throw new ServiceException("投标档案不存在");
        }
        if (!"2".equals(bid.getBidStatus()))
        {
            throw new ServiceException("只有中标的档案才能生成合同");
        }
        if (bid.getContractId() != null)
        {
            throw new ServiceException("该档案已生成过合同【" + bid.getContractNo() + "】，不可重复生成");
        }
        if (bid.getCustomerId() == null)
        {
            throw new ServiceException("投标档案未关联客户，无法生成合同");
        }
        MkContract contract = new MkContract();
        contract.setContractName(bid.getBidName() + "合同");
        contract.setContractType("0");
        contract.setCustomerId(bid.getCustomerId());
        contract.setCustomerName(bid.getCustomerName());
        contract.setContractAmount(bid.getWinAmount() != null ? bid.getWinAmount() : bid.getBidAmount());
        contract.setSignDate(new Date());
        contract.setEffectiveDate(new Date());
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, 1);
        contract.setExpireDate(cal.getTime());
        contract.setContractStatus("0");
        contract.setDelFlag("0");
        contract.setRemark("由投标档案 " + bid.getBidNo() + " 一键生成");
        mkContractService.insertContract(contract);

        MkBidArchive update = new MkBidArchive();
        update.setBidId(bidId);
        update.setContractId(contract.getContractId());
        update.setContractNo(contract.getContractNo());
        update.setUpdateBy(SecurityUtils.getUsername());
        mkBidArchiveMapper.updateBid(update);

        bid.setContractId(contract.getContractId());
        bid.setContractNo(contract.getContractNo());
        return bid;
    }

    private BigDecimal sumItems(MkBidArchive bid)
    {
        BigDecimal total = BigDecimal.ZERO;
        if (bid.getItemList() != null)
        {
            for (MkBidItem item : bid.getItemList())
            {
                if (item.getSubtotal() != null)
                {
                    total = total.add(item.getSubtotal());
                }
                else if (item.getUnitPrice() != null && item.getQuantity() != null)
                {
                    item.setSubtotal(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                    total = total.add(item.getSubtotal());
                }
            }
        }
        return total;
    }

    private void insertItems(MkBidArchive bid)
    {
        if (bid.getItemList() == null)
        {
            return;
        }
        int lineNo = 1;
        for (MkBidItem item : bid.getItemList())
        {
            item.setBidId(bid.getBidId());
            if (item.getLineNo() == null)
            {
                item.setLineNo(lineNo);
            }
            lineNo++;
            if (item.getSubtotal() == null && item.getUnitPrice() != null && item.getQuantity() != null)
            {
                item.setSubtotal(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
            }
            mkBidArchiveMapper.insertBidItem(item);
        }
    }
}

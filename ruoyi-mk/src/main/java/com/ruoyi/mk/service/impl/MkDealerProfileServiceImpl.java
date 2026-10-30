package com.ruoyi.mk.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.domain.MkDealerProfile;
import com.ruoyi.mk.mapper.MkCustomerMapper;
import com.ruoyi.mk.mapper.MkDealerProfileMapper;
import com.ruoyi.mk.service.IMkDealerProfileService;

/**
 * 经销商档案 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkDealerProfileServiceImpl implements IMkDealerProfileService
{
    @Autowired
    private MkDealerProfileMapper mkDealerProfileMapper;

    @Autowired
    private MkCustomerMapper mkCustomerMapper;

    @Override
    public List<MkDealerProfile> selectDealerProfileList(MkDealerProfile profile)
    {
        return mkDealerProfileMapper.selectDealerProfileList(profile);
    }

    @Override
    public MkDealerProfile selectDealerProfileByProfileId(Long profileId)
    {
        return mkDealerProfileMapper.selectDealerProfileByProfileId(profileId);
    }

    @Override
    public MkDealerProfile selectDealerProfileByCustomerId(Long customerId)
    {
        return mkDealerProfileMapper.selectDealerProfileByCustomerId(customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDealerProfile(MkDealerProfile profile)
    {
        // 客户必须存在且为经销商类型
        MkCustomer customer = mkCustomerMapper.selectCustomerTypeById(profile.getCustomerId());
        if (customer == null || !MkCustomer.TYPE_DEALER.equals(customer.getCustomerType()))
        {
            throw new ServiceException("档案必须归属「经销商」类型的客户");
        }
        // 一客户一档案
        if (mkDealerProfileMapper.selectDealerProfileByCustomerId(profile.getCustomerId()) != null)
        {
            throw new ServiceException("该经销商已存在档案，请直接编辑");
        }
        // 层级随上级推导：无上级=一级经销商，有上级=二级经销商
        profile.setDealerLevel(deriveLevel(customer.getParentCustomerId()));
        profile.setCreateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());
        return mkDealerProfileMapper.insertDealerProfile(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDealerProfile(MkDealerProfile profile)
    {
        MkDealerProfile old = mkDealerProfileMapper.selectDealerProfileByProfileId(profile.getProfileId());
        if (old == null)
        {
            throw new ServiceException("经销商档案不存在或已删除");
        }
        // 层级随上级推导，保证与客户层级关系一致
        MkCustomer customer = mkCustomerMapper.selectCustomerTypeById(old.getCustomerId());
        profile.setDealerLevel(deriveLevel(customer != null ? customer.getParentCustomerId() : null));
        profile.setUpdateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());
        return mkDealerProfileMapper.updateDealerProfile(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDealerProfileByProfileIds(Long[] profileIds)
    {
        // 经销商存在下级时不允许删除档案，防止层级孤儿（与客户删除口径一致）
        for (Long profileId : profileIds)
        {
            MkDealerProfile profile = mkDealerProfileMapper.selectDealerProfileByProfileId(profileId);
            if (profile != null && mkCustomerMapper.countChildDealer(profile.getCustomerId()) > 0)
            {
                throw new ServiceException("经销商「"
                        + (profile.getCustomerName() != null ? profile.getCustomerName() : "#" + profile.getCustomerId())
                        + "」存在下级经销商，请先调整其下级归属");
            }
        }
        return mkDealerProfileMapper.deleteDealerProfileByProfileIds(profileIds);
    }

    @Override
    public List<MkCustomer> selectDealerTree(MkCustomer query)
    {
        // 查询全部经销商客户（未删除）
        MkCustomer cond = new MkCustomer();
        cond.setCustomerType(MkCustomer.TYPE_DEALER);
        if (query != null && StringUtils.isNotEmpty(query.getCustomerName()))
        {
            cond.setCustomerName(query.getCustomerName());
        }
        List<MkCustomer> dealers = mkCustomerMapper.selectCustomerAllList(cond);
        return buildTree(dealers);
    }

    /** 无上级=一级经销商，有上级=二级经销商 */
    private String deriveLevel(Long parentCustomerId)
    {
        return parentCustomerId == null ? "1" : "2";
    }

    /** 按 parent_customer_id 构层级树 */
    private List<MkCustomer> buildTree(List<MkCustomer> dealers)
    {
        Map<Long, MkCustomer> nodeMap = new HashMap<>();
        for (MkCustomer d : dealers)
        {
            nodeMap.put(d.getCustomerId(), d);
        }
        List<MkCustomer> roots = new ArrayList<>();
        for (MkCustomer d : dealers)
        {
            Long parentId = d.getParentCustomerId();
            if (parentId != null && nodeMap.containsKey(parentId))
            {
                MkCustomer parent = nodeMap.get(parentId);
                if (parent.getChildren() == null)
                {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(d);
            }
            else
            {
                roots.add(d);
            }
        }
        return roots;
    }
}

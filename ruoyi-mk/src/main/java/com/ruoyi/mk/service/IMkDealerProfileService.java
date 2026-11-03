package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.domain.MkDealerProfile;

/**
 * 经销商档案 Service接口
 *
 * @author ruoyi
 */
public interface IMkDealerProfileService
{
    /** 查询经销商档案列表 */
    public List<MkDealerProfile> selectDealerProfileList(MkDealerProfile profile);

    /** 按档案ID查询经销商档案 */
    public MkDealerProfile selectDealerProfileByProfileId(Long profileId);

    /** 按客户ID查询经销商档案 */
    public MkDealerProfile selectDealerProfileByCustomerId(Long customerId);

    /** 新增经销商档案 */
    public int insertDealerProfile(MkDealerProfile profile);

    /** 修改经销商档案 */
    public int updateDealerProfile(MkDealerProfile profile);

    /** 批量删除经销商档案 */
    public int deleteDealerProfileByProfileIds(Long[] profileIds);

    /** 查询经销商层级树（customerType=2，按 parent_customer_id 构树） */
    public List<MkCustomer> selectDealerTree(MkCustomer query);
}

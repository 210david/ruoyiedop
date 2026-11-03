package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkDealerProfile;

/**
 * 经销商扩展档案 数据层
 *
 * @author ruoyi
 */
public interface MkDealerProfileMapper
{
    /** 查询经销商档案列表（关联客户信息） */
    public List<MkDealerProfile> selectDealerProfileList(MkDealerProfile profile);

    /** 按档案ID查询 */
    public MkDealerProfile selectDealerProfileByProfileId(Long profileId);

    /** 按客户ID查询（客户唯一档案） */
    public MkDealerProfile selectDealerProfileByCustomerId(Long customerId);

    /** 新增经销商档案 */
    public int insertDealerProfile(MkDealerProfile profile);

    /** 修改经销商档案 */
    public int updateDealerProfile(MkDealerProfile profile);

    /** 删除经销商档案 */
    public int deleteDealerProfileByProfileIds(Long[] profileIds);

    /** 按客户ID删除档案（客户删除时联动） */
    public int deleteDealerProfileByCustomerId(Long customerId);
}

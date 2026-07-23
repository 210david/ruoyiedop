package com.ruoyi.mk.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mk.domain.MkCustomer;

public interface MkCustomerMapper
{
    public List<MkCustomer> selectCustomerList(MkCustomer customer);

    /** 查询全部客户（含公海，用于导入查重） */
    public List<MkCustomer> selectCustomerAllList(MkCustomer customer);
    public MkCustomer selectCustomerById(Long customerId);
    public int insertCustomer(MkCustomer customer);
    public int updateCustomer(MkCustomer customer);
    public int deleteCustomerByIds(Long[] customerIds);

    /** 删除客户的标签关联 */
    public int deleteCustomerTags(Long customerId);

    /** 批量插入客户标签关联 */
    public int batchInsertCustomerTags(@Param("customerId") Long customerId, @Param("tagIds") Long[] tagIds);

    /** 查询客户的标签ID列表 */
    public Long[] selectTagIdsByCustomerId(Long customerId);

    /** 查询公海客户列表（user_id IS NULL） */
    public List<MkCustomer> selectPublicPoolList(MkCustomer customer);

    /** 释放客户到公海（user_id 置空） */
    public int releaseToPool(Long customerId);

    /** 批量分配客户负责人 */
    public int batchAssignCustomers(@Param("customerIds") Long[] customerIds, @Param("userId") Long userId, @Param("deptId") Long deptId, @Param("userName") String userName, @Param("deptName") String deptName);

    /** 批量更新客户状态 */
    public int batchUpdateStatus(@Param("customerIds") Long[] customerIds, @Param("customerStatus") String customerStatus);
}

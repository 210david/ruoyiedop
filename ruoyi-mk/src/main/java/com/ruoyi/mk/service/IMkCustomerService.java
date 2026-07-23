package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkCustomer;

/**
 * 企业客户 Service接口
 *
 * @author ruoyi
 */
public interface IMkCustomerService
{
    public List<MkCustomer> selectCustomerList(MkCustomer customer);
    public MkCustomer selectCustomerById(Long customerId);
    public int insertCustomer(MkCustomer customer);
    public int updateCustomer(MkCustomer customer);
    public int deleteCustomerByIds(Long[] customerIds);

    /** 查询公海客户列表 */
    public List<MkCustomer> selectPublicPoolList(MkCustomer customer);

    /** 分配客户负责人 */
    public int assignCustomer(Long customerId, Long userId, Long deptId, String userName, String deptName);

    /** 释放客户到公海 */
    public int releaseToPool(Long customerId);

    /** 从公海领取客户 */
    public int claimCustomer(Long customerId, Long userId, Long deptId, String userName, String deptName);

    /** 变更客户状态 */
    public int changeStatus(Long customerId, String customerStatus);

    /** 批量分配客户 */
    public int batchAssign(Long[] customerIds, Long userId, Long deptId, String userName, String deptName);

    /** 批量变更状态 */
    public int batchUpdateStatus(Long[] customerIds, String customerStatus);

    /** 导入客户数据 */
    public String importCustomer(List<MkCustomer> customerList, Boolean isUpdateSupport, String operName);
}

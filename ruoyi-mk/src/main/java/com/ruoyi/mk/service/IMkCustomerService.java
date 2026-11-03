package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.domain.MkCustomerChangeLog;

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
    public AjaxResult importCustomer(List<MkCustomer> customerList, Boolean isUpdateSupport, String updateKey, String operName);

    /** 变更客户类型（专用端点，记录变更日志） */
    public int changeType(MkCustomer change);

    /** 查询客户变更日志 */
    public List<MkCustomerChangeLog> selectChangeLogList(Long customerId);

    /** 查询客户列表（手机号明文，导出接口按权限选用） */
    public List<MkCustomer> selectCustomerListPlain(MkCustomer customer);
}

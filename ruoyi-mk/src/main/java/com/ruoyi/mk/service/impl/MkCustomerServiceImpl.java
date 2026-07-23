package com.ruoyi.mk.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.mapper.MkCustomerMapper;
import com.ruoyi.mk.service.IMkCustomerService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 企业客户 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkCustomerServiceImpl implements IMkCustomerService
{
    @Autowired
    private MkCustomerMapper mkCustomerMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MkCustomer> selectCustomerList(MkCustomer customer)
    {
        return mkCustomerMapper.selectCustomerList(customer);
    }

    @Override
    public MkCustomer selectCustomerById(Long customerId)
    {
        MkCustomer customer = mkCustomerMapper.selectCustomerById(customerId);
        if (customer != null)
        {
            Long[] tagIds = mkCustomerMapper.selectTagIdsByCustomerId(customerId);
            customer.setTagIds(tagIds);
        }
        return customer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCustomer(MkCustomer customer)
    {
        customer.setDelFlag("0");
        if (customer.getCustomerStatus() == null)
        {
            customer.setCustomerStatus("0");
        }
        if (customer.getCustomerLevel() == null)
        {
            customer.setCustomerLevel("3");
        }
        // 自动生成客户编号
        if (StringUtils.isEmpty(customer.getCustomerNo()))
        {
            customer.setCustomerNo(mkNumberRuleService.generateNumber("customer"));
        }
        int rows = mkCustomerMapper.insertCustomer(customer);
        if (customer.getTagIds() != null && customer.getTagIds().length > 0)
        {
            mkCustomerMapper.batchInsertCustomerTags(customer.getCustomerId(), customer.getTagIds());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCustomer(MkCustomer customer)
    {
        int rows = mkCustomerMapper.updateCustomer(customer);
        if (customer.getTagIds() != null)
        {
            mkCustomerMapper.deleteCustomerTags(customer.getCustomerId());
            if (customer.getTagIds().length > 0)
            {
                mkCustomerMapper.batchInsertCustomerTags(customer.getCustomerId(), customer.getTagIds());
            }
        }
        return rows;
    }

    @Override
    public int deleteCustomerByIds(Long[] customerIds)
    {
        return mkCustomerMapper.deleteCustomerByIds(customerIds);
    }

    @Override
    public List<MkCustomer> selectPublicPoolList(MkCustomer customer)
    {
        return mkCustomerMapper.selectPublicPoolList(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int assignCustomer(Long customerId, Long userId, Long deptId, String userName, String deptName)
    {
        MkCustomer customer = new MkCustomer();
        customer.setCustomerId(customerId);
        customer.setUserId(userId);
        customer.setDeptId(deptId);
        return mkCustomerMapper.updateCustomer(customer);
    }

    @Override
    public int releaseToPool(Long customerId)
    {
        return mkCustomerMapper.releaseToPool(customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int claimCustomer(Long customerId, Long userId, Long deptId, String userName, String deptName)
    {
        MkCustomer customer = new MkCustomer();
        customer.setCustomerId(customerId);
        customer.setUserId(userId);
        customer.setDeptId(deptId);
        return mkCustomerMapper.updateCustomer(customer);
    }

    @Override
    public int changeStatus(Long customerId, String customerStatus)
    {
        MkCustomer customer = new MkCustomer();
        customer.setCustomerId(customerId);
        customer.setCustomerStatus(customerStatus);
        return mkCustomerMapper.updateCustomer(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAssign(Long[] customerIds, Long userId, Long deptId, String userName, String deptName)
    {
        return mkCustomerMapper.batchAssignCustomers(customerIds, userId, deptId, userName, deptName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(Long[] customerIds, String customerStatus)
    {
        return mkCustomerMapper.batchUpdateStatus(customerIds, customerStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importCustomer(List<MkCustomer> customerList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(customerList) || customerList.size() == 0)
        {
            throw new RuntimeException("导入客户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (MkCustomer row : customerList)
        {
            try
            {
                // 验证企业名称不为空
                if (StringUtils.isEmpty(row.getCustomerName()))
                {
                    throw new RuntimeException("企业名称不能为空");
                }
                // 检查是否已存在（按企业名称匹配）
                MkCustomer query = new MkCustomer();
                query.setCustomerName(row.getCustomerName());
                List<MkCustomer> existing = mkCustomerMapper.selectCustomerAllList(query);
                if (existing.size() > 0)
                {
                    if (isUpdateSupport)
                    {
                        MkCustomer existingCustomer = existing.get(0);
                        row.setCustomerId(existingCustomer.getCustomerId());
                        mkCustomerMapper.updateCustomer(row);
                        successNum++;
                        successMsg.append("<br/>" + successNum + "、企业名称 " + row.getCustomerName() + " 更新成功");
                    }
                    else
                    {
                        failureNum++;
                        failureMsg.append("<br/>" + failureNum + "、企业名称 " + row.getCustomerName() + " 已存在");
                    }
                }
                else
                {
                    row.setDelFlag("0");
                    if (row.getCustomerStatus() == null)
                    {
                        row.setCustomerStatus("0");
                    }
                    if (row.getCustomerLevel() == null)
                    {
                        row.setCustomerLevel("3");
                    }
                    if (StringUtils.isEmpty(row.getCustomerNo()))
                    {
                        row.setCustomerNo(mkNumberRuleService.generateNumber("customer"));
                    }
                    mkCustomerMapper.insertCustomer(row);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、企业名称 " + row.getCustomerName() + " 导入成功");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                failureMsg.append("<br/>" + failureNum + "、企业名称 " + row.getCustomerName() + " 导入失败：" + e.getMessage());
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new RuntimeException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }
}

package com.ruoyi.mk.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.core.domain.AjaxResult;
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

    /**
     * 查询企业客户列表（已分配客户）。
     * 接入数据权限：按角色 data_scope 过滤（销售代表仅本人、销售经理本部门及以下等），
     * admin 及“全部数据”角色不受影响。别名 c 对应 mk_customer 表（含 user_id/dept_id）。
     */
    @Override
    @DataScope(userAlias = "c", deptAlias = "c")
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
    public AjaxResult importCustomer(List<MkCustomer> customerList, Boolean isUpdateSupport, String updateKey, String operName)
    {
        if (StringUtils.isNull(customerList) || customerList.size() == 0)
        {
            return AjaxResult.error("导入客户数据不能为空！");
        }
        // 默认按企业名称匹配
        if (StringUtils.isEmpty(updateKey))
        {
            updateKey = "customerName";
        }
        int successNum = 0;
        int failureNum = 0;
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();
        for (MkCustomer row : customerList)
        {
            try
            {
                // 1. 基础校验：企业名称不能为空
                if (StringUtils.isEmpty(row.getCustomerName()))
                {
                    throw new RuntimeException("企业名称不能为空");
                }
                // 2. 根据匹配字段校验并查找已存在的客户
                String matchValue = getMatchValue(row, updateKey);
                if (StringUtils.isEmpty(matchValue))
                {
                    String fieldLabel = getMatchFieldLabel(updateKey);
                    throw new RuntimeException("匹配字段「" + fieldLabel + "」不能为空");
                }
                MkCustomer matchedCustomer = findCustomerByMatchKey(updateKey, matchValue);

                if (matchedCustomer != null)
                {
                    if (isUpdateSupport)
                    {
                        row.setCustomerId(matchedCustomer.getCustomerId());
                        // 保留原编号不被覆盖
                        if (StringUtils.isEmpty(row.getCustomerNo()))
                        {
                            row.setCustomerNo(matchedCustomer.getCustomerNo());
                        }
                        row.setUpdateBy(operName);
                        mkCustomerMapper.updateCustomer(row);
                        successNum++;
                        successList.add(buildSuccessItem(row, "更新成功"));
                    }
                    else
                    {
                        failureNum++;
                        failureList.add(buildFailureItem(row, "客户已存在（" + getMatchFieldLabel(updateKey) + ": " + matchValue + "），未勾选更新选项"));
                    }
                }
                else
                {
                    // 新增：自动生成编号，设置默认值
                    row.setCustomerNo(mkNumberRuleService.generateNumber("customer"));
                    row.setDelFlag("0");
                    if (StringUtils.isEmpty(row.getCustomerStatus()))
                    {
                        row.setCustomerStatus("0");
                    }
                    if (StringUtils.isEmpty(row.getCustomerLevel()))
                    {
                        row.setCustomerLevel("3");
                    }
                    row.setCreateBy(operName);
                    mkCustomerMapper.insertCustomer(row);
                    successNum++;
                    successList.add(buildSuccessItem(row, "导入成功"));
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String errMsg = e.getMessage();
                if (errMsg != null && errMsg.length() > 200) { errMsg = errMsg.substring(0, 200) + "..."; }
                failureList.add(buildFailureItem(row, errMsg != null ? errMsg : "未知错误"));
            }
        }

        // 构建返回结果（结构化明细）
        AjaxResult ajax = AjaxResult.success();
        ajax.put("successNum", successNum);
        ajax.put("failureNum", failureNum);
        ajax.put("successList", successList);
        ajax.put("failureList", failureList);
        if (failureNum > 0 && successNum == 0)
        {
            ajax.put("code", 500);
            ajax.put("msg", "导入失败，共 " + failureNum + " 条数据异常");
        }
        else if (failureNum > 0)
        {
            ajax.put("msg", "部分导入成功：成功 " + successNum + " 条，失败 " + failureNum + " 条");
        }
        else
        {
            ajax.put("msg", "导入成功，共 " + successNum + " 条");
        }
        return ajax;
    }

    /**
     * 构建成功明细项
     */
    private Map<String, Object> buildSuccessItem(MkCustomer row, String result)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("customerNo", row.getCustomerNo() != null ? row.getCustomerNo() : "");
        item.put("customerName", row.getCustomerName() != null ? row.getCustomerName() : "");
        item.put("customerLevel", row.getCustomerLevel() != null ? row.getCustomerLevel() : "");
        item.put("result", result);
        return item;
    }

    /**
     * 构建失败明细项
     */
    private Map<String, Object> buildFailureItem(MkCustomer row, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("customerNo", row.getCustomerNo() != null ? row.getCustomerNo() : "");
        item.put("customerName", row.getCustomerName() != null ? row.getCustomerName() : "");
        item.put("customerLevel", row.getCustomerLevel() != null ? row.getCustomerLevel() : "");
        item.put("errorReason", errorReason);
        return item;
    }

    /**
     * 获取匹配字段的值
     */
    private String getMatchValue(MkCustomer row, String updateKey)
    {
        switch (updateKey)
        {
            case "creditCode":
                return row.getCreditCode();
            case "customerNo":
                return row.getCustomerNo();
            case "customerName":
            default:
                return row.getCustomerName();
        }
    }

    /**
     * 获取匹配字段的中文标签
     */
    private String getMatchFieldLabel(String updateKey)
    {
        switch (updateKey)
        {
            case "creditCode":
                return "统一社会信用代码";
            case "customerNo":
                return "客户编号";
            case "customerName":
            default:
                return "企业名称";
        }
    }

    /**
     * 根据匹配字段查找已存在的客户
     */
    private MkCustomer findCustomerByMatchKey(String updateKey, String matchValue)
    {
        MkCustomer query = new MkCustomer();
        switch (updateKey)
        {
            case "creditCode":
                query.setCreditCode(matchValue);
                break;
            case "customerNo":
                query.setCustomerNo(matchValue);
                break;
            case "customerName":
            default:
                query.setCustomerName(matchValue);
                break;
        }
        List<MkCustomer> candidates = mkCustomerMapper.selectCustomerAllList(query);
        // 精确匹配（selectCustomerAllList 用 LIKE，这里筛选精确匹配）
        for (MkCustomer c : candidates)
        {
            String val = getMatchValue(c, updateKey);
            if (val != null && val.equals(matchValue))
            {
                return c;
            }
        }
        return null;
    }
}

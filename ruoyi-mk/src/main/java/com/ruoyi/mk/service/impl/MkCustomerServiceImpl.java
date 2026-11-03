package com.ruoyi.mk.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mk.domain.MkContact;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.domain.MkCustomerChangeLog;
import com.ruoyi.mk.mapper.MkContactMapper;
import com.ruoyi.mk.mapper.MkCustomerChangeLogMapper;
import com.ruoyi.mk.mapper.MkCustomerMapper;
import com.ruoyi.mk.service.IMkCustomerService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 客户 Service实现（多类型客户体系：企业客户/经销商/个人客户）
 *
 * @author ruoyi
 */
@Service
public class MkCustomerServiceImpl implements IMkCustomerService
{
    /** 个人客户手机号格式（11位，1开头，第二位3-9） */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Autowired
    private MkCustomerMapper mkCustomerMapper;

    @Autowired
    private MkContactMapper mkContactMapper;

    @Autowired
    private MkCustomerChangeLogMapper mkCustomerChangeLogMapper;

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
        return maskPhoneList(mkCustomerMapper.selectCustomerList(customer));
    }

    @Override
    public MkCustomer selectCustomerById(Long customerId)
    {
        MkCustomer customer = mkCustomerMapper.selectCustomerById(customerId);
        if (customer != null)
        {
            Long[] tagIds = mkCustomerMapper.selectTagIdsByCustomerId(customerId);
            customer.setTagIds(tagIds);
            customer.setContactPhone(maskPhone(customer.getContactPhone()));
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
        // 客户类型默认企业客户
        if (StringUtils.isEmpty(customer.getCustomerType()))
        {
            customer.setCustomerType(MkCustomer.TYPE_ENTERPRISE);
        }
        validateCustomerCommon(customer, null);
        if (MkCustomer.TYPE_PERSONAL.equals(customer.getCustomerType()))
        {
            // 个人客户名称兜底为手机号
            if (StringUtils.isEmpty(customer.getCustomerName()))
            {
                customer.setCustomerName(customer.getContactPhone());
            }
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
        // 个人客户自动创建主联系人
        if (MkCustomer.TYPE_PERSONAL.equals(customer.getCustomerType()))
        {
            MkContact contact = new MkContact();
            contact.setCustomerId(customer.getCustomerId());
            contact.setName(customer.getCustomerName());
            contact.setPhone(customer.getContactPhone());
            contact.setIsPrimary("1");
            contact.setCreateBy(customer.getCreateBy());
            mkContactMapper.insertContact(contact);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCustomer(MkCustomer customer)
    {
        MkCustomer old = mkCustomerMapper.selectCustomerById(customer.getCustomerId());
        if (old == null)
        {
            throw new ServiceException("客户不存在或已删除");
        }
        // 客户类型变更必须走专用端点（权限收敛+变更日志）
        if (StringUtils.isNotEmpty(customer.getCustomerType()) && !customer.getCustomerType().equals(old.getCustomerType()))
        {
            throw new ServiceException("客户类型不允许直接修改，请使用「变更类型」功能");
        }
        // 编辑表单回显的是脱敏手机号，含 * 表示未修改，沿用原值
        if (StringUtils.isNotEmpty(customer.getContactPhone()) && customer.getContactPhone().contains("*"))
        {
            customer.setContactPhone(old.getContactPhone());
        }
        // 校验按原类型执行
        customer.setCustomerType(old.getCustomerType());
        validateCustomerCommon(customer, old.getCustomerId());
        // 个人客户手机号不允许被编辑清空（XML 无条件更新）
        if (MkCustomer.TYPE_PERSONAL.equals(customer.getCustomerType()) && StringUtils.isEmpty(customer.getContactPhone()))
        {
            customer.setContactPhone(old.getContactPhone());
        }
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
        // 经销商存在下级时不允许删除，防止层级孤儿
        for (Long customerId : customerIds)
        {
            MkCustomer customer = mkCustomerMapper.selectCustomerTypeById(customerId);
            if (customer != null && MkCustomer.TYPE_DEALER.equals(customer.getCustomerType())
                    && mkCustomerMapper.countChildDealer(customerId) > 0)
            {
                throw new ServiceException("经销商「" + customer.getCustomerName() + "」存在下级经销商，请先调整其下级归属");
            }
        }
        return mkCustomerMapper.deleteCustomerByIds(customerIds);
    }

    @Override
    public List<MkCustomer> selectPublicPoolList(MkCustomer customer)
    {
        return maskPhoneList(mkCustomerMapper.selectPublicPoolList(customer));
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
        // 默认按客户名称匹配
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
                // 1. 基础校验：客户名称不能为空
                if (StringUtils.isEmpty(row.getCustomerName()))
                {
                    throw new ServiceException("客户名称不能为空");
                }
                // 2. 字典类字段合法性校验
                validateDictFields(row);
                // 3. 根据匹配字段校验并查找已存在的客户
                String matchValue = getMatchValue(row, updateKey);
                if (StringUtils.isEmpty(matchValue))
                {
                    String fieldLabel = getMatchFieldLabel(updateKey);
                    throw new ServiceException("匹配字段「" + fieldLabel + "」不能为空");
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
     * 校验字典类字段：客户等级、客户状态
     */
    private void validateDictFields(MkCustomer row)
    {
        // 客户等级（dictType: marketing_customer_level）
        validateDictTypeField("客户等级", row.getCustomerLevel(), "marketing_customer_level");
        // 客户状态（dictType: marketing_customer_status）
        validateDictTypeField("客户状态", row.getCustomerStatus(), "marketing_customer_status");
        // 累计交易额校验
        if (row.getTotalAmount() != null && row.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("累计交易额不能为负数");
        }
    }

    /**
     * 校验dictType字段：如果值不为空但不是合法编码，说明用户输入了不合法的文字或数字
     */
    private void validateDictTypeField(String fieldLabel, String value, String dictType)
    {
        if (StringUtils.isNotEmpty(value))
        {
            String label = DictUtils.getDictLabel(dictType, value);
            if (StringUtils.isEmpty(label))
            {
                throw new ServiceException(fieldLabel + "值不合法，请填写文字描述（参考导入说明中的合法值）");
            }
        }
    }

    /**
     * 构建成功明细项
     */
    private Map<String, Object> buildSuccessItem(MkCustomer row, String result)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("customerNo", row.getCustomerNo() != null ? row.getCustomerNo() : "");
        item.put("customerName", row.getCustomerName() != null ? row.getCustomerName() : "");
        item.put("creditCode", row.getCreditCode() != null ? row.getCreditCode() : "");
        item.put("customerLevel", dictText(row.getCustomerLevel(), "marketing_customer_level"));
        item.put("customerStatus", dictText(row.getCustomerStatus(), "marketing_customer_status"));
        item.put("firstContactDate", row.getFirstContactDate() != null ? row.getFirstContactDate().toString() : "");
        item.put("cooperationDate", row.getCooperationDate() != null ? row.getCooperationDate().toString() : "");
        item.put("totalAmount", row.getTotalAmount() != null ? row.getTotalAmount().toString() : "");
        item.put("userName", row.getUserName() != null ? row.getUserName() : "");
        item.put("deptName", row.getDeptName() != null ? row.getDeptName() : "");
        item.put("result", result);
        return item;
    }

    /**
     * 构建失败明细项（去掉自动生成的客户编号，列与导入模板对齐）
     */
    private Map<String, Object> buildFailureItem(MkCustomer row, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("customerName", row.getCustomerName() != null ? row.getCustomerName() : "");
        item.put("creditCode", row.getCreditCode() != null ? row.getCreditCode() : "");
        item.put("customerLevel", dictText(row.getCustomerLevel(), "marketing_customer_level"));
        item.put("customerStatus", dictText(row.getCustomerStatus(), "marketing_customer_status"));
        item.put("firstContactDate", row.getFirstContactDate() != null ? row.getFirstContactDate().toString() : "");
        item.put("cooperationDate", row.getCooperationDate() != null ? row.getCooperationDate().toString() : "");
        item.put("totalAmount", row.getTotalAmount() != null ? row.getTotalAmount().toString() : "");
        item.put("userName", row.getUserName() != null ? row.getUserName() : "");
        item.put("deptName", row.getDeptName() != null ? row.getDeptName() : "");
        item.put("errorReason", errorReason);
        return item;
    }

    /**
     * 将编码转换为文字标签（用于明细展示）
     */
    private String dictText(String code, String dictType)
    {
        if (StringUtils.isEmpty(code)) { return ""; }
        String label = DictUtils.getDictLabel(dictType, code, "");
        return StringUtils.isNotEmpty(label) ? label : code;
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
                return "客户名称";
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

    // ====== 多类型客户体系：类型变更 / 变更日志 / 个人客户明文导出 ======

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeType(MkCustomer change)
    {
        MkCustomer old = mkCustomerMapper.selectCustomerById(change.getCustomerId());
        if (old == null)
        {
            throw new ServiceException("客户不存在或已删除");
        }
        String newType = change.getCustomerType();
        if (StringUtils.isEmpty(newType))
        {
            throw new ServiceException("请指定变更后的客户类型");
        }
        if (newType.equals(old.getCustomerType()))
        {
            throw new ServiceException("客户类型未发生变化");
        }
        // 个人客户数据模型差异大，不允许转为企业/经销商（P1-10：需另建档案）
        if (MkCustomer.TYPE_PERSONAL.equals(old.getCustomerType()))
        {
            throw new ServiceException("个人客户不允许变更为企业客户或经销商，请为其新建档案");
        }
        // 变更为个人客户：手机号必填（沿用原值或新填），格式与唯一性校验
        if (MkCustomer.TYPE_PERSONAL.equals(newType))
        {
            if (StringUtils.isNotEmpty(change.getContactPhone()) && change.getContactPhone().contains("*"))
            {
                change.setContactPhone(old.getContactPhone());
            }
            if (StringUtils.isEmpty(change.getContactPhone()))
            {
                throw new ServiceException("变更为个人客户时必须填写手机号");
            }
            checkPhoneFormat(change.getContactPhone());
            checkPhoneUnique(change.getContactPhone(), change.getCustomerId());
            // 个人客户无上级
            change.setParentCustomerId(null);
        }
        else if (MkCustomer.TYPE_DEALER.equals(newType))
        {
            // 变更为经销商：可指定上级，上级必须为经销商类型；个人手机号字段置空
            change.setContactPhone(null);
            validateParent(change.getParentCustomerId(), change.getCustomerId());
        }
        else
        {
            // 变更为企业客户：清空上级与个人手机号
            change.setContactPhone(null);
            change.setParentCustomerId(null);
        }
        change.setUpdateBy(SecurityUtils.getUsername());
        int rows = mkCustomerMapper.updateCustomer(change);
        if (rows > 0)
        {
            MkCustomerChangeLog log = new MkCustomerChangeLog();
            log.setCustomerId(change.getCustomerId());
            log.setChangeType("customer_type");
            log.setOldValue(typeLabel(old.getCustomerType()));
            log.setNewValue(typeLabel(newType));
            log.setChangeReason(change.getChangeReason());
            log.setCreateBy(SecurityUtils.getUsername());
            mkCustomerChangeLogMapper.insertChangeLog(log);
            // 变更为个人客户时补建主联系人
            if (MkCustomer.TYPE_PERSONAL.equals(newType))
            {
                MkContact contact = new MkContact();
                contact.setCustomerId(change.getCustomerId());
                contact.setName(old.getCustomerName());
                contact.setPhone(change.getContactPhone());
                contact.setIsPrimary("1");
                contact.setCreateBy(SecurityUtils.getUsername());
                mkContactMapper.insertContact(contact);
            }
        }
        return rows;
    }

    @Override
    public List<MkCustomerChangeLog> selectChangeLogList(Long customerId)
    {
        MkCustomerChangeLog query = new MkCustomerChangeLog();
        query.setCustomerId(customerId);
        return mkCustomerChangeLogMapper.selectChangeLogList(query);
    }

    @Override
    @DataScope(userAlias = "c", deptAlias = "c")
    public List<MkCustomer> selectCustomerListPlain(MkCustomer customer)
    {
        // 手机号不做脱敏，仅导出接口按 exportPersonal 权限选用
        return mkCustomerMapper.selectCustomerList(customer);
    }

    /**
     * 多类型客户通用校验：
     * 1. 类型合法性；2. 个人客户手机号必填/格式/唯一；
     * 3. 经销商上级校验与防环；4. 非个人清空手机号、非经销商清空上级。
     */
    private void validateCustomerCommon(MkCustomer customer, Long excludeId)
    {
        String type = customer.getCustomerType();
        if (!MkCustomer.TYPE_ENTERPRISE.equals(type) && !MkCustomer.TYPE_DEALER.equals(type) && !MkCustomer.TYPE_PERSONAL.equals(type))
        {
            throw new ServiceException("客户类型不合法");
        }
        if (MkCustomer.TYPE_PERSONAL.equals(type))
        {
            if (StringUtils.isEmpty(customer.getContactPhone()))
            {
                throw new ServiceException("个人客户必须填写手机号");
            }
            checkPhoneFormat(customer.getContactPhone());
            checkPhoneUnique(customer.getContactPhone(), excludeId);
            // 个人客户无上级经销商
            customer.setParentCustomerId(null);
        }
        else
        {
            // 手机号为个人客户专属字段，其他类型必须置空（释放唯一键）
            customer.setContactPhone(null);
            if (MkCustomer.TYPE_DEALER.equals(type))
            {
                validateParent(customer.getParentCustomerId(), excludeId);
            }
            else
            {
                customer.setParentCustomerId(null);
            }
        }
    }

    /** 校验手机号格式 */
    private void checkPhoneFormat(String phone)
    {
        if (!PHONE_PATTERN.matcher(phone).matches())
        {
            throw new ServiceException("手机号格式不正确，应为11位有效手机号（1开头）");
        }
    }

    /** 校验手机号唯一（个人客户手机号唯一建档） */
    private void checkPhoneUnique(String phone, Long excludeId)
    {
        MkCustomer exists = mkCustomerMapper.selectByContactPhone(phone, excludeId);
        if (exists != null)
        {
            throw new ServiceException("该手机号已建档：客户「" + exists.getCustomerName() + "」");
        }
    }

    /** 校验上级经销商：必须存在且为经销商类型 */
    private void validateParent(Long parentCustomerId, Long selfId)
    {
        if (parentCustomerId == null)
        {
            return;
        }
        if (selfId != null && parentCustomerId.equals(selfId))
        {
            throw new ServiceException("上级经销商不能选择自己");
        }
        MkCustomer parent = mkCustomerMapper.selectCustomerTypeById(parentCustomerId);
        if (parent == null || !MkCustomer.TYPE_DEALER.equals(parent.getCustomerType()))
        {
            throw new ServiceException("上级经销商必须是「经销商」类型的客户");
        }
        if (selfId != null && isDescendant(parentCustomerId, selfId))
        {
            throw new ServiceException("上级经销商选择冲突：该客户位于自身层级链中");
        }
    }

    /** 沿上级链向上遍历，判断 targetId 是否位于 startId 的祖先链中（防环） */
    private boolean isDescendant(Long startId, Long targetId)
    {
        int maxDepth = 20;
        Long current = startId;
        while (current != null && maxDepth-- > 0)
        {
            if (current.equals(targetId))
            {
                return true;
            }
            MkCustomer node = mkCustomerMapper.selectCustomerTypeById(current);
            if (node == null)
            {
                return false;
            }
            current = node.getParentCustomerId();
        }
        return false;
    }

    /** 客户类型字典标签 */
    private String typeLabel(String type)
    {
        String label = DictUtils.getDictLabel("mk_customer_type", type, "");
        return StringUtils.isNotEmpty(label) ? label : type;
    }

    /** 手机号脱敏：保留前3后4（幂等，含 * 不重复处理） */
    private String maskPhone(String phone)
    {
        if (StringUtils.isEmpty(phone) || phone.contains("*"))
        {
            return phone;
        }
        if (phone.length() != 11)
        {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /** 批量脱敏个人客户手机号 */
    private List<MkCustomer> maskPhoneList(List<MkCustomer> list)
    {
        if (list != null)
        {
            for (MkCustomer c : list)
            {
                c.setContactPhone(maskPhone(c.getContactPhone()));
            }
        }
        return list;
    }
}

package com.ruoyi.integration.controller;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.integration.domain.IntegrationUserBinding;
import com.ruoyi.integration.mapper.IntegrationUserBindingMapper;
import com.ruoyi.integration.service.IDingtalkSyncService;
import com.ruoyi.integration.service.IWecomSyncService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 第三方考勤集成管理接口（钉钉 / 企业微信）
 *
 * 同一套接口通过 connectorType 参数区分连接器：
 *   connectorType=dingtalk（默认，兼容历史调用）
 *   connectorType=wecom
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/integration/attendance")
public class IntegrationAttendanceController extends BaseController
{
    /** 连接器类型：钉钉 */
    private static final String CONNECTOR_DINGTALK = "dingtalk";

    /** 连接器类型：企业微信 */
    private static final String CONNECTOR_WECOM = "wecom";

    @Autowired
    private IDingtalkSyncService dingtalkSyncService;

    @Autowired
    private IWecomSyncService wecomSyncService;

    @Autowired
    private IntegrationUserBindingMapper bindingMapper;

    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;

    @Autowired
    private ISysUserService userService;

    /** 一一绑定约束在 DB 层的兜底（与唯一索引 uk_connector_user 保持一致） */

    /**
     * 归一化连接器类型（未传或非法值一律回退钉钉，保证历史前端调用行为不变）
     */
    private String normalizeConnector(String connectorType)
    {
        return CONNECTOR_WECOM.equalsIgnoreCase(connectorType) ? CONNECTOR_WECOM : CONNECTOR_DINGTALK;
    }

    /**
     * 连接器中文名（用于提示文案）
     */
    private String connectorLabel(String connectorType)
    {
        return CONNECTOR_WECOM.equals(connectorType) ? "企业微信" : "钉钉";
    }

    /**
     * 绑定关系列表（含未匹配，用于人工核对）
     *
     * @param connectorType 连接器类型（dingtalk 默认 / wecom）
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:list')")
    @GetMapping("/bindings")
    public TableDataInfo bindings(IntegrationUserBinding query, String connectorType)
    {
        String connector = normalizeConnector(StringUtils.isNotEmpty(connectorType) ? connectorType : query.getConnectorType());
        query.setConnectorType(connector);
        startPage();
        List<IntegrationUserBinding> list = bindingMapper.selectIntegrationUserBindingList(query);
        return getDataTable(list);
    }

    /**
     * 绑定状态统计（列表页状态页签计数：全部 / 已匹配 / 未匹配）
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:list')")
    @GetMapping("/bindingStats")
    public AjaxResult bindingStats(String connectorType)
    {
        String connector = normalizeConnector(connectorType);
        Long all = bindingMapper.countBinding(connector, null);
        Long matched = bindingMapper.countBinding(connector, "yes");
        long total = all == null ? 0L : all;
        long matchedCount = matched == null ? 0L : matched;
        AjaxResult ajax = success();
        ajax.put("all", total);
        ajax.put("matched", matchedCount);
        ajax.put("unmatched", total - matchedCount);
        return ajax;
    }

    /**
     * 同步通讯录并自动匹配员工
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:sync')")
    @PostMapping("/syncContacts")
    public AjaxResult syncContacts(String connectorType)
    {
        String connector = normalizeConnector(connectorType);
        return CONNECTOR_WECOM.equals(connector)
                ? wecomSyncService.syncContacts()
                : dingtalkSyncService.syncContacts();
    }

    /**
     * 手动同步打卡数据（按日期范围）
     *
     * @param beginDate yyyy-MM-dd
     * @param endDate   yyyy-MM-dd
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:sync')")
    @PostMapping("/sync/{beginDate}/{endDate}")
    public AjaxResult sync(@PathVariable("beginDate") String beginDate, @PathVariable("endDate") String endDate,
            String connectorType)
    {
        Date begin = DateUtils.parseDate(beginDate);
        Date end = DateUtils.parseDate(endDate);
        if (begin == null || end == null)
        {
            return error("日期格式应为 yyyy-MM-dd");
        }
        String connector = normalizeConnector(connectorType);
        return CONNECTOR_WECOM.equals(connector)
                ? wecomSyncService.syncAttendance(begin, end)
                : dingtalkSyncService.syncAttendance(begin, end);
    }

    /**
     * 人工绑定：将绑定关系指定到平台组织用户（sys_user）。
     *
     * 与企业微信一致的绑定口径：绑定对象是系统组织用户而非 HR 员工档案。
     * 前端提交 userId（推荐）或旧版 employeeNo（兼容，按登录账号解析为用户）。
     * 所选用户若已关联 HR 员工档案，则同时回填档案工号/档案ID，供考勤同步按工号落库；
     * 未关联档案时同样允许绑定，考勤同步时该用户打卡按「无档案映射」计入未映射。
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:sync')")
    @PutMapping("/binding/bind")
    public AjaxResult bind(@RequestBody IntegrationUserBinding body)
    {
        if (body.getBindingId() == null
                || (body.getUserId() == null && StringUtils.isEmpty(body.getEmployeeNo())))
        {
            return error("参数不完整：需要 bindingId 与 userId（或兼容旧参数 employeeNo）");
        }
        IntegrationUserBinding existing = bindingMapper.selectIntegrationUserBindingById(body.getBindingId());
        if (existing == null)
        {
            return error("绑定关系不存在");
        }
        // 解析目标系统用户：优先 userId，兼容旧工号入参（工号即登录账号）
        SysUser targetUser = body.getUserId() != null
                ? userService.selectUserById(body.getUserId())
                : userService.selectUserByUserName(body.getEmployeeNo());
        if (targetUser == null)
        {
            return error("所选平台用户不存在或已删除，请重新选择");
        }
        if ("1".equals(targetUser.getStatus()))
        {
            return error("平台用户「" + targetUser.getNickName() + "」已停用，不能绑定");
        }
        // 一一绑定约束：一个系统用户在同一连接器下只能绑定一个外部用户，否则打卡数据会互相覆盖
        String connector = normalizeConnector(existing.getConnectorType());
        IntegrationUserBinding occupied = bindingMapper.selectOccupiedBinding(connector,
                targetUser.getUserId(), body.getBindingId());
        if (occupied != null)
        {
            return error("该用户已被" + connectorLabel(connector) + "用户「" + occupied.getExternalName()
                    + "」绑定，一个用户只能绑定一个" + connectorLabel(connector) + "用户，请先解绑后再绑定");
        }
        IntegrationUserBinding update = new IntegrationUserBinding();
        update.setBindingId(body.getBindingId());
        update.setUserId(targetUser.getUserId());
        update.setEmployeeNo(resolveEmployeeNo(targetUser));
        update.setEmployeeId(resolveEmployeeId(targetUser));
        update.setMatchType("manual");
        update.setSyncTime(new Date());
        update.setUpdateBy(getUsername());
        bindingMapper.updateIntegrationUserBinding(update);
        return success("绑定成功：" + existing.getExternalName() + " → " + targetUser.getNickName()
                + (StringUtils.isNotEmpty(update.getEmployeeNo()) ? "（档案工号 " + update.getEmployeeNo() + "）" : "（该用户未关联员工档案，打卡数据将无法写入考勤）"));
    }

    /**
     * 解析系统用户对应的 HR 员工档案（biz_hr_employee.user_id 关联），未关联返回 null
     */
    private HrEmployee selectEmployeeByUserId(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        HrEmployee query = new HrEmployee();
        query.setUserId(userId);
        List<HrEmployee> employees = hrEmployeeMapper.selectHrEmployeeList(query);
        return employees.isEmpty() ? null : employees.get(0);
    }

    /**
     * 考勤落库工号：仅当系统用户关联了 HR 员工档案时返回档案工号；
     * 未关联档案返回 null（考勤导入按工号强校验档案，回退登录账号会产生导入失败，
     * 故无档案用户的打卡在同步时按「无档案映射」计入未映射并提示）
     */
    private String resolveEmployeeNo(SysUser user)
    {
        HrEmployee employee = selectEmployeeByUserId(user.getUserId());
        return employee != null ? employee.getEmployeeNo() : null;
    }

    /**
     * 考勤落库档案ID：未关联员工档案返回 null
     */
    private Long resolveEmployeeId(SysUser user)
    {
        HrEmployee employee = selectEmployeeByUserId(user.getUserId());
        return employee != null ? employee.getEmployeeId() : null;
    }

    /**
     * 检查平台用户是否已被其他外部用户绑定（一一绑定约束，供前端选人后即时提示）
     *
     * @param userId        平台组织用户ID（sys_user.user_id）
     * @param employeeNo    兼容旧参数：登录账号（工号），userId 为空时按账号解析
     * @param bindingId     当前操作的绑定关系ID（变更绑定时排除自身），可为 null
     * @param connectorType 连接器类型（dingtalk 默认 / wecom）
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:sync')")
    @GetMapping("/binding/occupant")
    public AjaxResult occupant(Long userId, String employeeNo, Long bindingId, String connectorType)
    {
        AjaxResult ajax = success();
        Long targetUserId = userId;
        if (targetUserId == null && StringUtils.isNotEmpty(employeeNo))
        {
            // 兼容旧工号入参：工号即登录账号
            SysUser user = userService.selectUserByUserName(employeeNo);
            targetUserId = user != null ? user.getUserId() : null;
        }
        if (targetUserId == null)
        {
            ajax.put("occupied", false);
            return ajax;
        }
        IntegrationUserBinding occupied = bindingMapper.selectOccupiedBinding(normalizeConnector(connectorType),
                targetUserId, bindingId);
        ajax.put("occupied", occupied != null);
        if (occupied != null)
        {
            ajax.put("externalName", occupied.getExternalName());
            ajax.put("mobile", occupied.getMobile());
            ajax.put("bindingId", occupied.getBindingId());
            ajax.put("employeeName", occupied.getEmployeeName());
        }
        return ajax;
    }

    /**
     * 解绑：清空该外部平台用户与平台组织用户的关联，外部用户信息保留以便重新绑定或等待下次自动匹配
     * <p>仅解除对应关系，不会删除已同步到 HR 考勤的历史记录</p>
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:sync')")
    @PutMapping("/binding/unbind/{bindingId}")
    public AjaxResult unbind(@PathVariable("bindingId") Long bindingId)
    {
        IntegrationUserBinding existing = bindingMapper.selectIntegrationUserBindingById(bindingId);
        if (existing == null)
        {
            return error("绑定关系不存在");
        }
        if (existing.getUserId() == null)
        {
            return error("该" + connectorLabel(normalizeConnector(existing.getConnectorType())) + "用户当前未绑定平台用户，无需解绑");
        }
        bindingMapper.clearBinding(bindingId, getUsername());
        return success("已解绑：" + existing.getExternalName() + " ✕ " + existing.getEmployeeName());
    }
}
